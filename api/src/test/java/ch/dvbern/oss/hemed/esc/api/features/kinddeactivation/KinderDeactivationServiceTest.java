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

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KinderDeactivationServiceTest extends AbstractITest {

    private static final int INACTIVATION_AGE = 16;
    private static final int UNUSED_FOR_MOTHS = 9;
    public static final int DEFAULT_BATCHSIZE_FOR_TESTING = 30000;

    private final FixtureService fixtureService;
    private final KindService kindService;
    private final KindDeactivationService kindDeactivationService;
    private final DossierService dossierService;

    @Test
    @TestAsAdminBS
    @TestTransaction
        // rollback after test since we might introduce new anlass configs
    void given_manually_created_kinder_findKinderToDeactivate_should_find_unused_kinder() {
        // Arrange
        LocalDate cutoffGeburtsdatum = getAppClock().localDateTime().now().minusYears(INACTIVATION_AGE).toLocalDate();
        LocalDate cutoffNotizAndAnlass =
            getAppClock().localDateTime().now().toLocalDate().minusMonths(UNUSED_FOR_MOTHS);

        // create some manually created Kinder that should be deleted
        List<ID<Kind>> manualKinderToBeDeleted = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1), null, true,
            null, null
        ).stream().map(Kind::getId).toList();

        // create some manually created Kinder that should not be deleted because of age
        List<ID<Kind>> manualNotDeletedAge = createKinder(
            2,
            cutoffGeburtsdatum.plusDays(1), null, true, null, null
        ).stream().map(Kind::getId).toList();

        // create some manually created Kinder that should not be deleted because of newish Notiz
        List<ID<Kind>> manualNotDeletedBecauseNewNotiz = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1), null,
            true, cutoffNotizAndAnlass.plusDays(1), null
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> manualDeletedWithOldNotiz = createKinder(
            1,
            cutoffGeburtsdatum.minusDays(1), null, true,
            cutoffNotizAndAnlass.minusDays(1), null
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> manualDeleteWithOldAnlass = createKinder(
            2,
            cutoffGeburtsdatum.minusDays(1), null, true,
            null, cutoffNotizAndAnlass.minusDays(1)
        ).stream().map(Kind::getId).toList();

        List<ID<Kind>> maualNotDeleteWithNewishAnlass = createKinder(
            3,
            cutoffGeburtsdatum.minusDays(1), null, true,
            null, cutoffNotizAndAnlass
        ).stream().map(Kind::getId).toList();

        // Act
        List<ID<Kind>> kinderToDeactivate = kindDeactivationService.findKinderToDeactivate(
            UNUSED_FOR_MOTHS, INACTIVATION_AGE,
            DEFAULT_BATCHSIZE_FOR_TESTING);

        // Assert
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
            createKinder(3, cutoffGeburtsdatum.minusYears(2), UUID.randomUUID().toString(), false, null, null)
                .stream().map(Kind::getId).toList();

        // keep kinder that are still active in escada (age etc is irrelevant)
        List<ID<Kind>> externalKinderToKeep =
            createKinder(3, cutoffGeburtsdatum.minusYears(50), UUID.randomUUID().toString(), true, null, null)
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
            4, cutoffGeburtsdatum,
            UUID.randomUUID().toString(), false, cutoffNotizAndAnlass, null)
            .stream().map(Kind::getId).toList();

        // keep kinder even if they are inaktiv in escada if they have a current notiz
        List<ID<Kind>> externalKinderWithNewishNotiz = createKinder(
            1, cutoffGeburtsdatum.minusYears(50),
            UUID.randomUUID().toString(), false, cutoffNotizAndAnlass.plusDays(1), null)
            .stream().map(Kind::getId).toList();

        //ACT
        List<ID<Kind>> foundKinderToDeactivate = kindDeactivationService
            .findKinderToDeactivate(UNUSED_FOR_MOTHS, INACTIVATION_AGE, DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        Assertions.assertThat(foundKinderToDeactivate)
            .containsAll(externalKinderToDeactivate)
            .doesNotContainAnyElementsOf(externalKinderWithNewishNotiz);
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
            2, cutoffGeburtsdatum,
            UUID.randomUUID().toString(), false, null, cutoffNotizAndAnlass.minusDays(1))
            .stream().map(Kind::getId).toList();

        // keep kinder even if they are inaktiv in escada if they have a current notiz
        List<ID<Kind>> externalKinderWithNewishAnlass = createKinder(
            3, cutoffGeburtsdatum.minusYears(50),
            UUID.randomUUID().toString(), false, null, cutoffNotizAndAnlass)
            .stream().map(Kind::getId).toList();

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
            6, cutoffGeburtsdatum,
            UUID.randomUUID().toString(), false, cutoffNotizAndAnlass, cutoffNotizAndAnlass.minusDays(1));

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
            6, cutoffGeburtsdatum,
            UUID.randomUUID().toString(), false, cutoffNotizAndAnlass, cutoffNotizAndAnlass.minusDays(1));
        Kind deactivatedKind = kinder.getFirst();
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

    private List<Kind> createKinder(
        int count,
        @NonNull LocalDate geburtsdatum, // to check if a manual kind is to be deleted
        @Nullable String externalId, // to determine if kind is manually added or through escada
        @Nullable Boolean externalAktiv, // null if not escada
        @Nullable LocalDate lastNotizDate,
        @Nullable LocalDate lastAnlassDate
    ) {
        List<Kind> createdKinder = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Kind k =
                createKindWithAnlassAndNotiz(geburtsdatum, externalId, externalAktiv, lastNotizDate, lastAnlassDate);
            createdKinder.add(k);
        }
        return createdKinder;
    }

    private Kind createKindWithAnlassAndNotiz(
        @NonNull LocalDate geburtsdatum,
        @Nullable String externalId,
        @Nullable Boolean externalAktiv,
        @Nullable LocalDate lastNotizDate,
        @Nullable LocalDate lastAnlassDate
    ) {
        Klasse klasse = fixtureService.createKlasse();
        Kind kind;
        if (externalAktiv != null && externalId != null ) {
            boolean primitiveExternalAktiv = externalAktiv;
            kind = KindBuilder.create(builder -> builder
                .withKlasse(klasse)
                .withGeburtsdatum(geburtsdatum)
                .withExternalImportInfo(externalId, primitiveExternalAktiv, getAppClock().offsetDateTime().now())
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
                fixtureService.createNotiz(dossier, Set.of());
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
