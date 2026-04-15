package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.context.api.ManagedExecutorConfig;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
class EdokidIdGeneratorSequenceServiceITest extends AbstractITest {

    private final ManagedExecutor parallelGenExecutor;
    private final EdokidIdGeneratorService edokidIdGeneratorService;
    private final EdokidIdRepo edokidIdRepo;
    private final LocalDate initalDate = getAppClock().localDateTime().now().toLocalDate();
    @Nullable
    private EdokidIdSequence previousSequence; // store previous sequence if it exists

    // get app clock from enclosing class

    @Inject
    public EdokidIdGeneratorSequenceServiceITest(
        @ManagedExecutorConfig(maxAsync = 8)
        ManagedExecutor parallelGenExecutor,
        EdokidIdGeneratorService edokidIdGeneratorService,
        EdokidIdRepo edokidIdRepo
    ) {
        this.parallelGenExecutor = parallelGenExecutor;
        this.edokidIdGeneratorService = edokidIdGeneratorService;
        this.edokidIdRepo = edokidIdRepo;

    }
    // get 2 digit year

    @BeforeAll
    @ActivateRequestContext
    void beforeAll() {
        Optional<EdokidIdSequence> sequenceOptional = edokidIdRepo.findSequenceByEintrittsjahrAndLock(
            EdokidIdGeneratorService.calculateEintrittsJahr(initalDate));
        previousSequence = sequenceOptional.orElse(null);

        // Remove existing sequence
        sequenceOptional.ifPresent(
            optional -> edokidIdRepo.remove(optional.getId())
        );
    }

    //    @AfterAll
    @ActivateRequestContext
    void restoreSequence() {
        // Restore previous sequence if it existed
        if (previousSequence != null) {
            // Remove any test sequence
            Optional<EdokidIdSequence> currentSequence =
                edokidIdRepo.findSequenceByEintrittsjahrAndLock(previousSequence.getEintrittsJahr());
            currentSequence.ifPresent(
                optional -> edokidIdRepo.remove(optional.getId())
            );
            edokidIdRepo.persist(previousSequence);
            LOG.info("Restored previous sequence in test: {}", previousSequence);
        }
        resetTime();
    }

    @Order(10)
    @ParameterizedTest
    @TestAsAdminBS
    @CsvSource({
        "A, 001, 94, A002",
        "A, 999, 94, B001",
        "B, 001, 94, B002",
        "B, 999, 94, C001",
        "A, 001, 95, A002",
        "A, 999, 95, B001",
        "B, 001, 95, B002",
        "B, 999, 95, C001",
    })
    void testStepToNextCharacter(char character, String laufnummer, int eintrittsJahr, String expectedLaufnummer) {
        // ARRANGE
        Optional<EdokidIdSequence> sequenceOptional = edokidIdRepo.findSequenceByEintrittsjahrAndLock(eintrittsJahr);
        sequenceOptional.ifPresent(
            optional -> edokidIdRepo.remove(optional.getId())
        );
        EdokidIdSequence sequence = new EdokidIdSequence(eintrittsJahr, character, Integer.parseInt(laufnummer));
        edokidIdRepo.persist(sequence);
        // ACT
        EdokidIdSequence nextLaufnummer = edokidIdGeneratorService.getNextEdokidIdSequence(eintrittsJahr);

        // ASSERT
        assertThat(nextLaufnummer.getCharacter() + nextLaufnummer.getFormattedLaufnummer())
            .isEqualTo(expectedLaufnummer);
        assertThat(nextLaufnummer.getEintrittsJahr()).isEqualTo(eintrittsJahr);
    }

    @Order(20)
    @Test
    @TestAsAdminBS
    void generateNextEdokidId() {
        // ARRANGE
        this.setTime(OffsetDateTime.of(
            2096, 9, 1,
            12, 0, 0, 0, ZoneOffset.UTC));

        LocalDate today = getAppClock().localDateTime().now().toLocalDate();
        int eintrittsJahr = EdokidIdGeneratorService.calculateEintrittsJahr(today);
        // ARRANGE

        Optional<EdokidIdSequence> sequenceOptional = edokidIdRepo.findSequenceByEintrittsjahrAndLock(eintrittsJahr);
        sequenceOptional.ifPresent(
            optional -> edokidIdRepo.remove(optional.getId())
        );
        Schule schule = SchuleBuilder.defaultSchule();
        // ACT
        EdokidId actualEdokidId1 = edokidIdGeneratorService.generateNextEdokidId(schule);
        schule.setSchulhausnummer("321");
        EdokidId actualEdokidId2 = edokidIdGeneratorService.generateNextEdokidId(schule);
        EdokidId actualEdokidId3 = edokidIdGeneratorService.generateNextEdokidId(schule);

        EdokidId expectedEdokidId1 = new EdokidId('A', "001", "123", eintrittsJahr);
        EdokidId expectedEdokidId2 = new EdokidId('A', "002", "321", eintrittsJahr);
        EdokidId expectedEdokidId3 = new EdokidId('A', "003", "321", eintrittsJahr);

        // ASSERT
        assertThat(actualEdokidId1).isNotNull();
        assertThat(actualEdokidId1.getCharacter()).isEqualTo(expectedEdokidId1.getCharacter());
        assertThat(actualEdokidId1.getNummer()).isEqualTo(expectedEdokidId1.getNummer());
        assertThat(actualEdokidId1.getSchulhausnummer()).isEqualTo(expectedEdokidId1.getSchulhausnummer());
        assertThat(actualEdokidId1.getEintrittsjahr()).isEqualTo(expectedEdokidId1.getEintrittsjahr());

        assertThat(actualEdokidId1.toFormattedString()).isEqualTo(expectedEdokidId1.toFormattedString());
        assertThat(actualEdokidId2.toFormattedString()).isEqualTo(expectedEdokidId2.toFormattedString());
        assertThat(actualEdokidId3.toFormattedString()).isEqualTo(expectedEdokidId3.toFormattedString());
    }

    @Order(30)
    @ParameterizedTest
    @CsvSource({
        "2098",
        "2099",
    })
    @TestAsAdminBS
    void genarateNextEdokidId_should_fail_when_ids_exhausted(int jahr) {
        // ARRANGE
        this.setTime(OffsetDateTime.of(
            jahr, 12, 1,
            1, 0, 0, 0, ZoneOffset.UTC));

        int eintrittsjahr =
            EdokidIdGeneratorService.calculateEintrittsJahr(this.getAppClock().localDateTime().now().toLocalDate());
        Optional<EdokidIdSequence> sequenceOptional = edokidIdRepo.findSequenceByEintrittsjahrAndLock(eintrittsjahr);
        sequenceOptional.ifPresent(
            optional -> edokidIdRepo.remove(optional.getId())
        );

        EdokidIdSequence lastSequence = new EdokidIdSequence(jahr % 100, 'Z', 999);
        edokidIdRepo.persist(lastSequence);

        Schule schule = SchuleBuilder.defaultSchule();
        // ACT & ASSERT
        assertThatThrownBy(() -> edokidIdGeneratorService.generateNextEdokidId(schule))
            .isInstanceOf(AppFailureException.class)
            .hasMessageContaining("AppFailure.EDOKID_ID_LAST_LAUFNUMMER_REACHED");
    }

    @Order(40)
    @Test
    @TestAsAdminBS
    void does_not_run_into_optimistic_locks() {
        // ARRANGE
        // set eintrittsjahr to 97 and remove previous sequence
        setTime(OffsetDateTime.of(
            2098, 2, 1,
            12, 0, 0, 0, ZoneOffset.UTC));
        Optional<EdokidIdSequence> sequenceOptional = edokidIdRepo.findSequenceByEintrittsjahrAndLock(97);
        sequenceOptional.ifPresent(
            optional -> edokidIdRepo.remove(optional.getId())
        );
        // make sure the sequence is initialized
        edokidIdGeneratorService.getNextEdokidIdSequence(97);

        int numOfNumbersToGenerate = 800;
        Schule schule = SchuleBuilder.defaultSchule();
        List<CompletableFuture<EdokidId>> futures = new ArrayList<>();
        for (int i = 0; i < numOfNumbersToGenerate; i++) {
            CompletableFuture<EdokidId> future = CompletableFuture.supplyAsync(
                    () -> edokidIdGeneratorService.generateNextEdokidId(schule),
                    parallelGenExecutor)
                .orTimeout(5, TimeUnit.SECONDS);
            futures.add(future);
        }

        List<EdokidId> edokidIds = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        Assertions.assertThat(edokidIds).hasSize(numOfNumbersToGenerate)
            .extracting(EdokidId::toFormattedString)
            .allMatch(s -> s.matches(EdokidIdTestGenerator.EDOKID_ID_REGEX_PATTERN))
            .allMatch(s -> s.endsWith(" 97"));

        restoreSequence(); // since this is the last test we clean up the sequence

    }

}
