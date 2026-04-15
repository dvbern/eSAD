package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungAbschlussMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungAbschlussService untersuchungAbschlussService;

    @Nullable
    public static UntersuchungAbschlussJson toJson(@Nullable UntersuchungAbschluss abschluss) {
        if (abschluss == null) {
            return null;
        }
        return new UntersuchungAbschlussJson(
            abschluss.getId(),
            EntityInfoJson.fromEntity(abschluss),
            new UntersuchungAbschlussSaveJson(
                SingleValueEnhancedMapper.toJson(AbschlussResultat.class, abschluss.getUeberweisungErfolgt()),
                SingleValueEnhancedMapper.toJson(AbschlussResultat.class, abschluss.getAerztlicheValidierungErforderlich()),
                SingleValueEnhancedMapper.toJson(AbschlussResultat.class, abschluss.getBeratungDurchgefuehrt()),
                abschluss.getBeurteilungEmpfohleneMassnahmen(),
                abschluss.getVersion()
            )
        );
    }

    @NonNull
    public static UntersuchungAbschluss toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungAbschluss abschluss,
        @NonNull UntersuchungAbschlussSaveJson saveJson
    ) {
        if (abschluss == null) {
            abschluss = UntersuchungAbschluss.forUntersuchung(untersuchung);
        }

        abschluss.setUeberweisungErfolgt(SingleValueEnhancedMapper.toEntity(
            AbschlussResultat.class,
            saveJson.getUeberweisungErfolgt()));
        abschluss.setAerztlicheValidierungErforderlich(SingleValueEnhancedMapper.toEntity(
            AbschlussResultat.class,
            saveJson.getAerztlicheValidierungErforderlich()));
        abschluss.setBeratungDurchgefuehrt(SingleValueEnhancedMapper.toEntity(
            AbschlussResultat.class,
            saveJson.getBeratungDurchgefuehrt()));
        abschluss.setBeurteilungEmpfohleneMassnahmen(saveJson.getBeurteilungEmpfohleneMassnahmen());

        return abschluss;
    }

    @Nullable
    public UntersuchungAbschlussJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungAbschluss());
    }

    @NonNull
    public UntersuchungAbschlussJson saveAbschluss(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungAbschlussSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungAbschluss abschluss = untersuchung.getUntersuchungAbschluss();
        UntersuchungAbschluss entity = toEntity(untersuchung, abschluss, saveJson);
        return toJson(untersuchungAbschlussService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
