package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.InfomaterialMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSehenMassnahmenMapper {

    private final UntersuchungService untersuchungService;
    private final InfomaterialMapper infomaterialMapper;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;

    @Nullable
    public static UntersuchungSehenMassnahmenJson toJson(@Nullable UntersuchungMassnahmen massnahmen) {
        if (massnahmen == null) {
            return null;
        }
        return new UntersuchungSehenMassnahmenJson(
            massnahmen.getId(),
            EntityInfoJson.fromEntity(massnahmen),
            new UntersuchungSehenMassnahmenSaveJson(
                massnahmen.isBereitsBehandeltSehen(),
                massnahmen.getAbgegebenesInfomaterial().stream().map(InfomaterialMapper::toJson).toList(),
                massnahmen.getInformationEltern(),
                massnahmen.isUeberweisungAugenarzt(),
                massnahmen.getNachkontrolle().isNachkontrolleSehen(),
                massnahmen.getVersion()
            )
        );
    }

    @NonNull
    public UntersuchungMassnahmen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungMassnahmen massnahmen,
        @NonNull UntersuchungSehenMassnahmenSaveJson saveJson
    ) {
        if (massnahmen == null) {
            massnahmen = UntersuchungMassnahmen.forUntersuchung(untersuchung);
        }
        massnahmen.setBereitsBehandeltSehen(saveJson.isBereitsBehandeltSehen());

        Set<Infomaterial> desiredInfomaterial = infomaterialMapper.toEntityList(saveJson.getAbgegebenesInfomaterial());
        InfomaterialMapper.requireNoInactivInfomaterialAddedOrRemoved(
            massnahmen.getAbgegebenesInfomaterial(),
            desiredInfomaterial);
        massnahmen.getAbgegebenesInfomaterial().clear();
        massnahmen.getAbgegebenesInfomaterial().addAll(desiredInfomaterial);

        massnahmen.setInformationEltern(saveJson.getInformationEltern());
        massnahmen.setUeberweisungAugenarzt(saveJson.isUeberweisungAugenarzt());
        massnahmen.getNachkontrolle().setNachkontrolleSehen(saveJson.isNachkontrolleSehen());
        return massnahmen;
    }

    @Nullable
    public UntersuchungSehenMassnahmenJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungMassnahmen());
    }

    @NonNull
    public UntersuchungSehenMassnahmenJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungSehenMassnahmenSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungMassnahmen massnahmen = untersuchung.getUntersuchungMassnahmen();
        UntersuchungMassnahmen entity = toEntity(untersuchung, massnahmen, saveJson);
        UntersuchungMassnahmen persistedEntity = untersuchungMassnahmenService.persistWithOptimisticLocking(entity, saveJson.getVersion());
        return toJson(persistedEntity);
    }
}
