package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class ArchivierungServiceTest extends AbstractITest {

    private final ArchivierungService archivierungService;
    private final FixtureService fixtureService;
    private final KindService kindService;
    private final DuplikatService duplikatService;
    private final KontaktPersonKindService kontaktPersonKindService;
    private final NotizService notizService;
    private final AufgabeService aufgabeService;
    private final EinladungService einladungService;
    private final ArchivdokumentService archivdokumentService;
    private final NachkontrolleService nachkontrolleService;
    private final AnlassService anlassService;
    private final DossierService dossierService;

    @Test
    @TestAsSupportBS
    void nothing_should_happen_if_kind_is_still_aktiv() {
        // ARRANGE
        Dossier dossier = fixtureService.createDossier();
        fixtureService.createArchivdokument(dossier.getId());

        assertThat(dossier.getKind().getKindAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);

        // ACT
        archivierungService.delete(dossier.getId());

        // ASSERT
        Optional<Kind> kind = kindService.findById(dossier.getKind().getId());
        assertThat(kind).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void nothing_should_happen_if_archivdokument_does_not_exist() {
        // ARRANGE
        Kind kind = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier = fixtureService.createDossier(kind);

        // ACT
        archivierungService.delete(dossier.getId());

        // ASSERT
        Optional<Kind> kindAfterDelete = kindService.findById(dossier.getKind().getId());
        assertThat(kindAfterDelete).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void nothing_should_happen_if_archivdokument_has_wrong_status() {
        // ARRANGE
        Kind kind = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier = fixtureService.createDossier(kind);
        fixtureService.createArchivdokument(dossier.getId(), ArchivierungStatus.DOWNLOADED);

        // ACT
        archivierungService.delete(dossier.getId());

        // ASSERT
        Optional<Kind> kindAfterDelete = kindService.findById(dossier.getKind().getId());
        assertThat(kindAfterDelete).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void all_data_from_kind_should_be_deleted() {
        // ARRANGE
        Kind kind = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier = fixtureService.createDossier(kind);
        Archivdokument archivdokument =
            fixtureService.createArchivdokument(dossier.getId(), ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);

        KontaktPersonKind kontaktPerson = fixtureService.createKontaktPersonKind(kind);
        Notiz notiz = fixtureService.createNotiz(dossier, Collections.emptySet(), NotizTyp.DEFAULT);
        fixtureService.createAufgabe(kind); // gets asserted by not beeing present
        fixtureService.createNachkontrolle(dossier, true); // gets asserted by not beeing present

        Anlass anlass1 =
            fixtureService.createAnlass(); // Sollte bestehen bleiben, da noch eine andere Einladung existiert.
        Anlass anlass2 = fixtureService.createAnlass();

        Einladung einladung1 = fixtureService.createEinladung(anlass1, dossier);
        Einladung einladung2 = fixtureService.createEinladung(anlass2, dossier);
        Einladung einladung3 =
            fixtureService.createEinladung(anlass1); // Sollte bestehen bleiben da von anderem Dossier.

        Notiz notizFragebogen = fixtureService.createFragebogenNotiz(einladung1.getId());
        fixtureService.createUntersuchungWithEveryTab(einladung1.getId()); // gets asserted by not beeing present

        Kind kind2 = fixtureService.createKind();
        fixtureService.createDuplikat(kind, kind2);

        // ACT
        archivierungService.delete(dossier.getId());

        // ASSERT
        Optional<KontaktPersonKind> kontaktPersonAfterDelete = kontaktPersonKindService.findById(kontaktPerson.getId());
        assertThat(kontaktPersonAfterDelete).isNotPresent();

        Optional<Notiz> notizAfterDelete = notizService.findById(notiz.getId());
        assertThat(notizAfterDelete).isNotPresent();

        List<Aufgabe> listAufgabeAfterDelete = aufgabeService.findAllForKind(kind.getId());
        assertThat(listAufgabeAfterDelete).isEmpty();

        List<Duplikat> duplikatAfterDelete = duplikatService.findByKindId(kind.getId());
        assertThat(duplikatAfterDelete).isNotEmpty()
            .noneMatch(duplikat -> duplikat.getKind1() != null && duplikat.getKind1().getId().equals(kind.getId()));

        Optional<Nachkontrolle> nachkontrolleAfterDelete = nachkontrolleService.findByDossier(dossier.getId());
        assertThat(nachkontrolleAfterDelete).isNotPresent();

        Optional<Notiz> notizFragebogenAfterDelete = notizService.findById(notizFragebogen.getId());
        assertThat(notizFragebogenAfterDelete).isNotPresent();

        List<Einladung> einladungenAfterDelete = einladungService.findAllByDossier(dossier.getId());
        assertThat(einladungenAfterDelete)
            .doesNotContain(einladung1)
            .doesNotContain(einladung2)
            .doesNotContain(einladung3)
            .isEmpty();

        List<Anlass> anlaesseAfterDelete = anlassService.findAll();
        assertThat(anlaesseAfterDelete).isNotEmpty()
            .anyMatch(anlass -> anlass.getId().equals(anlass1.getId()))
            .noneMatch(sameAnlass -> sameAnlass.getId().equals(anlass2.getId()));

        Optional<Dossier> dossierAfterDelete = dossierService.findByKind(kind.getId());
        assertThat(dossierAfterDelete).isNotPresent();

        Optional<Kind> kindAfterDelete = kindService.findById(kind.getId());
        assertThat(kindAfterDelete).isNotPresent();

        List<Archivdokument> archivdokumenteAfterDelete = archivdokumentService.findAll();
        archivdokumenteAfterDelete.stream().noneMatch(archiv -> archiv.getId().equals(archivdokument.getId()));
    }
}
