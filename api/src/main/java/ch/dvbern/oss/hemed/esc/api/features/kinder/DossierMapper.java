package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierMedizinischSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierNichtMedizinischSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.util.KindValidationUtil;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;

@JaxMapper
@RequiredArgsConstructor
public final class DossierMapper {

    private final DossierService dossierService;
    private final BenutzerService benutzerService;
    private final NachkontrolleService nachkontrolleService;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AppClock appClock;

    @NonNull
    public static DossierJson toJson(
        @NonNull Dossier dossier,
        @Nullable Nachkontrolle nachkontrolle,
        @NonNull LocalDate now,
        boolean canReadMedizinischeDaten
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
                BenutzerMapper.toOptionJson(zustaendigArzt),
                BenutzerMapper.toOptionJson(zustaendigMPA),
                dossier.getPapierakteStatus(),
                dossier.getVersion()
            ),
            KindMapper.toJson(dossier.getKind(), now)
        );
    }

    @NonNull
    private Dossier toEntity(
        @NonNull Dossier existingDossier,
        @Nullable DossierMedizinischSaveJson saveJsonMedizinisch,
        @Nullable DossierNichtMedizinischSaveJson saveJsonNichtMedizinisch) {
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
        } else if (saveJsonNichtMedizinisch != null) {
            existingDossier.setZustaendigArzt(
                benutzerService.getBenutzerIfCorrectRole(
                    saveJsonNichtMedizinisch.getZustaendigArzt(),
                    FachRolle.SCHULAERZTIN)
            );
            existingDossier.setZustaendigMPA(
                benutzerService.getBenutzerIfCorrectRole(saveJsonNichtMedizinisch.getZustaendigMPA(), FachRolle.MPA)
            );
            existingDossier.setPapierakteStatus(saveJsonNichtMedizinisch.getPapierakteStatus());
        }
        return existingDossier;
    }

    @NonNull
    public DossierJson getOrCreateByKind(@NonNull ID<Kind> kindID) {
        Dossier dossier = dossierService.getOrCreateByKind(kindID);
        aktivitaetHistoryService.createAktivitaetKind(dossier.getKind());
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(dossier.getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return DossierMapper.toJson(
            dossier,
            nachkontrolle,
            now,
            benutzerService.hasCallerBerechtigung(Berechtigung.KINDER_LESEN_MEDIZINISCH));
    }

    @NonNull
    public DossierJson updateMedizinisch(
        @NonNull ID<Dossier> dossierID,
        @NonNull DossierMedizinischSaveJson dossierJson) {
        Dossier existingDossier = dossierService.getById(dossierID);
        KindValidationUtil.throwErrorIfKindIsInactive(
            existingDossier.getKind(),
            AppValidationMessage.cannotEditEntityForInactiveKind(
                existingDossier.getKind(),
                existingDossier
            ));
        Dossier entity = toEntity(existingDossier, dossierJson, null);
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
            benutzerService.hasCallerBerechtigung(Berechtigung.KINDER_LESEN_MEDIZINISCH));
    }

    @NonNull
    public DossierJson updateNichtMedizinisch(
        @NonNull ID<Dossier> dossierID,
        @NonNull DossierNichtMedizinischSaveJson dossierJson) {
        Dossier existingDossier = dossierService.getById(dossierID);
        KindValidationUtil.throwErrorIfKindIsInactive(
            existingDossier.getKind(),
            AppValidationMessage.cannotEditEntityForInactiveKind(
                existingDossier.getKind(),
                existingDossier
            ));
        Dossier entity = toEntity(existingDossier, null, dossierJson);
        Dossier persistedDossier = dossierService.persistWithOptimisticLocking(entity, dossierJson.getVersion());
        persistedDossier.disableOptimisticLockingCheck();
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(
            persistedDossier,
            null,
            now,
            benutzerService.hasCallerBerechtigung(Berechtigung.KINDER_LESEN_MEDIZINISCH));
    }

    public void remove(@NonNull ID<Dossier> dossierID) {
        dossierService.remove(dossierID);
    }
}
