/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.kinddeactivation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KinderDeactivationServiceTest extends AbstractITest {

    private static final int INACTIVATION_AGE = 16;
    private static final int UNUSED_FOR_MOTHS = 9;
    public static final int DEFAULT_BATCHSIZE_FOR_TESTING = 30000;

    private final LocalDate shortTimeAgo = getAppClock().localDateTime().now().toLocalDate();
    private final LocalDate longTimeAgo = shortTimeAgo.minusYears(1);

    private final FixtureService fixtureService;
    private final KindService kindService;
    private final KindDeactivationService kindDeactivationService;
    private final DossierService dossierService;

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_manually_created_kinder_findKinderToDeactivate_should_find_unused_kinder() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        // create some manually created Kinder that should be deleted
        List<ID<Kind>> manualKinderToBeDeleted = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1),
            null,
            true,
            null,
            null,
            shortTimeAgo
            // last import timestamp is irrelevant for manually created (should not be set anyway)
        ).stream().map(Kind::getId).toList();

        // create some manually created Kinder that should not be deleted because of age
        List<ID<Kind>> manualNotDeletedAge = createKinder(
            2,
            cutoffGeburtsdatum.plusDays(1), null, true,
            null, null, longTimeAgo
        ).stream().map(Kind::getId).toList();

        // create some manually created Kinder that should not be deleted because of newish Notiz
        List<ID<Kind>> manualNotDeletedBecauseNewNotiz = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1), null, true,
            cutoffNotizAndAnlass.plusDays(1), null, longTimeAgo
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> manualDeletedWithOldNotiz = createKinder(
            1,
            cutoffGeburtsdatum.minusDays(1), null, true,
            cutoffNotizAndAnlass.minusDays(1), null, null
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> manualDeleteWithOldAnlass = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1), null, true,
            null, cutoffNotizAndAnlass.minusDays(1), longTimeAgo
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> maualNotDeleteWithNewishAnlass = createKinder(
            3,
            cutoffGeburtsdatum.minusDays(1), null, true,
            null, cutoffNotizAndAnlass, cutoffNotizAndAnlass
        ).stream().map(Kind::getId).toList();

        // ACT
        List<ID<Kind>> kinderToDeactivate = kindDeactivationService.findKinderToDeactivate(
            UNUSED_FOR_MOTHS, INACTIVATION_AGE,
            DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        assertThat(kinderToDeactivate).isNotEmpty()
            .containsAll(manualKinderToBeDeleted)
            .containsAll(manualDeletedWithOldNotiz)
            .containsAll(manualDeleteWithOldAnlass)
            .doesNotContainAnyElementsOf(manualNotDeletedBecauseNewNotiz)
            .doesNotContainAnyElementsOf(manualNotDeletedAge)
            .doesNotContainAnyElementsOf(maualNotDeleteWithNewishAnlass);

    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_external_kinder_findKinderToDeactivate_should_delete_kinder_with_no_dossier() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        // deactivate kinder that are inaktiv in escada if they have no dossier, age irrelevant
        List<ID<Kind>> externalKinderToDeactivate =
            createKinder(
                3,
                cutoffGeburtsdatum.minusYears(2),
                UUID.randomUUID().toString(),
                false,
                null,
                null,
                longTimeAgo // will only deactivate if it is a long time ago since we last imported
            ).stream().map(Kind::getId).toList();

        // keep kinder that are still active in escada (age etc is irrelevant)
        List<ID<Kind>> externalKinderToKeep =
            createKinder(
                3,
                cutoffGeburtsdatum.minusYears(50),
                UUID.randomUUID().toString(),
                true,
                null,
                null,
                longTimeAgo)
                .stream().map(Kind::getId).toList();

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .containsAll(externalKinderToDeactivate)
            .doesNotContainAnyElementsOf(externalKinderToKeep);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_external_kinder_findKinderToDeactivate_should_check_notizen() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        List<ID<Kind>> externalKinderToDeactivate = createKinder(
            4,
            cutoffGeburtsdatum,
            UUID.randomUUID().toString(),
            false,
            cutoffNotizAndAnlass,
            null,
            longTimeAgo
        ).stream().map(Kind::getId).toList();

        // keep kinder even if they are inaktiv in escada if they have a current notiz
        List<ID<Kind>> externalKinderWithNewishNotiz = createKinder(
            1,
            cutoffGeburtsdatum.minusYears(50),
            UUID.randomUUID().toString(),
            false,
            cutoffNotizAndAnlass.plusDays(1),
            null,
            longTimeAgo
        ).stream().map(Kind::getId).toList();

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .containsAll(externalKinderToDeactivate)
            .doesNotContainAnyElementsOf(externalKinderWithNewishNotiz);
    }

    /**
     * Prueft den Stichtag ({@value #UNUSED_FOR_MOTHS} Monate zurueck) minutengenau ueber
     * Sommer-/Winterzeitgrenzen hinweg. Der korrekte, zonenbewusst berechnete Stichtag hat dieselbe lokale
     * Uhrzeit wie "jetzt"; die Notiz wird jeweils eine Minute davor bzw. danach erstellt.
     * <p>
     * Der Vergleich in der DB ist {@code timestampErstellt >= cutoff}: eine Notiz ab dem Stichtag verhindert
     * die Deaktivierung, eine aeltere nicht.
     * <p>
     * Die fruehere, nicht zonenbewusste Berechnung ({@code OffsetDateTime.minusMonths()} behaelt den heutigen
     * Offset bei) verschob den Stichtag um eine Stunde und kippte damit das Resultat - je nach Richtung des
     * Zeitzonenwechsels in beide Richtungen.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("dstScenarios")
    @TestAsAdminBS
    @TestTransaction
    // rollback after test since we might introduce new anlass configs
    void findKinderToDeactivate_should_not_depend_on_dst(
        @SuppressWarnings("unused") String beschreibung,
        LocalDate heute,
        int notizMinutenRelativZumStichtag,
        boolean sollDeaktiviertWerden
    ) {
        // ARRANGE
        OffsetDateTime jetzt = atTestTime(heute);
        OffsetDateTime stichtag = atTestTime(heute.minusMonths(UNUSED_FOR_MOTHS));
        OffsetDateTime notizTimestamp = stichtag.plusMinutes(notizMinutenRelativZumStichtag);

        Kind kind = createKindWithNotizAt(heute, notizTimestamp);
        setTime(jetzt);

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        if (sollDeaktiviertWerden) {
            Assertions.assertThat(foundKinderToDeactivate)
                .contains(kind.getId());
        } else {
            Assertions.assertThat(foundKinderToDeactivate)
                .doesNotContain(kind.getId());
        }
    }

    static Stream<Arguments> dstScenarios() {
        // 2026-09-21 ist Sommerzeit (UTC+2), der Stichtag 2025-12-21 Winterzeit (UTC+1).
        LocalDate sommerzeitMitStichtagInWinterzeit = LocalDate.of(2026, 9, 21);
        // 2026-01-21 ist Winterzeit (UTC+1), der Stichtag 2025-04-21 Sommerzeit (UTC+2).
        LocalDate winterzeitMitStichtagInSommerzeit = LocalDate.of(2026, 1, 21);
        // 2026-06-21 und der Stichtag 2025-09-21 liegen beide in der Sommerzeit (UTC+2).
        LocalDate ohneZeitzonenwechsel = LocalDate.of(2026, 6, 21);

        return Stream.of(
            // Die alte Berechnung schob den Stichtag hier eine Stunde zurueck: die Notiz lag dadurch
            // faelschlicherweise nach dem Stichtag und das Kind wurde nicht deaktiviert.
            Arguments.of("Sommer->Winter, Notiz eine Minute vor Stichtag",
                sommerzeitMitStichtagInWinterzeit, -1, true),
            Arguments.of("Sommer->Winter, Notiz eine Minute nach Stichtag",
                sommerzeitMitStichtagInWinterzeit, 1, false),
            Arguments.of("Winter->Sommer, Notiz eine Minute vor Stichtag",
                winterzeitMitStichtagInSommerzeit, -1, true),
            // Die alte Berechnung schob den Stichtag hier eine Stunde vor: die Notiz lag dadurch
            // faelschlicherweise vor dem Stichtag und das Kind wurde faelschlicherweise deaktiviert.
            Arguments.of("Winter->Sommer, Notiz eine Minute nach Stichtag",
                winterzeitMitStichtagInSommerzeit, 1, false),
            Arguments.of("Ohne Zeitzonenwechsel, Notiz eine Minute vor Stichtag",
                ohneZeitzonenwechsel, -1, true),
            // Genau auf dem Stichtag: der Vergleich ist >=, die Notiz verhindert die Deaktivierung.
            Arguments.of("Ohne Zeitzonenwechsel, Notiz exakt auf Stichtag",
                ohneZeitzonenwechsel, 0, false),
            Arguments.of("Ohne Zeitzonenwechsel, Notiz eine Minute nach Stichtag",
                ohneZeitzonenwechsel, 1, false)
        );
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_external_kinder_findKinderToDeactivate_should_check_anlaesse() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        List<ID<Kind>> externalKinderToDeactivate = createKinder(
            2,
            cutoffGeburtsdatum,
            UUID.randomUUID().toString(),
            false,
            null,
            cutoffNotizAndAnlass.minusDays(1),
            longTimeAgo
        ).stream().map(Kind::getId).toList();

        // keep kinder even if they are inaktiv in escada if they have a current notiz
        List<ID<Kind>> externalKinderWithNewishAnlass = createKinder(
            3,
            cutoffGeburtsdatum.minusYears(50),
            UUID.randomUUID().toString(),
            false,
            null,
            cutoffNotizAndAnlass,
            longTimeAgo
        ).stream().map(Kind::getId).toList();

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .containsAll(externalKinderToDeactivate)
            .doesNotContainAnyElementsOf(externalKinderWithNewishAnlass);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void findKinderToDeactivate_should_respect_batchsize() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        createKinder(
            6,
            cutoffGeburtsdatum,
            UUID.randomUUID().toString(),
            false,
            cutoffNotizAndAnlass,
            cutoffNotizAndAnlass.minusDays(1),
            longTimeAgo
        );

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, 5);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate).hasSize(5);

    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void findKinderToDeactivate_should_not_find_deactivated_kinder() {
        // ARRANGE
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        List<Kind> kinder = createKinder(
            6,
            cutoffGeburtsdatum,
            UUID.randomUUID().toString(),
            false,
            cutoffNotizAndAnlass,
            cutoffNotizAndAnlass.minusDays(1),
            longTimeAgo
        );
        Kind deactivatedKind = kinder.getFirst();
        deactivatedKind.disableOptimisticLockingCheck();
        deactivatedKind.setKindAktivStatus(EntityAktivStatus.INAKTIV);
        deactivatedKind = kindService.persistWithoutDuplicateCheck(deactivatedKind);

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .isNotEmpty()
            .doesNotContain(deactivatedKind.getId());
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_external_kinder_findKinderToDeactivate_should_delete_kinder_with_last_import_long_ago() {
        // ARRANGE
        LocalDateTime now = getAppClock().localDateTime().now();
        LocalDate cutoffGeburtsdatum = now.minusYears(INACTIVATION_AGE).toLocalDate();
        // deactivate kinder that are inaktiv in escada if they have no dossier, age irrelevant
        LocalDate geburtsdatum = cutoffGeburtsdatum.minusYears(2);

        Kind kindExternAktivOhneDossierKuerzlichImportiert = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            true,
            null,
            null,
            shortTimeAgo);

        Kind kindExternAktivOhneDossierLangeImportiert = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            true,
            null,
            null,
            longTimeAgo);

        Kind kindExternInaktivOhneDossierKuerzlichImportiert = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            null,
            null,
            shortTimeAgo);

        // Kein Dossier, lange importiert -> deaktiveren
        Kind kindExternInaktivOhneDossierLangeImportiert = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            null,
            null,
            longTimeAgo);

        Kind kindExternInaktivMitDossierAllesKuerzlich = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            now.toLocalDate().minusMonths(1),
            now.toLocalDate().minusMonths(1),
            now.toLocalDate().minusMonths(1));

        Kind kindExternInaktivMitDossierLangeImportiert = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            now.toLocalDate().minusMonths(1),
            now.toLocalDate().minusMonths(1),
            now.toLocalDate().minusYears(1));

        Kind kindExternInaktivMitDossierLangeAnlass = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            now.toLocalDate().minusMonths(1),
            longTimeAgo,
            now.toLocalDate().minusMonths(1));

        Kind kindExternInaktivMitDossierLangeNotiz = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            longTimeAgo,
            now.toLocalDate().minusMonths(1),
            now.toLocalDate().minusMonths(1));

        Kind kindExternInaktivMitDossierLangeNotizAndAnlass = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            longTimeAgo,
            longTimeAgo,
            now.toLocalDate().minusMonths(1));

        // Sowohl Anlass, Notiz, Import lange -> deaktivieren
        Kind kindExternInaktivMitDossierLangeAlles = createKindWithAnlassAndNotiz(
            geburtsdatum,
            UUID.randomUUID().toString(),
            false,
            longTimeAgo,
            longTimeAgo,
            longTimeAgo);

        // ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .contains(
                kindExternInaktivOhneDossierLangeImportiert.getId(),
                kindExternInaktivMitDossierLangeAlles.getId()
            )
            .doesNotContain(
                kindExternAktivOhneDossierKuerzlichImportiert.getId(),
                kindExternAktivOhneDossierLangeImportiert.getId(),
                kindExternInaktivOhneDossierKuerzlichImportiert.getId(),
                kindExternInaktivMitDossierAllesKuerzlich.getId(),
                kindExternInaktivMitDossierLangeImportiert.getId(),
                kindExternInaktivMitDossierLangeAnlass.getId(),
                kindExternInaktivMitDossierLangeNotiz.getId(),
                kindExternInaktivMitDossierLangeNotizAndAnlass.getId()
            );
    }

    /**
     * Fixe Testuhrzeit kurz nach Mitternacht: damit liegen Stichtag und Notiz am selben Tag und eine
     * Verschiebung um eine Stunde ist am Resultat ablesbar.
     */
    @NonNull
    private static OffsetDateTime atTestTime(@NonNull LocalDate datum) {
        return datum
            .atTime(LocalTime.of(0, 30))
            .atZone(DateConst.ZONE_ID)
            .toOffsetDateTime();
    }

    /**
     * Erstellt ein extern inaktives Kind mit Dossier und genau einer Notiz zum uebergebenen Zeitpunkt.
     * Alle uebrigen Kriterien (Alter, letzter Import) liegen weit vor dem Stichtag, damit einzig die Notiz
     * ueber das Resultat entscheidet.
     */
    @NonNull
    private Kind createKindWithNotizAt(
        @NonNull LocalDate heute,
        @NonNull OffsetDateTime notizTimestamp
    ) {
        Klasse klasse = fixtureService.createKlasse();
        OffsetDateTime lastImport = heute.minusYears(1)
            .atStartOfDay(DateConst.ZONE_ID)
            .toOffsetDateTime();
        Kind kind = KindBuilder.create(builder -> builder
            .withKlasse(klasse)
            .withGeburtsdatum(heute.minusYears(INACTIVATION_AGE + 1L))
            .withExternalImportInfo(
                UUID.randomUUID().toString(),
                false,
                lastImport,
                lastImport,
                ImportTyp.ESCADA)
        );
        kindService.persistWithoutDuplicateCheck(kind);

        setTime(notizTimestamp);
        Dossier dossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind))
        );
        fixtureService.createNotiz(dossier, Set.of(), NotizTyp.DEFAULT);
        resetTime();

        return kind;
    }

    private List<Kind> createKinder(
        int count,
        @NonNull LocalDate geburtsdatum, // to check if a manual kind is to be deleted
        @Nullable String externalId, // to determine if kind is manually added or through escada
        @Nullable Boolean externalAktiv, // null if not escada
        @Nullable LocalDate lastNotizDate,
        @Nullable LocalDate lastAnlassDate,
        @Nullable LocalDate lastImportDate
    ) {
        List<Kind> createdKinder = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Kind k =
                createKindWithAnlassAndNotiz(
                    geburtsdatum,
                    externalId,
                    externalAktiv,
                    lastNotizDate,
                    lastAnlassDate,
                    lastImportDate);
            createdKinder.add(k);
        }
        return createdKinder;
    }

    private Kind createKindWithAnlassAndNotiz(
        @NonNull LocalDate geburtsdatum,
        @Nullable String externalId,
        @Nullable Boolean externalAktiv,
        @Nullable LocalDate lastNotizDate,
        @Nullable LocalDate lastAnlassDate,
        @Nullable LocalDate lastImportDate
    ) {
        Klasse klasse = fixtureService.createKlasse();
        Kind kind;
        if (externalAktiv != null && externalId != null) {
            Assertions.assertThat(lastImportDate).as("lastImportDate must be set if externalAktiv is set").isNotNull();
            OffsetDateTime lastImportTimestamp = OffsetDateTime.ofInstant(
                lastImportDate.atStartOfDay().atZone(DateConst.ZONE_ID).toInstant(),
                DateConst.ZONE_ID);
            boolean primitiveExternalAktiv = externalAktiv;
            kind = KindBuilder.create(builder -> builder
                .withKlasse(klasse)
                .withGeburtsdatum(geburtsdatum)
                .withExternalImportInfo(
                    externalId,
                    primitiveExternalAktiv,
                    getAppClock().offsetDateTime().now(),
                    lastImportTimestamp,
                    ImportTyp.ESCADA)
            );
        } else {
            kind = KindBuilder.create(builder -> builder
                .withKlasse(klasse)
                .withGeburtsdatum(geburtsdatum)
            );
        }
        kindService.persistWithoutDuplicateCheck(kind);

        // check if we need a dossier
        if (lastNotizDate != null || lastAnlassDate != null) {
            // dossier creation time
            LocalDate dossierCrtTime = Objects.requireNonNull(DateUtil.minDate(lastNotizDate, lastAnlassDate));
            setTime(OffsetDateTime.ofInstant(
                dossierCrtTime.atStartOfDay().atZone(DateConst.ZONE_ID).toInstant(),
                DateConst.ZONE_ID));
            Dossier dossier = dossierService.persist(
                DossierBuilder.create(builder -> builder.forKind(kind))
            );

            if (lastNotizDate != null) {
                // set timestamptErstellt Time
                setTime(OffsetDateTime.ofInstant(
                    lastNotizDate.atStartOfDay().atZone(DateConst.ZONE_ID).toInstant(),
                    DateConst.ZONE_ID));
                fixtureService.createNotiz(dossier, Set.of(), NotizTyp.DEFAULT);
            }
            if (lastAnlassDate != null) {
                // set timestamptErstellt Time
                setTime(OffsetDateTime.ofInstant(
                    lastAnlassDate.atStartOfDay().atZone(DateConst.ZONE_ID).toInstant(),
                    DateConst.ZONE_ID));
                Anlass anlass = fixtureService.createAnlass(lastAnlassDate);
                fixtureService.createEinladung(anlass, dossier);
            }
            resetTime();
        }
        return kind;
    }
}
