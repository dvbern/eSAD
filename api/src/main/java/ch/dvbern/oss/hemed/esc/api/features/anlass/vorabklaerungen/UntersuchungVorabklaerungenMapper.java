package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhancedMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungVorabklaerungenMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungVorabklaerungenService untersuchungVorabklaerungenService;

    @Nullable
    public static UntersuchungVorabklaerungenJson toJson(@Nullable UntersuchungVorabklaerungen vorabklaerungen) {
        if (vorabklaerungen == null) {
            return null;
        }
        return new UntersuchungVorabklaerungenJson(
            vorabklaerungen.getId(),
            EntityInfoJson.fromEntity(vorabklaerungen),
            toSaveJson(vorabklaerungen)
        );
    }

    @NonNull
    public static UntersuchungVorabklaerungenSaveJson toSaveJson(@NonNull UntersuchungVorabklaerungen vorabklaerungen) {
        return new UntersuchungVorabklaerungenSaveJson(
            SingleValueEnhancedMapper.toJson(FragebogenStatus.class, vorabklaerungen.getFragebogenStatus()),
            SingleValueEnhancedMapper.toJson(
                EinverstaendnisImpfungen.class,
                vorabklaerungen.getEinverstaendnisImpfungen()),
            SingleValueEnhancedMapper.toJson(
                ChronischeKrankheiten.class,
                vorabklaerungen.getChronischeKrankheiten()),
            vorabklaerungen.getVersion()
        );
    }

    @NonNull
    public static UntersuchungVorabklaerungen toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungVorabklaerungen vorabklaerungen,
        @NonNull UntersuchungVorabklaerungenSaveJson saveJson
    ) {
        if (vorabklaerungen == null) {
            vorabklaerungen = UntersuchungVorabklaerungen.forUntersuchung(untersuchung);
        }

        vorabklaerungen.setFragebogenStatus(SingleValueEnhancedMapper.toEntity(
            FragebogenStatus.class,
            saveJson.getFragebogenStatus()));
        vorabklaerungen.setEinverstaendnisImpfungen(SingleValueEnhancedMapper.toEntity(
            EinverstaendnisImpfungen.class,
            saveJson.getEinverstaendnisImpfungen()));
        vorabklaerungen.setChronischeKrankheiten(SingleValueEnhancedMapper.toEntity(
            ChronischeKrankheiten.class,
            saveJson.getChronischeKrankheiten()));

        return vorabklaerungen;
    }

    @Nullable
    public UntersuchungVorabklaerungenJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedID,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedID);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungVorabklaerungen());
    }

    @NonNull
    public UntersuchungVorabklaerungenJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungVorabklaerungenSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungVorabklaerungen vorabklaerungen = untersuchung.getUntersuchungVorabklaerungen();
        UntersuchungVorabklaerungen entity = toEntity(untersuchung, vorabklaerungen, saveJson);
        return toJson(untersuchungVorabklaerungenService.persistWithOptimisticLocking(entity, saveJson.getVersion()));
    }
}
