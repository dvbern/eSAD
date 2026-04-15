package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArchivdokumentBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("ResultOfMethodCallIgnored")
@QuarkusTest
@RequiredArgsConstructor
class ArchivdokumentServiceITest extends AbstractITest {

    private final ArchivdokumentRepo archivdokumentRepo;
    private final ArchivdokumentService archivdokumentService;
    private final BenutzerService benutzerService;
    private final KindService kindService;
    private final DossierService dossierService;
    private final FixtureService fixtureService;
    private final AppClock appClock;

    @Test
    @TestAsAdminBS
    void testFindKinderAelterAlsUndNichtArchiviert() {
        // ARRANGE
        int minAge = 20;
        LocalDate today = appClock.getToday();
        LocalDate olderThanMinAge = today.minusYears(minAge).minusDays(1);
        LocalDate youngerThanMinAge = today.minusYears(minAge);

        Benutzer owner = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(owner);

        // 1. Kind older than minAge, no Archivdokument -> Should be returned
        Kind kind1 = KindBuilder.create(b -> b
            .withName("Older", "NoDoc")
            .withGeburtsdatum(olderThanMinAge)
            .withKindAktivStatus(EntityAktivStatus.AKTIV));
        kindService.persistWithoutDuplicateCheck(kind1);
        Dossier dossier1 = Dossier.forKind(kind1);
        dossierService.persist(dossier1);

        // 2. Kind older than minAge, has Archivdokument with type OTHER -> Should be returned
        Kind kind2 = KindBuilder.create(b -> b
            .withName("Older", "OtherDoc")
            .withGeburtsdatum(olderThanMinAge)
            .withKindAktivStatus(EntityAktivStatus.AKTIV));
        kindService.persistWithoutDuplicateCheck(kind2);
        Dossier dossier2 = DossierBuilder.create(d -> d
            .forKind(kind2)
            .withTimestampDefinitivArchiviert(null));
        dossierService.persist(dossier2);

        // 3. Kind older than minAge, has Archivdokument with type KIND_DEAKTIVIERT -> Should NOT be returned
        Kind kind3 = KindBuilder.create(b -> b.withName("Older", "DeaktDoc")
            .withGeburtsdatum(olderThanMinAge)
            .withKindAktivStatus(EntityAktivStatus.AKTIV));
        kindService.persistWithoutDuplicateCheck(kind3);
        Dossier dossier3 = DossierBuilder.create(d -> d
            .forKind(kind3)
            .withTimestampDefinitivArchiviert(LocalDateTime.now()));
        dossierService.persist(dossier3);

        // 4. Kind younger than minAge -> Should NOT be returned
        Kind kind4 = KindBuilder.create(b -> b.withName("Younger", "NoDoc")
            .withGeburtsdatum(youngerThanMinAge)
            .withKindAktivStatus(EntityAktivStatus.AKTIV));
        kindService.persistWithoutDuplicateCheck(kind4);
        Dossier dossier4 = Dossier.forKind(kind4);
        dossierService.persist(dossier4);

        // ACT
        List<ID<Dossier>> result = archivdokumentRepo.findKinderAelterAlsUndNichtArchiviert(minAge, 100);

        // ASSERT
        assertThat(result)
            .contains(dossier1.getId(), dossier2.getId())
            .doesNotContain(dossier3.getId(), dossier4.getId());
    }

    @Test
    @TestAsSupportBS
    void findAll_shouldReturnOffeneDokumente_forSupportUser_whenOnlyActiveIsTrue() {
        // Arrange
        Archivdokument offenDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(offenDoc);

        Archivdokument downloadedDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(downloadedDoc);

        // Act
        List<Archivdokument> result = archivdokumentService.findAll(true);

        // Assert: SUPPORT user -> findAll with offene status, not filtered by benutzer
        assertThat(result)
            .anyMatch(doc -> doc.getId().equals(offenDoc.getId()))
            .noneMatch(doc -> doc.getId().equals(downloadedDoc.getId()));
    }

    @Test
    @TestAsSupportBS
    void findAll_shouldReturnAbgeschlosseneDokumente_forSupportUser_whenOnlyActiveIsFalse() {
        // Arrange
        Archivdokument offenDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(offenDoc);

        Archivdokument downloadedDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(downloadedDoc);

        // Act
        List<Archivdokument> result = archivdokumentService.findAll(false);

        // Assert: SUPPORT user + not onlyActive -> abgeschlossene status
        assertThat(result)
            .anyMatch(doc -> doc.getId().equals(downloadedDoc.getId()))
            .noneMatch(doc -> doc.getId().equals(offenDoc.getId()));
    }

    @Test
    @TestAsAdminBS
    void findAll_shouldReturnOnlyUserDokumente_forNonSupportUser_whenOnlyActiveIsTrue() {
        // Arrange
        var adminId = ID.of(UUID.fromString(TestAsAdminBS.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument ownOffenDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(ownOffenDoc);

        Archivdokument otherOffenDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(otherOffenDoc);

        // Act
        List<Archivdokument> result = archivdokumentService.findAll(true);

        // Assert: non-SUPPORT user -> findAllBenutzer filtered by user ID
        assertThat(result)
            .anyMatch(doc -> doc.getId().equals(ownOffenDoc.getId()))
            .noneMatch(doc -> doc.getId().equals(otherOffenDoc.getId()));
    }

    @Test
    @TestAsAdminBS
    void findAll_shouldReturnAbgeschlosseneUserDokumente_forNonSupportUser_whenOnlyActiveIsFalse() {
        // Arrange
        var adminId = ID.of(UUID.fromString(TestAsAdminBS.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument ownDownloadedDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(ownDownloadedDoc);

        Archivdokument otherDownloadedDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzer())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(otherDownloadedDoc);

        // Act
        List<Archivdokument> result = archivdokumentService.findAll(false);

        // Assert: non-SUPPORT user + not onlyActive -> abgeschlossene, filtered by user
        assertThat(result)
            .anyMatch(doc -> doc.getId().equals(ownDownloadedDoc.getId()))
            .noneMatch(doc -> doc.getId().equals(otherDownloadedDoc.getId()));
    }

    @Test
    @TestAsSupportBS
    void sendArchiveDocumentDownloadReminder_shouldFetchOwnersAndSendReminder() {
        // Arrange
        Benutzer owner1 = fixtureService.createBenutzer();
        Benutzer owner2 = fixtureService.createBenutzer();

        Archivdokument doc1 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(owner1)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(doc1);

        Archivdokument doc2 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(owner2)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(doc2);

        ArchivdokumentMailService mailServiceMock = mock(ArchivdokumentMailService.class);
        QuarkusMock.installMockForType(mailServiceMock, ArchivdokumentMailService.class);

        // Act
        archivdokumentService.sendArchiveDocumentDownloadReminder();

        // Assert
        verify(mailServiceMock).sendArchiveDocumentDownloadReminder(
            ArgumentMatchers.argThat(benutzerList ->
                benutzerList.stream().anyMatch(b -> b.getId().equals(owner1.getId()))
                    && benutzerList.stream().anyMatch(b -> b.getId().equals(owner2.getId()))
            )
        );
    }
}
