package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierMedizinischSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierNichtMedizinischSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public final class DossierMapper {

    private final DossierService dossierService;
    private final NachkontrolleService nachkontrolleService;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AppClock appClock;
    private final ZustaendigePersonService zustaendigePersonService;
    private final TenantContext tenantContext;

    @NonNull
    public static DossierJson toJson(
        @NonNull Dossier dossier,
        @Nullable Nachkontrolle nachkontrolle,
        @NonNull LocalDate now,
        boolean canReadMedizinischeDaten,
        @NonNull TenantEnum tenant
    ) {
        Benutzer zustaendigArzt = dossier.getZustaendigArztFromDossierOrSchule();
        Benutzer zustaendigMPA = dossier.getZustaendigMPAFromDossierOrSchule();
        return new DossierJson(
            dossier.getId(),
            EntityInfoJson.fromEntity(dossier),
            canReadMedizinischeDaten ? new DossierMedizinischSaveJson(
                dossier.isKinderschutzfall(),
                dossier.isKeineAuskunftAnDrittpersonen(),
                dossier.isSansPapier(),
                dossier.isAndereZusatzinformation(),
                dossier.getAndereZusatzInformationText(),
                dossier.isImmunitaetMasern(),
                dossier.isImmunitaetVarizellen(),
                NachkontrolleMapper.toJson(nachkontrolle),
                dossier.getVersion()
            ) : null,
            new DossierNichtMedizinischSaveJson(
                BenutzerMapper.toOptionJson(zustaendigArzt, tenant),
                BenutzerMapper.toOptionJson(zustaendigMPA, tenant),
                dossier.getPapierakteStatus(),
                dossier.getVersion()
            ),
            KindMapper.toJson(dossier.getKind(), now),
            dossier.getTimestampDefinitivArchiviert()
        );
    }

    @NonNull
    private Dossier toEntity(
        @NonNull Dossier existingDossier,
        @Nullable DossierMedizinischSaveJson saveJsonMedizinisch
    ) {
        // We don't set the EdokidId in toEntity() because it can't be changed
        // Either it's generated in the according service, or it's already set on existingDossier
        if (saveJsonMedizinisch != null) {
            existingDossier.setKinderschutzfall(saveJsonMedizinisch.isKinderschutzfall());
            existingDossier.setKeineAuskunftAnDrittpersonen(saveJsonMedizinisch.isKeineAuskunftAnDrittpersonen());
            existingDossier.setSansPapier(saveJsonMedizinisch.isSansPapier());
            existingDossier.setAndereZusatzinformation(saveJsonMedizinisch.isAndereZusatzinformation());
            existingDossier.setAndereZusatzInformationText(saveJsonMedizinisch.getAndereZusatzInformationText());
            existingDossier.setImmunitaetMasern(saveJsonMedizinisch.isImmunitaetMasern());
            existingDossier.setImmunitaetVarizellen(saveJsonMedizinisch.isImmunitaetVarizellen());
        }
        return existingDossier;
    }

    @NonNull
    public DossierJson getOrCreateByKind(
        @NonNull ID<Kind> kindID,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        Dossier dossier = dossierService.getOrCreateByKind(kindID);
        aktivitaetHistoryService.createAktivitaetKind(dossier.getKind());
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(dossier.getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return DossierMapper.toJson(
            dossier,
            nachkontrolle,
            now,
            canReadMedizinischeDaten.test(dossier),
            tenantContext.get());
    }

    @NonNull
    public DossierJson updateMedizinisch(
        @NonNull ID<Dossier> dossierID,
        @NonNull DossierMedizinischSaveJson dossierJson,
        @NonNull Consumer<Dossier> hasWriteAuthorization,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        Dossier existingDossier = dossierService.getByIdEnsureActive(dossierID);
        hasWriteAuthorization.accept(existingDossier);
        Dossier entity = toEntity(existingDossier, dossierJson);
        Dossier persistedDossier = dossierService.persistWithOptimisticLocking(entity, dossierJson.getVersion());
        persistedDossier.disableOptimisticLockingCheck();
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(existingDossier.getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        Nachkontrolle persistedNachkontrolle = nachkontrolleService.persistWithOptimisticLocking(
            NachkontrolleMapper.toEntity(nachkontrolle, dossierJson.getNachkontrolle()),
            dossierJson.getNachkontrolle().getData().getVersion()
        );
        return toJson(
            persistedDossier,
            persistedNachkontrolle,
            now,
            canReadMedizinischeDaten.test(persistedDossier),
            tenantContext.get());
    }

    @NonNull
    public DossierJson updateDossierVerantwortlicherArzt(
        @NonNull ID<Dossier> dossierID,
        @NonNull ID<Benutzer> arztId,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        Dossier existingDossier = dossierService.getByIdEnsureActive(dossierID);
        return updateDossierVerantwortlicherArztUndMpa(
            dossierID,
            arztId,
            existingDossier.getZustaendigMPA() != null ? existingDossier.getZustaendigMPA().getId() : null,
            canReadMedizinischeDaten);
    }

    @NonNull
    public DossierJson updateDossierVerantwortlicheMpa(
        @NonNull ID<Dossier> dossierID,
        @NonNull ID<Benutzer> mpaId,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        Dossier existingDossier = dossierService.getByIdEnsureActive(dossierID);
        return updateDossierVerantwortlicherArztUndMpa(
            dossierID,
            existingDossier.getZustaendigArzt() != null ? existingDossier.getZustaendigArzt().getId() : null,
            mpaId,
            canReadMedizinischeDaten);
    }

    @NonNull
    public DossierJson updateDossierVerantwortlicherArztUndMpa(
        @NonNull ID<Dossier> dossierID,
        @Nullable ID<Benutzer> arztId,
        @Nullable ID<Benutzer> mpaId,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        // Bewusst kein getByIdEnsureActive: Wird das Kind im selben Request (KindResource PUT /kinder/{id}) auf
        // INAKTIV gesetzt, ist der Status hier bereits INAKTIV. Die Aufrufer ausserhalb von KindResource pruefen den
        // aktiven Status selbst, bevor sie diese Methode aufrufen.
        Dossier existingDossier = dossierService.getById(dossierID);
        boolean zustaendigkeitChanged = zustaendigePersonService.hasZustaendigkeitChanged(existingDossier, arztId);
        // Die MPA wird wieder dieselbe gesetzt, ausser beim Arzt ist eine neue Organisation zustaendig.
        ID<Benutzer> mpaToSet = mpaId;
        if (zustaendigkeitChanged) {
            // Zustaendigkeit darf nur vom aktuell Zuständigen geändert werden
            zustaendigePersonService.requireImmediateZustaendigkeit(existingDossier);
            // Falls die Zustaendigkeit geändert hat, muss die MPA null gesetzt werden
            mpaToSet = null;
        }

        // https://intra.dvbern.ch/spaces/HEM/pages/284263663/Zust%C3%A4ndigkeiten+Archivierung
        // Change Zustaendigkeiten: Fall 3: Neue Verantwortliche auf Kind (manuell)
        Dossier savedDossier = zustaendigePersonService.verantwortlicherChangedOnDossier(
            existingDossier,
            arztId,
            mpaToSet);

        return getDossierJson(savedDossier, canReadMedizinischeDaten);
    }

    @NonNull
    public DossierJson updateDossierPapierakteStatus(
        @NonNull ID<Dossier> dossierID,
        @NonNull PapierakteStatus status,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        Dossier existingDossier = dossierService.getByIdEnsureActive(dossierID);
        existingDossier.disableOptimisticLockingCheck();
        existingDossier.setPapierakteStatus(status);
        Dossier persistedDossier = dossierService.persist(existingDossier);
        return getDossierJson(persistedDossier, canReadMedizinischeDaten);
    }

    public void remove(
        @NonNull ID<Dossier> dossierID,
        @NonNull Consumer<Dossier> hasDossierAuthorization
    ) {
        Dossier existingDossier = dossierService.getById(dossierID);
        hasDossierAuthorization.accept(existingDossier);
        dossierService.remove(dossierID);
    }

    @NonNull
    private DossierJson getDossierJson(
        @NonNull Dossier existingDossier,
        @NonNull Predicate<Dossier> canReadMedizinischeDaten
    ) {
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(
            existingDossier,
            null,
            now,
            canReadMedizinischeDaten.test(existingDossier),
            tenantContext.get());
    }
}
