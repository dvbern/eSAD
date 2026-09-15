package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungSpracheMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungSpracheService untersuchungSpracheService;

    @Nullable
    public UntersuchungSpracheJson toJson(@Nullable UntersuchungSprache sprache) {
        if (sprache == null) {
            return null;
        }
        return new UntersuchungSpracheJson(
            sprache.getId(),
            EntityInfoJson.fromEntity(sprache),
            toSaveJson(sprache)
        );
    }

    @NonNull
    public static UntersuchungSpracheSaveJson toSaveJson(@NonNull UntersuchungSprache sprache) {
        return new UntersuchungSpracheSaveJson(
            SingleValueEnhancedMapper.toJson(Nachsprechen.class, sprache.getNachsprechen()),
            SingleValueEnhancedMapper.toJson(Wortschatz.class, sprache.getWortschatz()),
            SingleValueEnhancedMapper.toJson(Sprachverstaendnis.class, sprache.getSprachverstaendnis()),
            sprache.getBemerkungen(),
            sprache.getVersion()
        );
    }

    @NonNull
    public UntersuchungSprache toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungSprache sprache,
        @NonNull UntersuchungSpracheSaveJson saveJson
    ) {
        if (sprache == null) {
            sprache = UntersuchungSprache.forUntersuchung(untersuchung);
        }
        sprache.setNachsprechen(SingleValueEnhancedMapper.toEntity(Nachsprechen.class, saveJson.getNachsprechen()));
        sprache.setWortschatz(SingleValueEnhancedMapper.toEntity(Wortschatz.class, saveJson.getWortschatz()));
        sprache.setSprachverstaendnis(SingleValueEnhancedMapper.toEntity(
            Sprachverstaendnis.class,
            saveJson.getSprachverstaendnis()));
        sprache.setBemerkungen(saveJson.getBemerkungen());
        return sprache;
    }

    @NonNull
    public UntersuchungSpracheSaveJson getDefaultUntersuchungSprache() {
        return new UntersuchungSpracheSaveJson(
            SingleValueEnhancedMapper.toJson(Nachsprechen.class, SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null)),
            SingleValueEnhancedMapper.toJson(Wortschatz.class, SingleValueEnhanced.of(Wortschatz.BESTANDEN, null)),
            SingleValueEnhancedMapper.toJson(Sprachverstaendnis.class, SingleValueEnhanced.of(Sprachverstaendnis.BESTANDEN, null)),
            null,
            0L
        );
    }

    @Nullable
    public UntersuchungSpracheJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungSprache untersuchungSprache =
            untersuchungSpracheService.getByUntersuchung(untersuchung.getId());
        return untersuchungSprache != null ? toJson(untersuchungSprache) : null;
    }

    @NonNull
    public UntersuchungSpracheJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungSpracheSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungSprache sprache = untersuchung.getUntersuchungSprache();
        UntersuchungSprache entity = toEntity(untersuchung, sprache, saveJson);
        return toJson(untersuchungSpracheService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
