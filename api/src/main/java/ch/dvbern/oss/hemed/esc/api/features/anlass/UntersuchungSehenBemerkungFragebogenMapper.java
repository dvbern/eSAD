package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenBemerkungFragebogenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenBemerkungFragebogenSaveJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSehenBemerkungFragebogenMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungSehenBemerkungFragebogenService untersuchungSehenBemerkungFragebogenService;

    @Nullable
    public static UntersuchungSehenBemerkungFragebogenJson toJson(
        @Nullable UntersuchungSehenBemerkungFragebogen sehen
    ) {
        if (sehen == null) {
            return null;
        }
        return new UntersuchungSehenBemerkungFragebogenJson(
            sehen.getId(),
            EntityInfoJson.fromEntity(sehen),
            new UntersuchungSehenBemerkungFragebogenSaveJson(
                sehen.getBemerkungenFragebogen(),
                sehen.getVersion()
            )
        );
    }

    @NonNull
    private UntersuchungSehenBemerkungFragebogen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungSehenBemerkungFragebogen sehen,
        @NonNull UntersuchungSehenBemerkungFragebogenSaveJson saveJson
    ) {
        if (sehen == null) {
            sehen = UntersuchungSehenBemerkungFragebogen.forUntersuchung(untersuchung);
        }
        sehen.setBemerkungenFragebogen(saveJson.getBemerkungenFragebogen());
        return sehen;
    }

    @Nullable
    public UntersuchungSehenBemerkungFragebogenJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungSehenBemerkungFragebogen());
    }

    @NonNull
    public UntersuchungSehenBemerkungFragebogenJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungSehenBemerkungFragebogenSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungSehenBemerkungFragebogen sehen = untersuchung.getUntersuchungSehenBemerkungFragebogen();
        UntersuchungSehenBemerkungFragebogen entity = toEntity(untersuchung, sehen, saveJson);
        return toJson(untersuchungSehenBemerkungFragebogenService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
