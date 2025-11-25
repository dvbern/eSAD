package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerTestGenerator;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.KindAnlaesseService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SuperAdminRunnerService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DuplikateApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DuplikatJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DuplikatListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DuplikatStatusSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@RequiredArgsConstructor
class DuplikatResourceITest extends AbstractITest {

    private final DuplikateApiSpec api = DuplikateApiSpec.duplikate(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizapi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final DuplikatRepo duplikatRepo;
    private final SuperAdminRunnerService superAdminRunnerService;
    private final KindService kindService;
    private final DossierService dossierService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final NotizService notizService;
    private final AufgabeService aufgabeService;
    private final KindAnlaesseService kindAnlaesseService;


    @Test
    @TestAsAdminBS
    void reloadDuplikate() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Laura", "Müller", LocalDate.of(2000, 1, 5), Geschlecht.WEIBLICH);
        Kind kind2 = fixtureService.createKind("Laura", "Mueller", LocalDate.of(2000, 1, 5), Geschlecht.WEIBLICH);
        Kind kind3 = fixtureService.createKind("Another", "Another", LocalDate.of(2000, 1, 5), Geschlecht.WEIBLICH);
        // ACT
        api.duplikatResourceReloadDuplikate()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
        // ASSERT
        assertThat(duplikatRepo.findByKindIds(kind1.getId(), kind2.getId())).isPresent();
        assertThat(duplikatRepo.findByKindIds(kind1.getId(), kind3.getId())).isEmpty();
        assertThat(duplikatRepo.findByKindIds(kind2.getId(), kind3.getId())).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void findAllDuplikate() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Miriam", "Müller", LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        Kind kind2 = fixtureService.createKind("Miriam", "Mueller", LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        String idKind1 = kind1.getId().getId();
        String idKind2 = kind2.getId().getId();
        superAdminRunnerService.runReloadDuplikateJob(true);
        // ACT
        DuplikatListJsonSpec result = api.duplikatResourceFindAllDuplikate()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DuplikatListJsonSpec.class);
        // ASSERT
        assertThat(result.getDuplikatList()).anyMatch(resultSpec -> {
                String resultIdKind1 = resultSpec.getKind1().getId();
                String resultIdKind2 = resultSpec.getKind2().getId();
                return (resultIdKind1.equals(idKind1) && resultIdKind2.equals(idKind2))
                    || (resultIdKind1.equals(idKind2) && resultIdKind2.equals(idKind1));
            }
        );
    }

    @Test
    @TestAsAdminBS
    void markAsFalsePositive() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Hannelore", "Müller", LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        Kind kind2 = fixtureService.createKind("Hannelore", "Mueller", LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        superAdminRunnerService.runReloadDuplikateJob(true);
        Optional<Duplikat> duplikatOptional = duplikatRepo.findByKindIds(kind1.getId(), kind2.getId());
        assertTrue(duplikatOptional.isPresent());
        // ACT
        DuplikatJsonSpec result = api.duplikatResourceMarkAsFalsePositive()
            .duplikatIdPath(duplikatOptional.get().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DuplikatJsonSpec.class);
        // ASSERT
        assertNotNull(result);
        assertThat(result.getDuplikatStatus()).isEqualTo(DuplikatStatusSpec.FALSE_POSITIVE);
    }

    @Test
    @TestAsAdminBS
    void merge() {
        // ARRANGE
        String name = TestUtil.getRandomString("DuplikatMergeTest");
        Kind kind1 = fixtureService.createKind(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        Kind kind2 = fixtureService.createKind(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        // Daten zu beiden Kindern erstellen
        Anlass anlass1 = fixtureService.createAnlass(AnlassTyp.KLASSE_4);
        Anlass anlass2 = fixtureService.createAnlass(AnlassTyp.KLASSE_8);
        Dossier dossier1 = fixtureService.createDossier(kind1);
        Dossier dossier2 = fixtureService.createDossier(kind2);
        KontaktPersonKind kontaktPersonKind1 = fixtureService.createKontaktPersonKind(kind1);
        KontaktPersonKind kontaktPersonKind2 = fixtureService.createKontaktPersonKind(kind2);
        Notiz notiz1 = fixtureService.createNotiz(dossier1, Set.of(Label.ID_LABEL_NACHZUEGLER), NotizTyp.DEFAULT);
        Notiz notiz2 = fixtureService.createNotiz(dossier2, Set.of(Label.ID_LABEL_DISPENSATIONSGESUCH), NotizTyp.DEFAULT);
        Aufgabe aufgabe1 = fixtureService.createAufgabe(kind1);
        Aufgabe aufgabe2 = fixtureService.createAufgabe(kind2);
        Untersuchung untersuchung1 =
            fixtureService.createUntersuchungWithCompleteDataInNewTransaction(anlass1, dossier1);
        Untersuchung untersuchung2 =
            fixtureService.createUntersuchungWithCompleteDataInNewTransaction(anlass2, dossier2);
        // Duplikat erstellen
        superAdminRunnerService.runReloadDuplikateJob(true);
        Optional<Duplikat> duplikatOptional = duplikatRepo.findByKindIds(kind1.getId(), kind2.getId());
        assertTrue(duplikatOptional.isPresent());
        Kind duplikatKind1 = duplikatOptional.get().getKind1();
        Assertions.assertThat(duplikatKind1).isNotNull();
        Kind duplikatKind2 = duplikatOptional.get().getKind2();
        Assertions.assertThat(duplikatKind2).isNotNull();

        var aufgaben1BeforeMerge = aufgabeService.findAllForKind(kind1.getId());
        var aufgaben2BeforeMerge = aufgabeService.findAllForKind(kind2.getId());
        var expectedAufgaben1AfterMerge =
            Stream.concat(aufgaben1BeforeMerge.stream(), aufgaben2BeforeMerge.stream()).toList();

        // ACT
        DuplikatJsonSpec result = api.duplikatResourceMerge()
            .duplikatIdPath(duplikatOptional.get().getId().getId())
            .basisKindIdQuery(duplikatKind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DuplikatJsonSpec.class);

        // ASSERT
        assertNotNull(result);
        assertThat(result.getDuplikatStatus()).isEqualTo(DuplikatStatusSpec.GEMERGT);
        assertThat(result.getKind1().getId()).isEqualTo(duplikatKind1.getId().getId());
        assertThat(result.getKind2().getId()).isEqualTo(duplikatKind2.getId().getId());
        assertThat(result.getKind2().getName()).isNull();
        assertThat(result.getKind2().getVorname()).isNull();
        assertThat(result.getKind2().getGeburtsdatum()).isNull();
        assertThat(result.getKind2().getGeschlecht()).isNull();
        // Kind 2 sollte nicht mehr existieren
        assertThat(kindService.findById(duplikatKind2.getId())).isEmpty();
        assertThat(dossierService.findByKind(duplikatKind2.getId())).isEmpty();
        assertThat(kontaktPersonKindService.findByKind(duplikatKind2.getId())).isEmpty();
        assertThat(notizService.findAllForKind(duplikatKind2.getId())).isEmpty();
        assertThat(aufgabeService.findAllForKind(duplikatKind2.getId())).isEmpty();
        assertThat(kindAnlaesseService.findAllEinladungForKind(duplikatKind2.getId())).isEmpty();
        // Kind 1 sollte die Daten von Kind 2 übernommen haben
        assertThat(kindService.findById(duplikatKind1.getId())).isPresent();
        assertThat(dossierService.findByKind(duplikatKind1.getId())).isPresent();
        assertThat(kontaktPersonKindService.findByKind(duplikatKind1.getId())).containsExactlyInAnyOrder(
            kontaktPersonKind1,
            kontaktPersonKind2);
        List<Notiz> notizenKind1 = fixtureService.findAllForKindWithLoadedFiles(duplikatKind1.getId());
        assertThat(notizenKind1).contains(notiz1, notiz2);
        assertThat(aufgabeService.findAllForKind(duplikatKind1.getId())).contains(aufgabe1, aufgabe2);
        assertThat(aufgabeService.findAllForKind(duplikatKind1.getId())).containsExactlyInAnyOrderElementsOf(
            expectedAufgaben1AfterMerge); // there are more aufgaben autocreated
        assertThat(kindAnlaesseService.findAllEinladungForKind(duplikatKind1.getId())).containsExactlyInAnyOrder(
            untersuchung1.getEinladung(),
            untersuchung2.getEinladung());

        // Kind1 hat eine zusätzliche Notiz erhalten, die den Merge beschreibt
        assertThat(notizenKind1).hasSize(3); // 2 gemergte plus 1 Merge-Notiz
        Optional<Notiz> notizMergeOptional =
            notizenKind1.stream().filter(notiz -> !notiz.getFiles().isEmpty()).findFirst();
        assertThat(notizMergeOptional).isPresent();
        assertThat(notizMergeOptional.get().getFiles()).hasSize(2);
        assertThat(notizMergeOptional.get().getFiles().getFirst().getFileTyp()).isEqualTo(FileTyp.KINDERDOSSIER);

        // download one of the files
        byte[] kinderDossierExport = notizapi.notizResourceDownloadNotizFile()
            .notizIdPath(notizMergeOptional.get().getId().getId())
            .fileIdPath(Objects.requireNonNull(notizMergeOptional.get().getFiles()).getFirst().getId().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(kinderDossierExport).isNotNull().hasSizeGreaterThan(0);
    }

    @Test
    @TestAsAdminBS
    void merge_escada_and_manual_choose_manual() {
        // ARRANGE
        String name = TestUtil.getRandomString("DuplikatMergeTest");
        SozialversicherungsNummer vnr = SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer();
        Kind kindManuell = fixtureService.createKind(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        Kind kindEscada = fixtureService.createKindEscada(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH, vnr, false);
        // Check
        assertThat(kindManuell.getExternalSystemIdOrNull()).isNull();
        assertThat(kindEscada.getExternalSystemIdOrNull()).isNotNull();
        assertThat(kindManuell.getExternalImportInfo()).isNull();
        assertThat(kindEscada.getExternalImportInfo()).isNotNull();
        assertThat(kindManuell.getSozialversicherungsNummer()).isNull();
        assertThat(kindEscada.getSozialversicherungsNummer()).isNotNull();

        // Duplikat erstellen
        superAdminRunnerService.runReloadDuplikateJob(true);
        Optional<Duplikat> duplikatOptional = duplikatRepo.findByKindIds(kindManuell.getId(), kindEscada.getId());
        assertTrue(duplikatOptional.isPresent());

        // ACT
        DuplikatJsonSpec result = api.duplikatResourceMerge()
            .duplikatIdPath(duplikatOptional.get().getId().getId())
            .basisKindIdQuery(kindManuell.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DuplikatJsonSpec.class);
        // Das Basiskind (manuell) neu laden
        Kind kindManuellAfterMerge = kindService.findById(kindManuell.getId()).orElseThrow();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(kindManuellAfterMerge.getId()).isEqualTo(kindManuell.getId());
        assertThat(kindManuellAfterMerge.getExternalSystemIdOrNull())
            .isNotNull()
            .isEqualTo(kindEscada.getExternalSystemIdOrNull());
        assertThat(kindManuellAfterMerge.getSozialversicherungsNummer()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void merge_escada_and_manual_choose_escada() {
        // ARRANGE
        String name = TestUtil.getRandomString("DuplikatMergeTest");
        SozialversicherungsNummer vnr = SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer();
        Kind kindManuell = fixtureService.createKind(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        Kind kindEscada = fixtureService.createKindEscada(name, name, LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH, vnr, false);
        // Check
        assertThat(kindManuell.getExternalSystemIdOrNull()).isNull();
        assertThat(kindEscada.getExternalSystemIdOrNull()).isNotNull();
        assertThat(kindManuell.getExternalImportInfo()).isNull();
        assertThat(kindEscada.getExternalImportInfo()).isNotNull();
        assertThat(kindManuell.getSozialversicherungsNummer()).isNull();
        assertThat(kindEscada.getSozialversicherungsNummer()).isNotNull();

        // Duplikat erstellen
        superAdminRunnerService.runReloadDuplikateJob(true);
        Optional<Duplikat> duplikatOptional = duplikatRepo.findByKindIds(kindManuell.getId(), kindEscada.getId());
        assertTrue(duplikatOptional.isPresent());

        // ACT
        DuplikatJsonSpec result = api.duplikatResourceMerge()
            .duplikatIdPath(duplikatOptional.get().getId().getId())
            .basisKindIdQuery(kindEscada.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DuplikatJsonSpec.class);
        // Das Basiskind (manuell) neu laden
        Kind kindEscadaAfterMerge = kindService.findById(kindEscada.getId()).orElseThrow();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(kindEscadaAfterMerge.getId()).isEqualTo(kindEscada.getId());
        assertThat(kindEscadaAfterMerge.getExternalSystemIdOrNull())
            .isNotNull()
            .isEqualTo(kindEscada.getExternalSystemIdOrNull());
        assertThat(kindEscadaAfterMerge.getSozialversicherungsNummer()).isNotNull();
    }
}
