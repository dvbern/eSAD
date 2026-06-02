package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json.UntersuchungBewegungsapparatSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungBewegungsapparatMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungBewegungsapparatService untersuchungBewegungsapparatService;

    @Nullable
    public static UntersuchungBewegungsapparatJson toJson(@Nullable UntersuchungBewegungsapparat bewegungsapparat) {
        if (bewegungsapparat == null) {
            return null;
        }
        return new UntersuchungBewegungsapparatJson(
            bewegungsapparat.getId(),
            EntityInfoJson.fromEntity(bewegungsapparat),
            toSaveJson(bewegungsapparat)
        );
    }

    @NonNull
    public static UntersuchungBewegungsapparatSaveJson toSaveJson(
        @NonNull UntersuchungBewegungsapparat bewegungsapparat
    ) {
        return new UntersuchungBewegungsapparatSaveJson(
            SingleValueEnhancedMapper.toJson(Skoliose.class, bewegungsapparat.getSkoliose()),
            SingleValueEnhancedMapper.toJson(Haltungsschwaeche.class, bewegungsapparat.getHaltungsschwaeche()),
            SingleValueEnhancedMapper.toJson(
                VerkuerzungHamstringmuskulatur.class,
                bewegungsapparat.getVerkuerzungHamstringmuskulatur()),
            bewegungsapparat.getBemerkungen(),
            bewegungsapparat.getVersion()
        );
    }

    @NonNull
    private UntersuchungBewegungsapparat toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungBewegungsapparat bewegungsapparat,
        @NonNull UntersuchungBewegungsapparatSaveJson saveJson
    ) {
        if (bewegungsapparat == null) {
            bewegungsapparat = UntersuchungBewegungsapparat.forUntersuchung(untersuchung);
        }
        bewegungsapparat.setSkoliose(SingleValueEnhancedMapper.toEntity(Skoliose.class, saveJson.getSkoliose()));
        bewegungsapparat.setHaltungsschwaeche(SingleValueEnhancedMapper.toEntity(
            Haltungsschwaeche.class,
            saveJson.getHaltungsschwaeche()));
        bewegungsapparat.setVerkuerzungHamstringmuskulatur(SingleValueEnhancedMapper.toEntity(
            VerkuerzungHamstringmuskulatur.class,
            saveJson.getVerkuerzungHamstringmuskulatur()));
        bewegungsapparat.setBemerkungen(saveJson.getBemerkungen());

        return bewegungsapparat;
    }

    @Nullable
    public UntersuchungBewegungsapparatJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungBewegungsapparat());
    }

    @NonNull
    public UntersuchungBewegungsapparatJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungBewegungsapparatSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungBewegungsapparat bewegungsapparat = untersuchung.getUntersuchungBewegungsapparat();
        UntersuchungBewegungsapparat entity = toEntity(untersuchung, bewegungsapparat, saveJson);
        return toJson(untersuchungBewegungsapparatService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
