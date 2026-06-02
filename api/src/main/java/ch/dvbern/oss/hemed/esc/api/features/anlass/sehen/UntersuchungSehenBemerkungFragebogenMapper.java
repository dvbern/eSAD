package ch.dvbern.oss.hemed.esc.api.features.anlass.sehen;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
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
        @Nullable UntersuchungSehenBemerkungFragebogen sehenBemerkungFragebogen
    ) {
        if (sehenBemerkungFragebogen == null) {
            return null;
        }
        return new UntersuchungSehenBemerkungFragebogenJson(
            sehenBemerkungFragebogen.getId(),
            EntityInfoJson.fromEntity(sehenBemerkungFragebogen),
            toSaveJson(sehenBemerkungFragebogen)
        );
    }

    @NonNull
    public static UntersuchungSehenBemerkungFragebogenSaveJson toSaveJson(
        @NonNull UntersuchungSehenBemerkungFragebogen sehenBemerkungFragebogen
    ) {
        return new UntersuchungSehenBemerkungFragebogenSaveJson(
            sehenBemerkungFragebogen.getBemerkungenFragebogen(),
            sehenBemerkungFragebogen.getVersion()
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
    public UntersuchungSehenBemerkungFragebogenJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungSehenBemerkungFragebogen());
    }

    @NonNull
    public UntersuchungSehenBemerkungFragebogenJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungSehenBemerkungFragebogenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungSehenBemerkungFragebogen sehen = untersuchung.getUntersuchungSehenBemerkungFragebogen();
        UntersuchungSehenBemerkungFragebogen entity = toEntity(untersuchung, sehen, saveJson);
        return toJson(untersuchungSehenBemerkungFragebogenService.persistWithOptimisticLocking(
            entity,
            saveJson.getVersion()));
    }
}
