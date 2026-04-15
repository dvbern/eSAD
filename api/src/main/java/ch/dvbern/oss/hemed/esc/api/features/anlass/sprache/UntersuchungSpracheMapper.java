package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
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
            new UntersuchungSpracheSaveJson(
                SingleValueEnhancedMapper.toJson(Nachsprechen.class, sprache.getNachsprechen()),
                SingleValueEnhancedMapper.toJson(Wortschatz.class, sprache.getWortschatz()),
                SingleValueEnhancedMapper.toJson(Sprachverstaendnis.class, sprache.getSprachverstaendnis()),
                sprache.getBemerkungen(),
                sprache.getVersion()
            )
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
        sprache.setSprachverstaendnis(SingleValueEnhancedMapper.toEntity(Sprachverstaendnis.class, saveJson.getSprachverstaendnis()));
        sprache.setBemerkungen(saveJson.getBemerkungen());
        return sprache;
    }

    @Nullable
    public UntersuchungSpracheJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        UntersuchungSprache untersuchungSprache =
            untersuchungSpracheService.getByUntersuchung(untersuchung.getId());
        return untersuchungSprache != null ? toJson(untersuchungSprache) : null;
    }

    @NonNull
    public UntersuchungSpracheJson save(
        @NonNull ID<Einladung> einladungID,
        @NonNull UntersuchungSpracheSaveJson saveJson
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        UntersuchungSprache sprache = untersuchung.getUntersuchungSprache();
        UntersuchungSprache entity = toEntity(untersuchung, sprache, saveJson);
        return toJson(untersuchungSpracheService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
