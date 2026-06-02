package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.Set;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.InfomaterialMapper;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlagsMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungMassnahmenMapper {

    private final UntersuchungService untersuchungService;
    private final InfomaterialMapper infomaterialMapper;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;

    @Nullable
    public static UntersuchungMassnahmenJson toJson(@Nullable UntersuchungMassnahmen massnahmen) {
        if (massnahmen == null) {
            return null;
        }
        return new UntersuchungMassnahmenJson(
            massnahmen.getId(), EntityInfoJson.fromEntity(massnahmen), new UntersuchungMassnahmenSaveJson(
            massnahmen.isKeineMassnahmen(),
            massnahmen.isBeratungMPA(),
            massnahmen.isBeratungSchulAerztin(),
            massnahmen.isAnderes(),
            massnahmen.getAnderesBemerkung(),
            massnahmen.isBereitsBehandeltSehen(),
            massnahmen.isBereitsBehandeltHoeren(),
            massnahmen.isBereitsBehandeltGroesseGewicht(),
            massnahmen.isBereitsBehandeltSprache(),
            massnahmen.isBereitsBehandeltMotorik(),
            massnahmen.isBereitsBehandeltAnderes(),
            massnahmen.getBereitsBehandeltAnderesBemerkung(),
            massnahmen.getAbgegebenesInfomaterial().stream().map(InfomaterialMapper::toJson).toList(),
            massnahmen.getInformationEltern(),
            massnahmen.isUeberweisungHausarzt(),
            massnahmen.isUeberweisungAugenarzt(),
            AnlassFlagsMapper.toJson(massnahmen.getNachkontrolle()),
            massnahmen.getBemerkungen(),
            massnahmen.getVersion()
        ));
    }

    @NonNull
    public UntersuchungMassnahmen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungMassnahmen massnahmen,
        @NonNull UntersuchungMassnahmenSaveJson saveJson
    ) {
        if (massnahmen == null) {
            massnahmen = UntersuchungMassnahmen.forUntersuchung(untersuchung);
        }
        massnahmen.setKeineMassnahmen(saveJson.isKeineMassnahmen());
        massnahmen.setBeratungMPA(saveJson.isBeratungMPA());
        massnahmen.setBeratungSchulAerztin(saveJson.isBeratungSchulAerztin());
        massnahmen.setAnderes(saveJson.isAnderes());
        massnahmen.setAnderesBemerkung(saveJson.isAnderes()
            ? saveJson.getAnderesBemerkung()
            : null);
        massnahmen.setBereitsBehandeltSehen(saveJson.isBereitsBehandeltSehen());
        massnahmen.setBereitsBehandeltHoeren(saveJson.isBereitsBehandeltHoeren());
        massnahmen.setBereitsBehandeltGroesseGewicht(saveJson.isBereitsBehandeltGroesseGewicht());
        massnahmen.setBereitsBehandeltSprache(saveJson.isBereitsBehandeltSprache());
        massnahmen.setBereitsBehandeltMotorik(saveJson.isBereitsBehandeltMotorik());
        massnahmen.setBereitsBehandeltAnderes(saveJson.isBereitsBehandeltAnderes());
        massnahmen.setBereitsBehandeltAnderesBemerkung(saveJson.isBereitsBehandeltAnderes()
            ? saveJson.getBereitsBehandeltAnderesBemerkung()
            : null);

        Set<Infomaterial> desiredInfomaterial = infomaterialMapper.toEntityList(saveJson.getAbgegebenesInfomaterial());
        InfomaterialMapper.requireNoInactivInfomaterialAddedOrRemoved(
            massnahmen.getAbgegebenesInfomaterial(),
            desiredInfomaterial);
        massnahmen.getAbgegebenesInfomaterial().clear();
        massnahmen.getAbgegebenesInfomaterial().addAll(desiredInfomaterial);

        massnahmen.setInformationEltern(saveJson.getInformationEltern());
        massnahmen.setUeberweisungHausarzt(saveJson.isUeberweisungHausarzt());
        massnahmen.setUeberweisungAugenarzt(saveJson.isUeberweisungAugenarzt());
        massnahmen.setNachkontrolle(AnlassFlagsMapper.toEntity(saveJson.getNachkontrolle()));
        massnahmen.setBemerkungen(saveJson.getBemerkungen());

        return massnahmen;
    }

    @Nullable
    public UntersuchungMassnahmenJson getByEinladung(
        @NonNull ID<Einladung> einladungId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungMassnahmen());
    }

    @NonNull
    public UntersuchungMassnahmenJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungMassnahmenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId.toEinladungId());
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungMassnahmen massnahmen = untersuchung.getUntersuchungMassnahmen();
        UntersuchungMassnahmen entity = toEntity(untersuchung, massnahmen, saveJson);
        UntersuchungMassnahmen persistedEntity = untersuchungMassnahmenService.persistWithOptimisticLocking(
            entity,
            saveJson.getVersion(),
            wrappedId.isAnonymerEintrag()
        );
        return toJson(persistedEntity);
    }
}
