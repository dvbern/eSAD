package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungImpfungInternRepoTest extends AbstractITest {

    private final UntersuchungImpfungInternRepo untersuchungImpfungInternRepo;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final ImpfstoffService impfstoffService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findByUntersuchung_should_order_by_impfstoffName_then_datumImpfung_desc_with_nulls_first() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
        Assertions.assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        Impfstoff impfstoffA = impfstoffService.persist(ImpfstoffBuilder.create(b -> b.withName("AAA-Impfstoff")));
        Impfstoff impfstoffB = impfstoffService.persist(ImpfstoffBuilder.create(b -> b.withName("BBB-Impfstoff")));

        // Innerhalb von "BBB-Impfstoff": null zuerst, danach neueste zuerst
        UntersuchungImpfungIntern bNullDate = persistImpfung(untersuchung, impfstoffB, null);
        UntersuchungImpfungIntern bNewest = persistImpfung(untersuchung, impfstoffB, LocalDate.of(2024, 6, 1));
        UntersuchungImpfungIntern bMiddle = persistImpfung(untersuchung, impfstoffB, LocalDate.of(2023, 1, 15));

        // Innerhalb von "AAA-Impfstoff": neueste zuerst
        UntersuchungImpfungIntern aNewest = persistImpfung(untersuchung, impfstoffA, LocalDate.of(2025, 3, 10));
        UntersuchungImpfungIntern aOldest = persistImpfung(untersuchung, impfstoffA, LocalDate.of(2020, 1, 1));

        // ACT
        List<UntersuchungImpfungIntern> result =
            untersuchungImpfungInternRepo.findByUntersuchung(untersuchung.getId());

        // ASSERT
        Assertions.assertThat(result)
            .extracting(UntersuchungImpfungIntern::getId)
            .containsExactly(
                // Zuerst alphabetisch nach Impfstoff-Name aufsteigend (AAA vor BBB),
                // innerhalb gleicher Impfstoffe nach datumImpfung aufsteigend, null zuletzt.
                aOldest.getId(),
                aNewest.getId(),
                bMiddle.getId(),
                bNewest.getId(),
                bNullDate.getId()
            );
    }

    @NonNull
    private UntersuchungImpfungIntern persistImpfung(
        @NonNull Untersuchung untersuchung,
        @NonNull Impfstoff impfstoff,
        @Nullable LocalDate datumImpfung
    ) {
        return untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(Objects.requireNonNull(untersuchung.getUntersuchungImpfungen()))
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withDatumImpfung(datumImpfung)));
    }
}
