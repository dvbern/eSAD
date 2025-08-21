package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungImpfungenMassnahmenMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfungenMassnahmenService untersuchungImpfungenMassnahmenService;

    @Nullable
    public static UntersuchungImpfungenMassnahmenJson toJson(
        @Nullable UntersuchungImpfungenMassnahmen massnahmen
    ) {
        if (massnahmen == null) {
            return null;
        }
        return new UntersuchungImpfungenMassnahmenJson(
            massnahmen.getId(),
            EntityInfoJson.fromEntity(massnahmen),
            new UntersuchungImpfungenMassnahmenSaveJson(
                massnahmen.isKeineMassnahmen(),
                massnahmen.isVormerkenFuerGruppenFolgetermin(),
                massnahmen.isImpfberatungMpaDurchgefuehrt(),
                massnahmen.getImpfberatungMpaDurchgefuehrtBemerkungen(),
                massnahmen.isInformationenAbgegeben(),
                massnahmen.getInformationEltern(),
                massnahmen.isInformationHausarzt(),
                massnahmen.isRueckfrageHausarzt(),
                massnahmen.getBemerkungen()
            )
        );
    }

    @NonNull
    private UntersuchungImpfungenMassnahmen toEntity(
        @NonNull UntersuchungImpfungen impfungen,
        @Nullable UntersuchungImpfungenMassnahmen massnahmen,
        @NonNull UntersuchungImpfungenMassnahmenSaveJson saveJson
    ) {
        if (massnahmen == null) {
            massnahmen = new UntersuchungImpfungenMassnahmen();
            massnahmen.setUntersuchungImpfungen(impfungen);
            impfungen.setUntersuchungImpfungenMassnahmen(massnahmen);
        }

        massnahmen.setKeineMassnahmen(saveJson.isKeineMassnahmen());
        massnahmen.setVormerkenFuerGruppenFolgetermin(saveJson.isVormerkenFuerGruppenFolgetermin());
        massnahmen.setImpfberatungMpaDurchgefuehrt(saveJson.isImpfberatungMpaDurchgefuehrt());
        massnahmen.setImpfberatungMpaDurchgefuehrtBemerkungen(saveJson.getImpfberatungMpaDurchgefuehrtBemerkungen());
        massnahmen.setInformationenAbgegeben(saveJson.isInformationenAbgegeben());
        massnahmen.setInformationEltern(saveJson.getInformationEltern());
        massnahmen.setInformationHausarzt(saveJson.isInformationHausarzt());
        massnahmen.setRueckfrageHausarzt(saveJson.isRueckfrageHausarzt());
        massnahmen.setBemerkungen(saveJson.getBemerkungen());

        return massnahmen;
    }

    @NonNull
    public UntersuchungImpfungenMassnahmenJson save(
        @NonNull ID<Einladung> einladungId,
        @NonNull UntersuchungImpfungenMassnahmenSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        UntersuchungImpfungenMassnahmen massnahmen = impfungen.getUntersuchungImpfungenMassnahmen();
        UntersuchungImpfungenMassnahmen entity = toEntity(impfungen, massnahmen, saveJson);
        return toJson(untersuchungImpfungenMassnahmenService.persist(entity));
    }

    public void einzelTerminErstellen(@NonNull ID<Einladung> einladungId) {
        untersuchungImpfungenMassnahmenService.einzelTerminErstellen(einladungId);
    }
}
