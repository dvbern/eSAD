package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ImpfstoffChargeRepoTest extends AbstractITest {

    private final UntersuchungImpfungInternRepo impfungInternRepo;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getImpfstoffChargenNotUsedSince_should_return_charges_used_only_before_cutoff() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        ImpfstoffCharge chargeUsedContinuously = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("ContinuouslyUsedCharge"));
        impfstoffChargeService.persist(chargeUsedContinuously);

        ImpfstoffCharge chargeUsedRecently = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("RecentCharge"));
        impfstoffChargeService.persist(chargeUsedRecently);

        ImpfstoffCharge chargeUsedLongAgo = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("OldCharge"));
        impfstoffChargeService.persist(chargeUsedLongAgo);

        ImpfstoffCharge chargeNeverUsed = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedCharge"));
        impfstoffChargeService.persist(chargeNeverUsed);

        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.now());
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedContinuously)
            .withDatumImpfung(LocalDate.now().minusMonths(1))));
        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedContinuously)
            .withDatumImpfung(LocalDate.now().minusMonths(13))));

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedRecently)
            .withDatumImpfung(LocalDate.now().minusMonths(1))));

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedLongAgo)
            .withDatumImpfung(LocalDate.now().minusMonths(13))));

        // ACT
        List<ImpfstoffCharge> result = impfungInternRepo.getImpfstoffChargenNotUsedSince(12, 100);

        // ASSERT
        Assertions.assertThat(result)
            .contains(chargeUsedLongAgo)
            .doesNotContain(chargeUsedRecently)
            .doesNotContain(chargeUsedContinuously)
            .doesNotContain(chargeNeverUsed);
    }

    @Test
    @TestAsAdminBS
    void getImpfstoffChargenNotUsedSince_should_return_empty_list_when_no_charges_match() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        ImpfstoffCharge chargeUsedRecently = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("RecentCharge"));
        impfstoffChargeService.persist(chargeUsedRecently);

        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.now());
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedRecently)
            .withDatumImpfung(LocalDate.now().minusMonths(1))));

        // ACT
        List<ImpfstoffCharge> result = impfungInternRepo.getImpfstoffChargenNotUsedSince(12, 100);

        // ASSERT
        Assertions.assertThat(result).isNotEmpty().doesNotContain(chargeUsedRecently);
    }

    @Test
    @TestAsAdminBS
    void getImpfstoffChargenNotUsedSince_should_respect_batchsize() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        ImpfstoffCharge charge1 = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("OldCharge1"));
        impfstoffChargeService.persist(charge1);

        ImpfstoffCharge charge2 = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("OldCharge2"));
        impfstoffChargeService.persist(charge2);

        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.now());
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(charge1)
            .withDatumImpfung(LocalDate.now().minusMonths(13))));

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(charge2)
            .withDatumImpfung(LocalDate.now().minusMonths(14))));

        // ACT
        List<ImpfstoffCharge> result = impfungInternRepo.getImpfstoffChargenNotUsedSince(12, 1);

        // ASSERT
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @TestAsAdminBS
    void getImpfstoffChargenNotReferencedSince_should_return_never_used_charges_older_than_cutoff() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        ImpfstoffCharge chargeUsed = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("UsedCharge"));
        impfstoffChargeService.persist(chargeUsed);

        setTime(OffsetDateTime.now().minusMonths(13));
        ImpfstoffCharge chargeNeverUsedOld = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedOldCharge"));
        impfstoffChargeService.persist(chargeNeverUsedOld);
        resetTime();

        ImpfstoffCharge chargeNeverUsedRecent = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedRecentCharge"));
        impfstoffChargeService.persist(chargeNeverUsedRecent);

        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.now());
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsed)
            .withDatumImpfung(LocalDate.now().minusMonths(1))));

        // ACT
        List<ImpfstoffCharge> result = impfungInternRepo.getImpfstoffChargenNotReferencedSince(12, 100);

        // ASSERT
        Assertions.assertThat(result)
            .contains(chargeNeverUsedOld)
            .doesNotContain(chargeUsed)
            .doesNotContain(chargeNeverUsedRecent);
    }

    @Test
    @TestAsAdminBS
    void getImpfstoffChargenNotReferencedSince_should_respect_batchsize() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        setTime(OffsetDateTime.now().minusMonths(13));
        ImpfstoffCharge charge1 = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedOld1"));
        impfstoffChargeService.persist(charge1);

        setTime(OffsetDateTime.now().minusMonths(14));
        ImpfstoffCharge charge2 = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedOld2"));
        impfstoffChargeService.persist(charge2);
        resetTime();

        // ACT
        List<ImpfstoffCharge> result = impfungInternRepo.getImpfstoffChargenNotReferencedSince(12, 1);

        // ASSERT
        Assertions.assertThat(result).hasSize(1);
    }
}
