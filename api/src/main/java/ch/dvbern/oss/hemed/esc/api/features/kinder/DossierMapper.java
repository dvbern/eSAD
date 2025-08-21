package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        @NonNull LocalDate now) {
        Benutzer zustaendigArzt = dossier.getZustaendigArztFromDossierOrSchule();
        Benutzer zustaendigMPA = dossier.getZustaendigMPAFromDossierOrSchule();
        return new DossierJson(
            dossier.getId(),
            EntityInfoJson.fromEntity(dossier),
            new DossierSaveJson(
                dossier.isKinderschutzfall(),
                dossier.isKeineAuskunftAnDrittpersonen(),
                dossier.isSansPapier(),
                dossier.isAndereZusatzinformation(),
                dossier.getAndereZusatzInformationText(),
                dossier.isImmunitaetMasern(),
                dossier.isImmunitaetVarizellen(),
                dossier.getPapierakteStatus(),
                BenutzerMapper.toOptionJson(zustaendigArzt),
                BenutzerMapper.toOptionJson(zustaendigMPA),
                NachkontrolleMapper.toJson(nachkontrolle)
            ),
            KindMapper.toJson(dossier.getKind(), now)
        );
    }

    @NonNull
    private Dossier toEntity(@NonNull Dossier existingDossier, @NonNull DossierSaveJson saveJson) {
        // We don't set the EdokidId in toEntity() because it can't be changed
        // Either it's generated in the according service, or it's already set on existingDossier
        existingDossier.setKinderschutzfall(saveJson.isKinderschutzfall());
        existingDossier.setKeineAuskunftAnDrittpersonen(saveJson.isKeineAuskunftAnDrittpersonen());
        existingDossier.setSansPapier(saveJson.isSansPapier());
        existingDossier.setAndereZusatzinformation(saveJson.isAndereZusatzinformation());
        existingDossier.setAndereZusatzInformationText(saveJson.getAndereZusatzInformationText());
        existingDossier.setImmunitaetMasern(saveJson.isImmunitaetMasern());
        existingDossier.setImmunitaetVarizellen(saveJson.isImmunitaetVarizellen());
        existingDossier.setPapierakteStatus(saveJson.getPapierakteStatus());
        existingDossier.setZustaendigArzt(
            benutzerService.getBenutzerIfCorrectRole(saveJson.getZustaendigArzt(), FachRolle.SCHULAERZTIN)
        );
        existingDossier.setZustaendigMPA(
            benutzerService.getBenutzerIfCorrectRole(saveJson.getZustaendigMPA(), FachRolle.MPA)
        );
        return existingDossier;
    }

    @NonNull
    public DossierJson getOrCreateByKind(@NonNull ID<Kind> kindID) {
        Dossier dossier = dossierService.getOrCreateByKind(kindID);
        aktivitaetHistoryService.createAktivitaetKind(dossier.getKind());
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(dossier.getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return DossierMapper.toJson(dossier, nachkontrolle, now);
    }

    @NonNull
    public DossierJson update(@NonNull ID<Dossier> dossierID, @NonNull DossierSaveJson dossierJson) {
        Dossier existingDossier = dossierService.getById(dossierID);
        Dossier entity = toEntity(existingDossier, dossierJson);
        Nachkontrolle nachkontrolle = nachkontrolleService.getOrCreateByDossier(existingDossier.getId());
        LocalDate now = appClock.offsetDateTime().now().toLocalDate();
        return toJson(
            dossierService.persist(entity),
            nachkontrolleService.persist(NachkontrolleMapper.toEntity(nachkontrolle, dossierJson.getNachkontrolle())),
            now);
    }

    public void remove(@NonNull ID<Dossier> dossierID) {
        dossierService.remove(dossierID);
    }

}
