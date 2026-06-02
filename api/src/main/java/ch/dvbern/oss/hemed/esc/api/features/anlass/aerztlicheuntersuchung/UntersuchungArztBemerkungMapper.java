package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungArztBemerkungMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungArztBemerkungService untersuchungArztBemerkungService;

    @Nullable
    public static UntersuchungArztBemerkungJson toJson(@Nullable UntersuchungArztBemerkung arztBemerkung) {
        if (arztBemerkung == null) {
            return null;
        }
        return new UntersuchungArztBemerkungJson(
            arztBemerkung.getId(),
            EntityInfoJson.fromEntity(arztBemerkung),
            toSaveJson(arztBemerkung)
        );
    }

    @NonNull
    public static UntersuchungArztBemerkungSaveJson toSaveJson(@NonNull UntersuchungArztBemerkung arztBemerkung) {
        return new UntersuchungArztBemerkungSaveJson(
            arztBemerkung.getBemerkungen(),
            arztBemerkung.getVersion()
        );
    }

    @NonNull
    public static UntersuchungArztBemerkung toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungArztBemerkung untersuchungArztBemerkung,
        @NonNull UntersuchungArztBemerkungSaveJson saveJson
    ) {
        if (untersuchungArztBemerkung == null) {
            untersuchungArztBemerkung = UntersuchungArztBemerkung.forUntersuchung(untersuchung);
        }
        untersuchungArztBemerkung.setBemerkungen(saveJson.getBemerkungen());
        return untersuchungArztBemerkung;
    }

    @Nullable
    public UntersuchungArztBemerkungJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungArztBemerkung());
    }

    @NonNull
    public UntersuchungArztBemerkungJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungArztBemerkungSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungArztBemerkung arztBem = untersuchung.getUntersuchungArztBemerkung();
        UntersuchungArztBemerkung entity = toEntity(untersuchung, arztBem, saveJson);
        return toJson(untersuchungArztBemerkungService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
