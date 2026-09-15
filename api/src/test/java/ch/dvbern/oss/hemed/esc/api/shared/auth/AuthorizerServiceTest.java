package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungStatus;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArchivdokumentBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfangBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsLausbeauftragteWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsPfpWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;

import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class AuthorizerServiceTest {
    private final AuthorizerService authorizerService;
    private final FixtureService fixtureService;
    private final OrganisationService organisationService;
    private final ArchivdokumentService archivdokumentService;

    private MockedStatic<FachRolleUtil> useWinBerechtigungenForBs = null;

    @AfterEach
    void tearDown() {
        if (useWinBerechtigungenForBs != null) {
            useWinBerechtigungenForBs.close();
        }
    }

    private void useBerechtigungenWin() {
        // Da wir keine WIN Datenbank haben, testen wir die WIN-speizifischen Benutzer in der BS Datenbank
        // z.B. Laeusekontrolle. Damit die Berechtigungen trotzdem von WIN berechnet werden$
        // wird fuer Tenant
        useWinBerechtigungenForBs = Mockito.mockStatic(FachRolleUtil.class);
        useWinBerechtigungenForBs
            .when(() -> FachRolleUtil.getBerechtigungen(
                ArgumentMatchers.any(TenantEnum.class),
                ArgumentMatchers.any(FachRolle.class)))
            .thenAnswer(invocation -> {
                FachRolle argument = invocation.getArgument(1);
                return argument.getBerechtigungen(TenantEnum.WIN);
            });
    }

    @Test
    @TestAsSupportBE
    void hasReadAuthorizationOrganisation_SUPPORT_success() {
        // ARRANGE
        Organisation anyOrganisation = new Organisation();
        // ACT
        boolean result = authorizerService.hasReadAuthorizationOrganisation(anyOrganisation);
        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    @TestAsSchularztBE
    void hasReadAuthorizationOrganisation_SCHULARZT() {
        // ARRANGE
        Organisation organisationOfSchularzt = organisationService.getById(TestAsSchularztBE.ORGANISATION_ID_ID);
        Organisation anyOtherOrganisation = new Organisation();
        // ACT
        boolean allowedForMyOrganisation = authorizerService.hasReadAuthorizationOrganisation(organisationOfSchularzt);
        boolean allowedForOtherOrganisation = authorizerService.hasReadAuthorizationOrganisation(anyOtherOrganisation);
        // ASSERT
        assertThat(allowedForMyOrganisation).isTrue();
        assertThat(allowedForOtherOrganisation).isFalse();
    }

    @Test
    @TestAsSupportBS
    void checkWriteAuthorizationAnlass_anlass_offen_SUPPORT() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(
            anlass.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupportBS
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_SUPPORT() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(
            anlass.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsAdminBS
    void checkWriteAuthorization_anlass_abgeschlossen_ADMIN() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(
            anlass.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsMpaBS
    void checkWriteAuthorizationAnlass_anlass_offen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(
            anlass.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsMpaBS
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThrows(
            Exception.class,
            () -> authorizerService.requireWriteAuthorizationAnlass(
                anlass.getId(),
                List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)));
    }

    @Test
    @TestAsSupportBS
    void checkAuthorizationNotiz_Fragebogen_SUPPORT() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_FRAGEBOGEN));
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)))
            .doesNotThrowAnyException();
        assertThatCode(() -> authorizerService.requireWriteAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupportBS
    void checkAuthorizationNotiz_Dispensationsgesuch_SUPPORT() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_DISPENSATIONSGESUCH));
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)))
            .doesNotThrowAnyException();
        assertThatCode(() -> authorizerService.requireWriteAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsEmpfangBS
    void checkAuthorizationNotiz_Fragebogen_EMPFANG() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_FRAGEBOGEN));
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireReadAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)));
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationNotiz(
            notiz,
            List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)));
    }

    @Test
    @TestAsLausbeauftragteWIN
    void checkAuthorizationAnlass_Impftermin_Lausbeauftragte() {
        // ARRANGE
        useBerechtigungenWin();
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.IMPFTERMIN);
        // ACT / ASSERT
        assertThrows(
            Exception.class,
            () -> authorizerService.requireReadAuthorizationAnlass(
                anlass.getId(),
                List.of(Berechtigung.ANLASSPLANUNG_LESEN)));
    }

    @Test
    @TestAsLausbeauftragteWIN
    void checkAuthorizationAnlass_Lauskontrolle_Lausbeauftragte() {
        // ARRANGE
        useBerechtigungenWin();
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.LAEUSEKONTROLLE);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationAnlass(
            anlass.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE)))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsPfpWIN
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_PFP() {
        // ARRANGE
        useBerechtigungenWin();
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThrows(
            Exception.class,
            () -> authorizerService.requireWriteAuthorizationAnlass(
                anlass.getId(),
                List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN)));
    }

    @Test
    @TestAsSupportBE
    void hasReadAuthorizationAufgabe_SUPPORT_success() {
        // ARRANGE
        Benutzer empfaenger = fixtureService.createBenutzerWithOrganisation();
        Aufgabe aufgabe = fixtureService.createAufgabeForBenutzer(empfaenger);

        // ACT
        boolean result = authorizerService.hasReadAuthorizationAufgabe(aufgabe, List.of(Berechtigung.AUFGABEN_LESEN));

        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    @TestAsSupportBS
    void hasReadAuthorizationAufgabe_SUPPORT_BS_success() {
        // ARRANGE
        Benutzer empfaenger = fixtureService.createBenutzerFuerZentraleOrganisation();
        Aufgabe aufgabe = fixtureService.createAufgabeForBenutzer(empfaenger);

        // ACT
        boolean result = authorizerService.hasReadAuthorizationAufgabe(aufgabe, List.of(Berechtigung.AUFGABEN_LESEN));

        // ASSERT
        assertThat(result).isTrue();
    }

    @Test
    @TestAsSchularztBE
    void hasReadAuthorizationAufgabe_SCHULARZT() {
        // ARRANGE
        Benutzer currentBenutzer = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Aufgabe aufgabeInMyOrg = fixtureService.createAufgabeForBenutzer(currentBenutzer);

        Benutzer empfaengerInOtherOrg = fixtureService.createBenutzerWithOrganisation();
        Aufgabe aufgabeInOtherOrg = fixtureService.createAufgabeForBenutzer(empfaengerInOtherOrg);

        // ACT
        boolean allowedForMyOrg = authorizerService.hasReadAuthorizationAufgabe(aufgabeInMyOrg, List.of(Berechtigung.AUFGABEN_LESEN));
        boolean allowedForOtherOrg = authorizerService.hasReadAuthorizationAufgabe(aufgabeInOtherOrg, List.of(Berechtigung.AUFGABEN_LESEN));

        // ASSERT
        assertThat(allowedForMyOrg).isTrue();
        assertThat(allowedForOtherOrg).isFalse();
    }

    @Test
    @TestAsSupportBS
    void requireArchivBulkDownloadAuthorization_ownDokument_SUPPORT_success() {
        // ARRANGE: current user (SUPPORT, has ARCHIVIERUNG) is the owner of the Archivdokument
        Benutzer currentBenutzer = fixtureService.readBenutzerWithBerechtigungen(
            ID.of(UUID.fromString(TestAsSupportBS.BENUTZER_ID), Benutzer.class));
        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireArchivBulkDownloadAuthorization(archivdokument.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupportBS
    void requireArchivBulkDownloadAuthorization_foreignDokument_SUPPORT_throws() {
        // ARRANGE: the Archivdokument belongs to another user, even though the current user is SUPPORT.
        // Unlike the single download (requireArchivDownloadAuthorization), SUPPORT may not bulk download
        // Archivdokumente owned by somebody else.
        Benutzer otherBenutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(otherBenutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        // ACT / ASSERT
        var id = archivdokument.getId();
        assertThrows(
            AppFailureException.class,
            () -> authorizerService.requireArchivBulkDownloadAuthorization(id));
    }
}
