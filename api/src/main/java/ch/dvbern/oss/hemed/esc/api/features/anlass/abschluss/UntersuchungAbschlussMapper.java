package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

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
    public UntersuchungAbschlussJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungAbschluss());
    }

    @NonNull
    public UntersuchungAbschlussJson saveAbschluss(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungAbschlussSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungAbschluss abschluss = untersuchung.getUntersuchungAbschluss();
        UntersuchungAbschluss entity = toEntity(untersuchung, abschluss, saveJson);
        return toJson(untersuchungAbschlussService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
