package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusKrankheitSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusListSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungImpfstatusKrankheitMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final EinladungService einladungService;
    private final AppClock appClock;

    @Nullable
    public static UntersuchungImpfstatusKrankheitSaveJson toJson(
        @Nullable UntersuchungImpfstatusKrankheit impfstatusKrankheit) {
        if (impfstatusKrankheit == null) {
            return null;
        }
        return new UntersuchungImpfstatusKrankheitSaveJson(
            impfstatusKrankheit.getKrankheit(),
            impfstatusKrankheit.getAnzahlImpfungen(),
            impfstatusKrankheit.getDatumLetzteImpfung(),
            impfstatusKrankheit.getImpfschutzGegeben(),
            impfstatusKrankheit.isImpfempfehlung(),
            impfstatusKrankheit.isImpfentscheid(),
            impfstatusKrankheit.getVersion()
        );
    }

    @NonNull
    private UntersuchungImpfstatusKrankheit toEntity(
        @NonNull Untersuchung untersuchung,
        @Nullable UntersuchungImpfstatusKrankheit impfstatusKrankheit,
        @NonNull UntersuchungImpfstatusKrankheitSaveJson saveJson) {
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        if (impfstatusKrankheit == null) {
            impfstatusKrankheit = new UntersuchungImpfstatusKrankheit(
                impfungen,
                saveJson.getKrankheit(),
                saveJson.getAnzahlImpfungen(),
                saveJson.getDatumLetzteImpfung(),
                saveJson.getImpfschutzGegeben(),
                saveJson.isImpfempfehlung(),
                saveJson.isImpfentscheid(),
                appClock.offsetDateTime().now());
        } else {
            if (!Objects.equals(impfstatusKrankheit.getAnzahlImpfungen(), saveJson.getAnzahlImpfungen())
                || !Objects.equals(impfstatusKrankheit.getDatumLetzteImpfung(), saveJson.getDatumLetzteImpfung())
                || impfstatusKrankheit.getImpfschutzGegeben() != saveJson.getImpfschutzGegeben()
                || !Objects.equals(impfstatusKrankheit.isImpfempfehlung(), saveJson.isImpfentscheid())
                || !Objects.equals(impfstatusKrankheit.isImpfentscheid(), saveJson.isImpfentscheid())
            ) {
                impfstatusKrankheit.setTimestampLastManualChange(appClock.offsetDateTime().now());
            }
            impfstatusKrankheit.setAnzahlImpfungen(saveJson.getAnzahlImpfungen());
            impfstatusKrankheit.setDatumLetzteImpfung(saveJson.getDatumLetzteImpfung());
            impfstatusKrankheit.setImpfschutzGegeben(saveJson.getImpfschutzGegeben());
            impfstatusKrankheit.setImpfempfehlung(saveJson.isImpfempfehlung());
            impfstatusKrankheit.setImpfentscheid(saveJson.isImpfentscheid());
        }
        return impfstatusKrankheit;
    }

    @NonNull
    public List<UntersuchungImpfstatusKrankheitSaveJson> getByUntersuchungImpfungen(
        @NonNull UntersuchungImpfungen impfungen
    ) {
        return getByUntersuchungImpfungen(impfungen, true);
    }

    @NonNull
    public List<UntersuchungImpfstatusKrankheitSaveJson> getByUntersuchungImpfungen(
        @NonNull UntersuchungImpfungen impfungen, boolean copyImpfhistory
    ) {
        List<UntersuchungImpfstatusKrankheit> existingImpfstatus =
            untersuchungImpfstatusKrankheitService.findByUntersuchung(impfungen.getUntersuchung().getId());
        // Eigentlich sollten die Impfstatus beim Erstellen der Untersuchung erstellt werden. Bei schon bestehenden
        // Untersuchungen erstellen wir sie hier noch.
        if (existingImpfstatus.isEmpty()) {
            if (copyImpfhistory) {
                Optional<Einladung> lastEinladung =
                    einladungService.findLastEinladungForKind(
                        impfungen.getUntersuchung().getAnlass(),
                        impfungen.getUntersuchung().getEinladungRequired().getDossier().getKind().getId());
                untersuchungService.createCompleteImpfstatusBasedOnLastAnlass(impfungen, lastEinladung);
            } else {
                untersuchungService.createCompleteImpfstatusWithoutHistory(impfungen);
            }
            existingImpfstatus =
                untersuchungImpfstatusKrankheitService.findByUntersuchung(impfungen.getUntersuchung().getId());
        }
        return existingImpfstatus.stream()
            .sorted(Comparator.comparing(UntersuchungImpfstatusKrankheit::getKrankheit))
            .map(UntersuchungImpfstatusKrankheitMapper::toJson)
            .toList();
    }

    @NonNull
    public List<UntersuchungImpfstatusKrankheitSaveJson> getByEinladung(@NonNull HasUntersuchungIDWrapper wrappedId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        return getByUntersuchungImpfungen(impfungen);
    }

    public void persistAll(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungImpfstatusListSaveJson saveJsonList,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        for (UntersuchungImpfstatusKrankheitSaveJson statusJson : saveJsonList.getImpfstatusList()) {
            Optional<UntersuchungImpfstatusKrankheit> existing =
                untersuchungImpfstatusKrankheitService.findByUntersuchungAndKrankheit(
                    impfungen.getId(),
                    statusJson.getKrankheit());
            UntersuchungImpfstatusKrankheit entity = toEntity(untersuchung, existing.orElse(null), statusJson);
            untersuchungImpfstatusKrankheitService.persistWithOptimisticLocking(
                entity,
                statusJson.getVersion(),
                !wrappedId.isAnonymerEintrag());
        }
    }
}
