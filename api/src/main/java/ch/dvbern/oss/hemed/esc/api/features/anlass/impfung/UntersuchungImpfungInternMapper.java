package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffMapper;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@JaxMapper
@RequiredArgsConstructor
public final class UntersuchungImpfungInternMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungImpfungenService untersuchungImpfungenService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final BenutzerService benutzerService;
    private final AppClock appClock;
    private final TenantContext tenantContext;
    private final EinladungService einladungService;

    @Nullable
    public static UntersuchungImpfungInternJson toJson(@Nullable UntersuchungImpfungIntern impfung, @NonNull TenantEnum tenant) {
        if (impfung == null) {
            return null;
        }
        return new UntersuchungImpfungInternJson(
            impfung.getId(),
            EntityInfoJson.fromEntity(impfung),
            new UntersuchungImpfungInternSaveJson(
                ImpfstoffMapper.toJson(impfung.getImpfstoff()),
                impfung.getCharge() != null ? impfung.getCharge().getName() : null,
                impfung.getDatumImpfung(),
                impfung.getUserDurchgefuehrt() != null ?
                    BenutzerMapper.toOptionJson(impfung.getUserDurchgefuehrt(), tenant) :
                    null,
                impfung.getImpfStatus(),
                impfung.getGrundNichtDurchfuehrung(),
                impfung.getKrankheitenImmunitaeten()
                    .stream()
                    .map(UntersuchungImpfungKrankheitImmunitaetMapper::toJson)
                    .toList(),
                impfung.getVersion()
            ),
            BenutzerMapper.toOptionJson(impfung.getUserErfasst(), tenant),
            impfung.getAnlassImpfungDurchgefuehrt() != null ? impfung.getAnlassImpfungDurchgefuehrt().getId() : null
        );
    }

    @NonNull
    private UntersuchungImpfungIntern toEntity(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @Nullable UntersuchungImpfungIntern existingImpfungIntern,
        @NonNull UntersuchungImpfungInternSaveJson saveJson) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        Impfstoff impfstoff = impfstoffService.getById(saveJson.getImpfstoff().getId());
        ImpfstoffCharge charge =
            impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled(saveJson.getCharge(), impfstoff);
        Benutzer userErfasst = benutzerService.getCurrentBenutzerOrThrowException();
        Benutzer userDurchgefuehrt = saveJson.getUserDurchgefuehrt() != null ?
            benutzerService.getById(saveJson.getUserDurchgefuehrt().getId()) :
            null;
        if (existingImpfungIntern == null) {
            existingImpfungIntern = new UntersuchungImpfungIntern(
                impfungen,
                impfstoff,
                charge,
                saveJson.getDatumImpfung(),
                userErfasst,
                userDurchgefuehrt,
                saveJson.getImpfStatus(),
                saveJson.getGrundNichtDurchfuehrung(),
                new HashSet<>(),
                // Wird separat behandelt
                untersuchung.getAnlass(),
                null,
                appClock.offsetDateTime().now());
        } else {
            if (!Objects.equals(existingImpfungIntern.getImpfstoff(), impfstoff)
                || !Objects.equals(existingImpfungIntern.getCharge(), charge)
                || !Objects.equals(existingImpfungIntern.getDatumImpfung(), saveJson.getDatumImpfung())
                || existingImpfungIntern.getImpfStatus() != saveJson.getImpfStatus()
                || !Objects.equals(
                existingImpfungIntern.getGrundNichtDurchfuehrung(),
                saveJson.getGrundNichtDurchfuehrung())
            ) {
                existingImpfungIntern.setTimestampLastManualChange(appClock.offsetDateTime().now());
            }
            existingImpfungIntern.setImpfstoff(impfstoff);
            existingImpfungIntern.setCharge(charge);
            existingImpfungIntern.setDatumImpfung(saveJson.getDatumImpfung());
            existingImpfungIntern.setUserDurchgefuehrt(userDurchgefuehrt);
            if (existingImpfungIntern.getImpfStatus() == ImpfStatus.GEPLANT
                && saveJson.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT) {
                existingImpfungIntern.setAnlassImpfungDurchgefuehrt(untersuchung.getAnlass());
            }
            existingImpfungIntern.setImpfStatus(saveJson.getImpfStatus());
            existingImpfungIntern.setGrundNichtDurchfuehrung(saveJson.getGrundNichtDurchfuehrung());
            // KrankheitenImmunitaeten werden separat behandelt
        }
        return existingImpfungIntern;
    }

    @NonNull
    public List<UntersuchungImpfungInternJson> getByUntersuchungImpfungen(@NonNull UntersuchungImpfungen impfungen) {
        List<UntersuchungImpfungIntern> impfungenIntern = untersuchungImpfungInternService.findByUntersuchung(impfungen.getUntersuchung().getId());
        // Eigentlich sollten die Impfstatus beim Erstellen der Untersuchung erstellt werden. Bei schon bestehenden Untersuchungen erstellen wir sie hier noch.
        if (impfungenIntern.isEmpty()) {
            Optional<Einladung> lastEinladung =
                einladungService.findLastEinladungForKind(
                    impfungen.getUntersuchung().getAnlass(),
                    impfungen.getUntersuchung().getEinladungRequired().getDossier().getKind().getId());
            untersuchungService.createCompleteImpfungenInternBasedOnLastAnlass(impfungen, lastEinladung);
            impfungenIntern = untersuchungImpfungInternService.findByUntersuchung(impfungen.getUntersuchung().getId());
        }
        TenantEnum tenant = tenantContext.get();
        return impfungenIntern.stream().
            map(i -> UntersuchungImpfungInternMapper.toJson(i, tenant)).
            toList();
    }

    @NonNull
    public List<UntersuchungImpfungInternJson> findByEinladung(@NonNull HasUntersuchungIDWrapper wrappedId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        // Sicherstellen, dass das UntersuchungImpfungen Objekt existiert
        UntersuchungImpfungen impfungen =
            untersuchungImpfungenService.getOrCreateUntersuchungImpfungen(untersuchung.getId());
        return getByUntersuchungImpfungen(impfungen);
    }

    @NonNull
    public UntersuchungImpfungInternJson create(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull UntersuchungImpfungInternSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        return save(wrappedId, null, saveJson, hasAuthorizationUntersuchung);
    }

    @NonNull
    public UntersuchungImpfungInternJson update(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull ID<UntersuchungImpfungIntern> impfungInternId,
        @NonNull UntersuchungImpfungInternSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        UntersuchungImpfungIntern existingImpfung = untersuchungImpfungInternService.getById(impfungInternId);
        return save(wrappedId, existingImpfung, saveJson, hasAuthorizationUntersuchung);
    }

    @NonNull
    private UntersuchungImpfungInternJson save(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @Nullable UntersuchungImpfungIntern existingImpfung,
        @NonNull UntersuchungImpfungInternSaveJson saveJson,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        UntersuchungImpfungIntern entity = toEntity(wrappedId, existingImpfung, saveJson);
        replaceKrankheitImmunitaeten(entity, saveJson);
        UntersuchungImpfungIntern persisted =
            untersuchungImpfungInternService.persistWithOptimisticLocking(entity, saveJson.getVersion(), !wrappedId.isAnonymerEintrag());
        hasAuthorizationUntersuchung.accept(persisted.getUntersuchungImpfungen().getUntersuchung());
        return toJson(persisted, tenantContext.get());
    }

    private void replaceKrankheitImmunitaeten(
        @NonNull UntersuchungImpfungIntern impfung,
        @NonNull UntersuchungImpfungInternSaveJson saveJson
    ) {
        Set<UntersuchungImpfungKrankheitImmunitaet> desiredImmunitaeten = saveJson.getKrankheitenImmunitaeten()
            .stream()
            .map(untersuchungImpfungKrankheitImmunitaetJson -> UntersuchungImpfungKrankheitImmunitaetMapper.toEntity(
                impfung,
                untersuchungImpfungKrankheitImmunitaetJson))
            .collect(Collectors.toSet());
        impfung.getKrankheitenImmunitaeten().clear();
        impfung.getKrankheitenImmunitaeten().addAll(desiredImmunitaeten);
    }

    public void remove(
        @NonNull ID<Anlass> anlassId,
        @NonNull ID<UntersuchungImpfungIntern> impfungInternId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        UntersuchungImpfungIntern impfung = untersuchungImpfungInternService.getById(impfungInternId);
        hasAuthorizationUntersuchung.accept(impfung.getUntersuchungImpfungen().getUntersuchung());
        untersuchungImpfungInternService.remove(anlassId, impfung);
    }
}
