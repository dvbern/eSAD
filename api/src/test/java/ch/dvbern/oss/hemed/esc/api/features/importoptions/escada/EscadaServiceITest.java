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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.HasExternalImportInfo;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseService;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv.EscadaCsvDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.json.EscadaImportResultJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.external.KontaktPersonKindExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.DummySchulenIds;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.LehrpersonService;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external.LehrpersonExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternal;
import ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal.SchuleExternalRepo;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportMode;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportStatus;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEscadaBS;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromPath;
import static ch.dvbern.oss.hemed.esc.api.features.schule.DummySchulenIds.isDummySchule;
import static ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportMode.CREATE;
import static ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportMode.DELETE;
import static ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportMode.UPDATE;
import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isInaktiv;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class EscadaServiceITest extends AbstractITest {

    private final EscadaService escadaService;
    private final DatabaseService databaseService;

    private final SchuleService schuleService;
    private final SchuleExternalRepo schuleExternalRepo;

    private final KlasseService klasseService;
    private final KlasseExternalRepo klasseExternalRepo;

    private final LehrpersonService lehrpersonService;
    private final LehrpersonExternalRepo lehrpersonExternalRepo;

    private final KontaktPersonKindService kontaktPersonKindService;
    private final KontaktPersonKindExternalRepo kontaktPersonKindExternalRepo;

    private final KindService kindService;
    private final KindExternalRepo kindExternalRepo;

    private final FixtureService fixtureService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        resetDatabase();
    }

    @AfterAll
    @ActivateRequestContext
    void afterAll() {
        resetDatabase();
    }

    private void resetDatabase() {
        databaseService.resetAndReinitDatabasePreservingHibernateSequenceValue(TenantEnum.BS);
    }

    @Test
    @TestAsEscadaBS
    @Order(10)
    void testSchuleUpload_should_be_runnable_multiple_times() {
        String fileImport = "/EscadaTest/schulen/csv_schulen_1.csv";
        String fileUpdateAndDelete = "/EscadaTest/schulen/csv_schulen_2.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);
        File secondFile = TestUtil.loadTestfile(fileUpdateAndDelete);

        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Schule>> resultFirstImport = escadaService.extractAndSyncSchulen(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(7);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        // run first import again
        var noopRun = escadaService.extractAndSyncSchulen(escadaCsvDTOS);
        Assertions.assertThat(noopRun).isNotEmpty().hasSize(7);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == UPDATE);

        // second import (should update some schulen)
        // ARRANGE
        Schule schuleBeforeUpdate = schuleService.getByExternalId("57"); // should be Margrethen
        Assertions.assertThat(schuleBeforeUpdate).isNotNull()
            .returns("Primarstufe Margarethen", Schule::getSchulstandort)
            .returns("Margarethen", Schule::getSchulhaus)
            .returns("Volksschulen", Schule::getSchulkreis)
            .returns("57", Schule::getExternalSystemIdOrNull);
        assertExternalImportInfo(schuleBeforeUpdate, "57", true, this.getAppClock().offsetDateTime().now());
        fixtureService.setSchuleAktivStatus(
            schuleBeforeUpdate.getId(),
            EntityAktivStatus.INAKTIV); // set inaktiv to test if update will activate it again

        // ACT
        resetTime();
        Reader reader2 = createReaderFromPath(Path.of(secondFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);

        // ASSERT
        List<ItemSyncResultJson<Schule>> resultSecondImport = escadaService.extractAndSyncSchulen(escadaCsvDTOS2);
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(8);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(CREATE, UPDATE, UPDATE, UPDATE, UPDATE, DELETE, DELETE, DELETE);

        // check if the schule was updated
        SchuleExternal schuleExtAfterUpdate = schuleExternalRepo.getExternalSchuleForSchule(schuleBeforeUpdate.getId());
        Assertions.assertThat(schuleExtAfterUpdate).isNotNull()
            .returns("Primarstufe Foo", SchuleExternal::getSchulanlageBezeichnung)
            .returns("Foothen", SchuleExternal::getSchulhausBezeichnung)
            .returns("Volksschulen", SchuleExternal::getSchulkreisBezeichnung)
            .returns("57", SchuleExternal::getSchulhausId);
        Schule schuleAfterUpdate = schuleExtAfterUpdate.getSchule();
        Assertions.assertThat(schuleAfterUpdate).isNotNull()
            .returns("Primarstufe Foo", Schule::getSchulstandort)
            .returns("Foothen", Schule::getSchulhaus)
            .returns("Volksschulen", Schule::getSchulkreis)
            .returns("57", Schule::getExternalSystemIdOrNull)
            .returns(schuleBeforeUpdate.getId(), Schule::getId);
        assertExternalImportInfo(schuleAfterUpdate, "57", false, this.getAppClock().offsetDateTime().now());
        Assertions.assertThat(schuleAfterUpdate.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);

        // check that for deleted Schulen the SchuleExternal is removed and the Schule is set to INAKTIV
        Optional<Schule> psBurgerstrasse = schuleService.findByExternalId("444");
        Assertions.assertThat(psBurgerstrasse).isPresent();
        Assertions.assertThat(psBurgerstrasse.get().getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        Assertions.assertThat(schuleExternalRepo.findExternalSchuleForSchule(psBurgerstrasse.get().getId())).isEmpty();

        Optional<Schule> psBrunmatt = schuleService.findByExternalId("337");
        Assertions.assertThat(psBrunmatt).isPresent();
        Assertions.assertThat(psBrunmatt.get().getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        Assertions.assertThat(schuleExternalRepo.findExternalSchuleForSchule(psBrunmatt.get().getId())).isEmpty();

        Optional<Schule> psErlenmatt = schuleService.findByExternalId("404");
        Assertions.assertThat(psErlenmatt).isPresent();
        Assertions.assertThat(psErlenmatt.get().getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        Assertions.assertThat(schuleExternalRepo.findExternalSchuleForSchule(psErlenmatt.get().getId())).isEmpty();
    }

    @Test
    @TestAsEscadaBS
    @Order(11)
    void testSchuleUpload_should_set_timestampFirstImport_even_if_unset() {
        // ARRANGE
        String fileImport = "/EscadaTest/schulen/csv_schulen_1.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);

        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        escadaService.extractAndSyncSchulen(escadaCsvDTOS);
        Schule schule = schuleService.getByExternalId("57"); // should be Margrethen
        fixtureService.setTimestampFirstImportOfSchule(schule.getId(), null);
        Assertions.assertThat(schuleService.getByExternalId("57").getExternalImportInfo()).isNotNull()
            .returns(
                null,
                externalImportInfo -> externalImportInfo != null ? externalImportInfo.getTimestampFirstImport() : null);

        // ACT - run import again to update the timestamp
        resetTime();
        escadaService.extractAndSyncSchulen(escadaCsvDTOS);

        Schule schuleAfterUpdate = schuleService.getByExternalId("57");
        assertExternalImportInfo(schuleAfterUpdate, "57", true, this.getAppClock().offsetDateTime().now());
    }

    @Test
    @TestAsEscadaBS
    @Order(20)
    @SuppressWarnings("java:S5961" /* Keep number of assertions */)
    void testKlassenImportShouldBeRunnableMultipleTimes() {
        importSchulenBasedataIntoEmptyDB("/EscadaTest/klassen/csv_klassen_1.csv");

        String fileImport = "/EscadaTest/klassen/csv_klassen_1.csv";
        String fileUpdateAndDelete = "/EscadaTest/klassen/csv_klassen_2.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);
        File secondFile = TestUtil.loadTestfile(fileUpdateAndDelete);

        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Klasse>> resultFirstImport = escadaService.extractAndSyncKlassen(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(13);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // check that some klassen were assigned to dummyschulen
        Assertions.assertThat(klasseService.getByExternalId("12006223").getSchule())
            .returns("Berufsfachschule Basel", Schule::getSchulstandort);
        Assertions.assertThat(klasseService.getByExternalId("11991832").getSchule())
            .returns("Gymnasium am Münsterplatz", Schule::getSchulstandort);
        Assertions.assertThat(klasseService.getByExternalId("11987712").getSchule())
            .returns("Berufsfachschule Basel", Schule::getSchulstandort);
        Assertions.assertThat(klasseService.getByExternalId("12013151").getSchule())
            .returns("Zentrum für Brückenangebote", Schule::getSchulstandort);

        // run first import again
        var noopRun = escadaService.extractAndSyncKlassen(escadaCsvDTOS);
        Assertions.assertThat(noopRun).isNotEmpty().hasSize(13);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == UPDATE);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // second import (should update some klassen)
        // ARRANGE
        Klasse klasseBeforeUpdate = klasseService.getByExternalId("11962244"); // should be Brunmatt
        Assertions.assertThat(klasseBeforeUpdate).isNotNull()
            .returns("PS  BM BRUNN 4B", Klasse::getBezeichnung)
            .returns(Klassenstufe.PRIMAR_KLASSE_4, Klasse::getKlassenstufe)
            .returns("Brunnmatt (PS)", k -> k.getSchule().getSchulhaus())
            .returns("11962244", Klasse::getExternalSystemIdOrNull)
            .returns("RK", Klasse::getKlasseSchulart);
        assertExternalImportInfo(klasseBeforeUpdate, "11962244", true, this.getAppClock().offsetDateTime().now());

        // ACT
        this.resetTime(); // advance time
        Reader reader2 = createReaderFromPath(Path.of(secondFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);
        List<ItemSyncResultJson<Klasse>> resultSecondImport = escadaService.extractAndSyncKlassen(escadaCsvDTOS2);

        // ASSERT
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(14);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(
                CREATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                DELETE,
                DELETE,
                DELETE
            );

        KlasseExternal extKlasseAfterUpdate =
            klasseExternalRepo.getExternalKlasseForKlasse(klasseBeforeUpdate.getId());

        Assertions.assertThat(extKlasseAfterUpdate).isNotNull()
            .returns("KG  BM BRUNN 4B", KlasseExternal::getKlasseBezeichnung)
            .returns("Kindergarten", KlasseExternal::getKlasseStufe)
            .returns("KG", KlasseExternal::getKlasseSchulart)
            .returns("11962244", KlasseExternal::getKlasseId)
            .returns("2", KlasseExternal::getKlasseProgrammjahr)
            .returns("337", KlasseExternal::getEscadaSchuleId);
        // now check that the Klasse in our model was updated as well
        Klasse klasseAfterUpdate = extKlasseAfterUpdate.getKlasse();

        Assertions.assertThat(klasseAfterUpdate)
            .isNotNull()
            .returns("KG  BM BRUNN 4B", Klasse::getBezeichnung)
            .returns(Klassenstufe.KINDERGARTEN_2, Klasse::getKlassenstufe)
            .returns("Brunnmatt (PS)", k -> k.getSchule().getSchulhaus())
            .returns("11962244", Klasse::getExternalSystemIdOrNull)
            .returns("KG", Klasse::getKlasseSchulart)
            .returns(klasseBeforeUpdate.getId(), Klasse::getId);
        assertExternalImportInfo(klasseAfterUpdate, "11962244", false, this.getAppClock().offsetDateTime().now());

        // check that the deleted klasse is  set to inaktiv
        Optional<Klasse> kgWeissOpt = klasseService.findByExternalId("11994011");
        Assertions.assertThat(kgWeissOpt).isPresent();
        Assertions.assertThat(klasseExternalRepo.findExternalKlasseForKlasse(kgWeissOpt.get().getId())).isEmpty();
        Assertions.assertThat(kgWeissOpt.get().getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);

        Optional<Klasse> sek = klasseService.findByExternalId("12013151");
        Assertions.assertThat(sek).isPresent();
        Assertions.assertThat(klasseExternalRepo.findExternalKlasseForKlasse(sek.get().getId())).isEmpty();
        Assertions.assertThat(sek.get().getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);

        Optional<Klasse> vk = klasseService.findByExternalId("12005018");
        Assertions.assertThat(vk).isPresent();
        Assertions.assertThat(klasseExternalRepo.findExternalKlasseForKlasse(vk.get().getId())).isEmpty();
        Assertions.assertThat(sek.get().getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);

    }

    @Test
    @TestAsEscadaBS
    @Order(30)
    void testLehrpersonImportShouldBeRunnableMultipleTimes() {

        // ARRANGE
        this.importSchulenBasedataIntoEmptyDB("/EscadaTest/lehrperson/csv_lehrperson_1.csv");
        this.importKlassenBasedataIntoEmptyDB("/EscadaTest/lehrperson/csv_lehrperson_1.csv");

        String fileImport = "/EscadaTest/lehrperson/csv_lehrperson_1.csv";
        String fileUpdateAndDelete = "/EscadaTest/lehrperson/csv_lehrperson_2.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);
        File secondFile = TestUtil.loadTestfile(fileUpdateAndDelete);

        // ACT
        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Lehrperson>> resultFirstImport =
            escadaService.extractAndSyncLehrpersonen(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(18);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // run first import again
        var noopRun = escadaService.extractAndSyncLehrpersonen(escadaCsvDTOS);
        Assertions.assertThat(noopRun).isNotEmpty().hasSize(18);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == UPDATE);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // Check that Lehrer (387439) has one Klasse (11994011), we will add a second later
        String multiklasseLehrpersonExternalId = "387439";
        assertLehrpersonHasKlassenExactly(multiklasseLehrpersonExternalId, "11994011");
        // check that Lehrer 163629 has 2 Klassen, will be removed from one later
        String lehrpersonLooskingKlassen = "163629";
        assertLehrpersonHasKlassenExactly(lehrpersonLooskingKlassen, "11994159", "11962244");

        // second import (should update some kontaktpersonen)
        // ARRANGE
        Lehrperson lehrpersonBeforeUpdate =
            lehrpersonService.getByExternalId("364336"); // should be Margrethen
        Assertions.assertThat(lehrpersonBeforeUpdate).isNotNull()
            .returns("Weller", Lehrperson::getName)
            .returns("Nicole", Lehrperson::getVorname)
            .returns(Geschlecht.WEIBLICH, Lehrperson::getGeschlecht)
            .returns("364336", Lehrperson::getExternalSystemIdOrNull);
        assertExternalImportInfo(lehrpersonBeforeUpdate, "364336", true, this.getAppClock().offsetDateTime().now());

        // ACT
        this.resetTime(); // advance time
        Reader reader2 = createReaderFromPath(Path.of(secondFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);
        var resultSecondImport =
            escadaService.extractAndSyncLehrpersonen(escadaCsvDTOS2);
        // ASSERT
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(19);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(
                CREATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                DELETE,
                DELETE);

        LehrpersonExternal lehrpersonExtAfterUpdate =
            lehrpersonExternalRepo.getExternalForLehrperson(lehrpersonBeforeUpdate.getId());

        Assertions.assertThat(lehrpersonExtAfterUpdate).isNotNull()
            .returns("Romanov", LehrpersonExternal::getKlasseLehrpersonName)
            .returns("Marius", LehrpersonExternal::getKlasseLehrpersonVorname)
            .returns("Herr", LehrpersonExternal::getKlasseLehrpersonAnrede);

        // now check that the Lehrperson in our model was updated as well
        Lehrperson lehrpersonAfterUpdate = lehrpersonExtAfterUpdate.getLehrperson();
        Assertions.assertThat(lehrpersonAfterUpdate).isNotNull()
            .returns("Romanov", Lehrperson::getName)
            .returns("Marius", Lehrperson::getVorname)
            .returns(Geschlecht.MAENNLICH, Lehrperson::getGeschlecht)
            .returns("364336", Lehrperson::getExternalSystemIdOrNull)
            .returns(lehrpersonBeforeUpdate.getId(), Lehrperson::getId);
        assertExternalImportInfo(lehrpersonAfterUpdate, "364336", false, this.getAppClock().offsetDateTime().now());

        // now check that the Lehrperson that was added to a Klasse was added to the correct Klasse
        // Check that Lehrer (387439) has one Klasse (11994011), we will add a second later
        assertLehrpersonHasKlassenExactly(multiklasseLehrpersonExternalId, "11994011", "12009662");
        assertLehrpersonHasKlassenExactly(lehrpersonLooskingKlassen, "11962244");
    }

    @Test
    @TestAsEscadaBS
    @Order(40)
    @SuppressWarnings("java:S5961" /* Keep number of assertions */)
    void testKinderImportShouldBeRunnableMultipleTimes() {

        // ARRANGE
        this.importSchulenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_1.csv");
        this.importKlassenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_1.csv");
        this.importLehrpersonenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_1.csv");

        String fileImport = "/EscadaTest/kinder/csv_kinder_1.csv";
        String fileUpdateAndDelete = "/EscadaTest/kinder/csv_kinder_2.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);
        File secondFile = TestUtil.loadTestfile(fileUpdateAndDelete);

        // ACT
        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Kind>> resultFirstImport =
            escadaService.extractAndSyncKinder(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(11);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // run first import again
        List<ItemSyncResultJson<Kind>> noopRun = escadaService.extractAndSyncKinder(escadaCsvDTOS);
        Assertions.assertThat(noopRun).isNotEmpty().hasSize(11);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == UPDATE);
        Assertions.assertThat(noopRun).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // manually change a field to prevent future updates from escada (we do not update values that were chagned
        // manually)
        Kind kindWithManualEmail = kindService.getByExternalId("412023");
        fixtureService.changeEmailOfKind(
            kindWithManualEmail.getId(),
            EmailAddress.fromString("mailwillnotupdate@mailbucket.dvbern.ch"));

        // second import (should update some kinder)
        // ARRANGE
        Kind kindBeforeUpdate = kindService.getByExternalId("222538");
        Assertions.assertThat(kindBeforeUpdate).isNotNull()
            .returns("Schuster", Kind::getName)
            .returns("Tomislav", Kind::getVorname)
            .returns(LocalDate.of(2018, 5, 28), Kind::getGeburtsdatum)
            .returns(Land.ITALIEN, Kind::getNationalitaet)
            .returns(NormalizedPhoneNumber.parse("075 430 88 77"), Kind::getTelefon)
            .returns(null, Kind::getEmail)
            .returns(new SozialversicherungsNummer("756.7301.4699.72"), Kind::getSozialversicherungsNummer)
            .returns("Italienisch", Kind::getErstsprache)
            .returns(Geschlecht.MAENNLICH, Kind::getGeschlecht)
            .returns("222538", Kind::getExternalSystemIdOrNull)
            .extracting(Kind::getAdresse).isNotNull()
            .returns("Inzlingerstrasse 270c", a -> a != null ? a.getStrasseUndNr() : null)
            .returns("4125", a -> a != null ? a.getPlz() : null)
            .returns("Riehen", a -> a != null ? a.getOrt() : null)
            .returns(Land.SCHWEIZ, a -> a != null ? a.getLand() : null);
        assertExternalImportInfo(kindBeforeUpdate, "222538", true, this.getAppClock().offsetDateTime().now());

        Assertions.assertThat(kindBeforeUpdate.getKlasse()).isNotNull()
            .returns("11994011", k -> k != null ? k.getExternalSystemIdOrNull() : null)
            .returns("KG  BU WEISS 2", k -> k != null ? k.getBezeichnung() : null);

        // ACT
        resetTime(); // advance time
        Reader reader2 = createReaderFromPath(Path.of(secondFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);
        var resultSecondImport =
            escadaService.extractAndSyncKinder(escadaCsvDTOS2);
        // ASSERT
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(13);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(
                CREATE,
                CREATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                DELETE
            );

        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getStatus)
            .filteredOn(status -> status == ImportStatus.OK)
            .hasSize(13);
        // check that invalid ahvnr was imported as null
        Assertions.assertThat(kindService.getByExternalId("666667")).returns(null, Kind::getSozialversicherungsNummer);

        Kind kindAfterUpdate = kindService.getByExternalId("222538");
        Assertions.assertThat(kindAfterUpdate).isNotNull()
            .returns("Changeo", Kind::getName)
            .returns("Tomislavo", Kind::getVorname)
            .returns(LocalDate.of(2018, 6, 29), Kind::getGeburtsdatum)
            .returns(Land.FRANKREICH, Kind::getNationalitaet)
            .returns(NormalizedPhoneNumber.parse("075 430 88 66"), Kind::getTelefon)
            .returns(EmailAddress.fromString("newmail@mailbucket.dvbern.ch"), Kind::getEmail)
            .returns(new SozialversicherungsNummer("756.7836.9843.84"), Kind::getSozialversicherungsNummer)
            .returns("Französisch", Kind::getErstsprache)
            .returns(Geschlecht.WEIBLICH, Kind::getGeschlecht)
            .returns("222538", Kind::getExternalSystemIdOrNull)
            .extracting(Kind::getAdresse).isNotNull()
            .returns("Changestrasse 1", a -> a != null ? a.getStrasseUndNr() : null)
            .returns("4126", a -> a != null ? a.getPlz() : null)
            .returns("Riehen BS", a -> a != null ? a.getOrt() : null)
            .returns(Land.SCHWEIZ, a -> a != null ? a.getLand() : null);

        Assertions.assertThat(kindAfterUpdate.getKlasse()).isNotNull()
            .returns("11976367", k -> k != null ? k.getExternalSystemIdOrNull() : null)
            .returns("Sek DW DEWET 3t", k -> k != null ? k.getBezeichnung() : null);

        KindExternal extKindAfterUpdate = kindExternalRepo.getExternalKindForKind(kindAfterUpdate.getId());
        Assertions.assertThat(extKindAfterUpdate).isNotNull()
            .returns("222538", KindExternal::getSchuelerId)
            .returns("Eltern", KindExternal::getWohntyp)
            .returns("Changeo", KindExternal::getSchuelerName)
            .returns("Tomislavo", KindExternal::getSchuelerVorname)
            .returns("2018-06-29T00:00:00", KindExternal::getSchuelerGeburtsdatum)
            .returns("2", KindExternal::getSchuelerGeschlecht)
            .returns("756.7836.9843.84", KindExternal::getSchuelerSozialversicherung)
            .returns("Changestrasse 1", KindExternal::getSchuelerAdresse)
            .returns(null, KindExternal::getSchuelerAdresse1)
            .returns(null, KindExternal::getSchuelerAdresse2)
            .returns("4126", KindExternal::getSchuelerPLZ)
            .returns("Riehen BS", KindExternal::getSchuelerOrt)
            .returns("075 430 88 66", KindExternal::getSchuelerMobile)
            .returns("newmail@mailbucket.dvbern.ch", KindExternal::getSchuelerEMail)
            .returns("Frankreich", KindExternal::getSchuelerNationalitaet)
            .returns("FR", KindExternal::getSchuelerStaatcodeISO)
            .returns("Italienisch", KindExternal::getSchuelerSprache)
            .returns("Französisch", KindExternal::getSchuelerErstsprache)
            .returns("Frankreich", KindExternal::getSchuelerNationalitaet);
        assertExternalImportInfo(kindAfterUpdate, "222538", false, this.getAppClock().offsetDateTime().now());

        // check that Kind 509389 was marked as not aktiv
        Kind kindNotAktiv = kindService.getByExternalId("509389");
        Assertions.assertThat(kindNotAktiv)
            .isNotNull()
            .returns("509389", Kind::getExternalSystemIdOrNull);

        // check that the manually changed field was not updated
        Kind kindWithManualEmailAfterUpdate = kindService.getByExternalId("412023");
        Assertions.assertThat(kindWithManualEmailAfterUpdate.getEmail())
            .describedAs("Email should not have been updated because it was changed manually")
            .isEqualTo(EmailAddress.fromString("mailwillnotupdate@mailbucket.dvbern.ch"));
        Assertions.assertThat(kindExternalRepo.getExternalKindForKind(kindWithManualEmailAfterUpdate.getId())
                .getSchuelerEMail())
            .describedAs("Email should not have been updated because it was changed manually")
            .isEqualTo("csilla.marti@stud.edu.ch");
    }

    @Test
    @TestAsAdminBS
    @Order(45)
    void extractUniqueExternalKinder() {
        // this test should check if Kinder entries that have multiple Klassen are checked for one of the Klassen being
        // the Schule fuer Gestaltung, if that is the case SfG entry should be removed.
        // However if the SgF entry is the only klasse then it should be kept

        // we have one entry that is in 2 klassen where one of them is sfg
        // we have an entry that is sfg only

        // ARRANGE
        this.importSchulenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kontaktpersonkind_with_schule_fuer_gest.csv");
        this.importKlassenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kontaktpersonkind_with_schule_fuer_gest.csv");
        this.importLehrpersonenBasedataIntoEmptyDB(
            "/EscadaTest/kinder/csv_kontaktpersonkind_with_schule_fuer_gest.csv");

        String fileImport = "/EscadaTest/kinder/csv_kontaktpersonkind_with_schule_fuer_gest.csv";
        File firstFile = TestUtil.loadTestfile(fileImport);

        // ACT
        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Kind>> resultFirstImport =
            escadaService.extractAndSyncKinder(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(8);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // check the 2 kinder that should be in schule fuer gestaltung
        Kind kindSgf1 = kindService.getByExternalId("528310");
        Assertions.assertThat(kindSgf1)
            .extracting(Kind::getSchule)
            .returns(DummySchulenIds.SCHULE_FUER_GESTALTUNG, s -> s != null ? s.getId() : null);

        Kind kindSgf2 = kindService.getByExternalId("442859");
        Assertions.assertThat(kindSgf2)
            .extracting(Kind::getSchule)
            .returns(DummySchulenIds.SCHULE_FUER_GESTALTUNG, s -> s != null ? s.getId() : null);

        Kind kindNotSgf = kindService.getByExternalId("307324");
        Assertions.assertThat(kindNotSgf)
            .extracting(Kind::getSchule)
            .returns(
                DummySchulenIds.ALLGEMEINE_GEWERBESCHULE_BASEL,
                s -> s != null ? s.getId() : null);
    }

    @Test
    @TestAsEscadaBS
    @Order(50)
    @SuppressWarnings("java:S5961" /* Keep number of assertions */)
    void testKontaktpersonKindImportShouldBeRunnableMultipleTimes() {

        // ARRANGE
        this.importSchulenBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        this.importKlassenBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        this.importKinderBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");

        String fileImport = "/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv";
        String fileUpdateAndDelete = "/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_2.csv";

        File firstFile = TestUtil.loadTestfile(fileImport);
        File secondFile = TestUtil.loadTestfile(fileUpdateAndDelete);

        // ACT
        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<KontaktPersonKind>> resultFirstImport =
            escadaService.extractAndSyncKontaktPersonKind(escadaCsvDTOS);

        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(17);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == CREATE);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // run first import again
        List<ItemSyncResultJson<KontaktPersonKind>> resultFirstImportSecondRun =
            escadaService.extractAndSyncKontaktPersonKind(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImportSecondRun).isNotEmpty().hasSize(17);
        Assertions.assertThat(resultFirstImportSecondRun).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .allMatch(mode -> mode == UPDATE);
        Assertions.assertThat(resultFirstImportSecondRun).extracting(ItemSyncResultJson::getStatus)
            .allMatch(status -> status == ImportStatus.OK);

        // second import (should update some kontaktpersonen)
        // ARRANGE
        KontaktPersonKind kontaktPersonBeforeUpdate =
            kontaktPersonKindService.getByExternalId("325925"); //should be Stocker
        Assertions.assertThat(kontaktPersonBeforeUpdate).isNotNull()
            .returns("Stocker", KontaktPersonKind::getName)
            .returns("Pascal", KontaktPersonKind::getVorname)
            .returns(Geschlecht.MAENNLICH, KontaktPersonKind::getGeschlecht)
            .returns(new NormalizedPhoneNumber("+41792977784"), KontaktPersonKind::getTelefon)
            .returns("325925", KontaktPersonKind::getExternalSystemIdOrNull);
        assertExternalImportInfo(kontaktPersonBeforeUpdate, "325925", true, this.getAppClock().offsetDateTime().now());

        // the Eltern (568509 und 568508) should have 2 Kinder '222568', '222538';
        String externalIdElternperson1 = "568508";
        String externalIdElternperson2 = "568509";
        assertElternHaveKinder(externalIdElternperson1, "222568", "222538");
        assertElternHaveKinder(externalIdElternperson2, "222568", "222538");

        // ACT
        this.resetTime(); // advance time
        Reader reader2 = createReaderFromPath(Path.of(secondFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);
        List<ItemSyncResultJson<KontaktPersonKind>> resultSecondImport =
            escadaService.extractAndSyncKontaktPersonKind(escadaCsvDTOS2);
        // ASSERT
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(23);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(
                CREATE,
                CREATE,
                CREATE,
                CREATE,
                CREATE,
                CREATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                UPDATE,
                DELETE,
                DELETE,
                DELETE,
                DELETE,
                DELETE);

        KontaktPersonKindExternal kpKindExternalAfterUpdate =
            kontaktPersonKindExternalRepo.getExternalForKontaktPerson(kontaktPersonBeforeUpdate.getId());

        Assertions.assertThat(kpKindExternalAfterUpdate).isNotNull()
            .returns("Stocker", KontaktPersonKindExternal::getAdressierungName)
            .returns("Fritz", KontaktPersonKindExternal::getAdressierungVorname)
            .returns("Herr", KontaktPersonKindExternal::getAdressierungAnrede)
            .returns("079 297 77 85", KontaktPersonKindExternal::getAdressierungNatel);

        // now check that the Kontaktperson in our model was updated as well
        KontaktPersonKind kpKindAfterUpdate = kpKindExternalAfterUpdate.getKontaktPersonKind();
        Assertions.assertThat(kpKindAfterUpdate).isNotNull()
            .returns("Stocker", KontaktPersonKind::getName)
            .returns("Fritz", KontaktPersonKind::getVorname)
            .returns(Geschlecht.MAENNLICH, KontaktPersonKind::getGeschlecht)
            .returns("325925", KontaktPersonKind::getExternalSystemIdOrNull)
            .returns(new NormalizedPhoneNumber("079 297 77 85"), KontaktPersonKind::getTelefon)
            .returns(kontaktPersonBeforeUpdate.getId(), KontaktPersonKind::getId);
        assertExternalImportInfo(kpKindAfterUpdate, "325925", false, this.getAppClock().offsetDateTime().now());

        // now check the Eltern are assigned to the correct Kinder
        assertElternHaveKinder("777777", "222538");
        // elterperson 568509  nicht mehr bei Kind '222538';
        assertElternHaveKinder(externalIdElternperson2, "222568");
        assertElternHaveKinder(externalIdElternperson1, "222568", "222538");
    }

    @Test
    @TestAsEscadaBS
    @Order(60)
    @SuppressWarnings("java:S5961" /* Keep number of assertions */)
    void testDeactivateAndReactivate() {
        // ARRANGE
        // make sure there are some kinder to deaktivieren
        var schulenRes =
            this.importSchulenBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        var klassenRes =
            this.importKlassenBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        var lehrerRes =
            this.importLehrpersonenBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        var kinderRes =
            this.importKinderBasedataIntoEmptyDB("/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");
        var elternRes = this.importKontaktpersonkindKinderBasedataIntoEmptyDB(
            "/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1.csv");

        // ACT IMPORT EMPTY
        FileUpload minimalCSV =
            TestUtil.fileToFileUpload(TestUtil.loadTestfile("/EscadaTest/csv_escada_minimal.csv"), "minimal-csv.csv");

        // import a completly empty csv
        EscadaImportResultJson escadaImportResultJson = escadaService.processEscadaCsvUpload(minimalCSV);
        // ASSERT
        Assertions.assertThat(escadaImportResultJson).isNotNull();
        assertAllItemsHaveMode(escadaImportResultJson.getSchuleImportResultJsons(), schulenRes, DELETE);
        assertAllItemsHaveMode(escadaImportResultJson.getKlasseImportResultJsons(), klassenRes, DELETE);
        assertAllItemsHaveMode(escadaImportResultJson.getLehrpersonResultJsons(), lehrerRes, DELETE);
        assertAllItemsHaveMode(escadaImportResultJson.getKindImportResultJsons(), kinderRes, DELETE);
        assertAllItemsHaveMode(escadaImportResultJson.getKontaktPersonenKindImportResultJsons(), elternRes, DELETE);

        // check that the Kinder are deactivated
        escadaImportResultJson.getKindImportResultJsons().stream().map(ItemSyncResultJson::getExternalId)
            .forEach(externalId -> {
                Kind kind = kindService.getByExternalId(externalId);
                Assertions.assertThat(kind).isNotNull();
                // Beim Deaktivieren sollten die Kinder keine Klasse und keine Schule mehr haben
                Assertions.assertThat(kind.getKlasse()).isNull();
                // if schule is not dummy schule
                Assertions.assertThat(kind.getSchule()).satisfiesAnyOf(
                    schule -> Assertions.assertThat(schule).isNull(),
                    schule -> Assertions.assertThat(schule)
                        .isNotNull()
                        .extracting(s -> Objects.requireNonNull(s).getExternalSystemIdOrNull())
                        .isNull()
                );
            });

        // check that non-dummy-Schulen and Klassen are deactivated
        var deactSchulen = schuleService.findAll().stream().filter(s -> !isDummySchule(s.getId()));
        Assertions.assertThat(deactSchulen).isNotEmpty()
            .allMatch(schule -> isInaktiv(schule.getSchuleAktivStatus()));
        var deactKlassen = klasseService.findAll();
        Assertions.assertThat(deactKlassen).isNotEmpty()
            .allMatch(klasse -> isInaktiv(klasse.getKlasseAktivStatus()));

        // REIMPORT
        // import a full csv, should reactivate disabled entities
        FileUpload fullCsv = TestUtil.fileToFileUpload(
            TestUtil.loadTestfile(
                "/EscadaTest/kontaktpersonkind/csv_kontaktpersonkind_1_b_afterdeletion.csv"),
            "full-csv.csv");
        escadaImportResultJson = escadaService.processEscadaCsvUpload(fullCsv);
        assertAllItemsHaveMode(
            escadaImportResultJson.getSchuleImportResultJsons(),
            schulenRes,
            CREATE);// should want to create them
        assertAllItemsHaveMode(escadaImportResultJson.getKlasseImportResultJsons(), klassenRes, CREATE);
        assertAllItemsHaveMode(escadaImportResultJson.getLehrpersonResultJsons(), lehrerRes, CREATE);
        assertAllItemsHaveMode(escadaImportResultJson.getKindImportResultJsons(), kinderRes, CREATE);
        assertAllItemsHaveMode(escadaImportResultJson.getKontaktPersonenKindImportResultJsons(), elternRes, CREATE);
        // should not have errors
        Assertions.assertThat(escadaImportResultJson.getKindImportResultJsons())
            .filteredOn(itemSyncResultJson -> itemSyncResultJson.getStatus() == ImportStatus.ERROR)
            .isEmpty();

        // check that schule with name only exists once
        Assertions.assertThat(schuleService.findAll()
                .stream()
                .filter(schule -> schule.getSchulhaus().equals("Erlenmatt")))
            .hasSize(1)
            .first()
            .returns(EntityAktivStatus.AKTIV, Schule::getSchuleAktivStatus)
            .returns("Upd Primarstufe Erlenmatt", Schule::getSchulstandort);

        // check that klasse exists only once
        Assertions.assertThat(klasseService.findAll()
                .stream()
                .filter(klasse -> klasse.getBezeichnung().equals("Sek DW DEWET 1l")))
            .hasSize(1)
            .first()
            .returns(EntityAktivStatus.AKTIV, Klasse::getKlasseAktivStatus)
            .returns("Upd SEKE", Klasse::getKlasseSchulart);
        // check that lehrperson exists only once because the old one was reactivated and reused
        Assertions.assertThat(lehrpersonService.findAll()
                .stream()
                .filter(lehrperson -> lehrperson.getName().equals("Yesilyayla")))
            .hasSize(1)
            .first()
            .returns("Upd Astrid", Lehrperson::getVorname);

        // check that Kind exists only once because the old one was reactivated and reused
        Assertions.assertThat(kindService.findAll()
                .stream()
                .filter(kind -> kind.getName().equals("Muliqi")))
            .hasSize(1)
            .first()
            .as("Kind Data should not be updated since we could have changed it manually")
            .returns("Beat", Kind::getVorname)
            .doesNotReturn(null, Kind::getSchule)
            .doesNotReturn(null, Kind::getKlasse);

        // check that kontaktperson kind exists only once because the old one was reactivated and reused
        Assertions.assertThat(kontaktPersonKindService.findAll()
                .stream()
                .filter(kontaktPersonKind -> kontaktPersonKind.getName().equals("Roth")))
            .hasSize(1)
            .first()
            .returns("Upd Wilhelmus", KontaktPersonKind::getVorname);

    }

    @Test
    @TestAsEscadaBS
    @Order(61)
    void testDuplicateSozialversicherungsnummer() {
        // ARRANGE
        this.importSchulenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_duplicate_vnr_1.csv");
        this.importKlassenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_duplicate_vnr_1.csv");
        this.importLehrpersonenBasedataIntoEmptyDB("/EscadaTest/kinder/csv_kinder_duplicate_vnr_1.csv");
        SozialversicherungsNummer vnrClash1 = new SozialversicherungsNummer("756.6613.7842.09");

        // Manuelles Kind mit derselben VNR erstellen, welche dann im ersten Import vorkommt
        Kind existingManualKindWithSameVnr = KindBuilder.create(builder -> builder
            .withSozialversicherungsNummer(vnrClash1));
        existingManualKindWithSameVnr = kindService.persistWithoutDuplicateCheck(existingManualKindWithSameVnr);

        // ACT
        // first import
        File file1 = TestUtil.loadTestfile("/EscadaTest/kinder/csv_kinder_duplicate_vnr_1.csv");
        Reader reader1 = createReaderFromPath(Path.of(file1.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS1 = escadaService.parseEscadaCsvAndRemoveUndesired(reader1);
        List<ItemSyncResultJson<Kind>> resultFirstImport = escadaService.extractAndSyncKinder(escadaCsvDTOS1);

        // ASSERT
        // Es sollten alle Kinder erfolgreich importiert worden sein.
        // Aber das manuelle Kind, welches bisher dieselbe VNR hatte, sollte keine VNR mehr gesetzt haben
        Assertions.assertThat(resultFirstImport).isNotEmpty().hasSize(3);
        Assertions.assertThat(resultFirstImport)
            .extracting(ItemSyncResultJson::getDeterminedImportMode).containsExactly(
                CREATE,
                CREATE,
                CREATE);
        Assertions.assertThat(resultFirstImport)
            .extracting(ItemSyncResultJson::getStatus).containsExactly(
                ImportStatus.OK,
                ImportStatus.OK,
                ImportStatus.OK);
        Kind existingManuellesKindAfterFirstImport = kindService.getById(existingManualKindWithSameVnr.getId());
        Assertions.assertThat(existingManuellesKindAfterFirstImport.getSozialversicherungsNummer())
            .describedAs("SVNR of existing kind should not have been removed")
            .isNull();

        Kind kindWithVnrClashToManualKind = kindService.getByExternalId("111111");
        Kind kindNoVnrInFirstImport = kindService.getByExternalId("333333");

        Assertions.assertThat(kindWithVnrClashToManualKind)
            .isNotEqualTo(existingManuellesKindAfterFirstImport)
            .extracting(Kind::getSozialversicherungsNummer).isEqualTo(vnrClash1);
        Assertions.assertThat(kindNoVnrInFirstImport)
            .extracting(Kind::getSozialversicherungsNummer).isNull();

        // ACT
        // ein weiteres manuelles kind erstellen, welches dann beim update einen clash verursacht
        Kind existingManualKindWithVnrOfFutureEscadaKind = KindBuilder.create(builder -> builder
            .withSozialversicherungsNummer(new SozialversicherungsNummer("756.5538.4525.07")));
        existingManualKindWithVnrOfFutureEscadaKind =
            kindService.persistWithoutDuplicateCheck(existingManualKindWithVnrOfFutureEscadaKind);
        // Zweiten Import starten
        File file2 = TestUtil.loadTestfile("/EscadaTest/kinder/csv_kinder_duplicate_vnr_2.csv");
        Reader reader2 = createReaderFromPath(Path.of(file2.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS2 = escadaService.parseEscadaCsvAndRemoveUndesired(reader2);
        List<ItemSyncResultJson<Kind>> resultSecondImport = escadaService.extractAndSyncKinder(escadaCsvDTOS2);

        // ASSERT
        // Es solle nur eines der beiden Kinder importiert worden sein, da das andere Kind bereits ein VNR-Duplikat
        // aus Escada hatte aus dem ersten Import
        Assertions.assertThat(resultSecondImport).isNotEmpty().hasSize(4);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getDeterminedImportMode)
            .containsExactly(
                CREATE, // Nr. 444444 ist CREATE/FAILED da die VNR bereits in einem Escada-Kind existiert (222222)
                UPDATE,
                UPDATE,
                UPDATE);
        Assertions.assertThat(resultSecondImport).extracting(ItemSyncResultJson::getStatus)
            .containsExactly(
                ImportStatus.ERROR,
                ImportStatus.OK,
                ImportStatus.OK,
                ImportStatus.OK);
        Kind existingManualKindWithVnrOfFutureEscadaKindAfterSecondImport =
            kindService.getById(existingManualKindWithVnrOfFutureEscadaKind.getId());
        Assertions.assertThat(existingManualKindWithVnrOfFutureEscadaKindAfterSecondImport.getSozialversicherungsNummer())
            .isNull();
    }

    private static <T extends AbstractEntity<T>> void assertAllItemsHaveMode(
        @NonNull List<ItemSyncResultJson<T>> importResult,
        @NonNull List<ItemSyncResultJson<T>> preexistingItems,
        @NonNull ImportMode importMode
    ) {
        Assertions.assertThat(importResult)
            .allMatch(schuleItemSyncResultJson -> schuleItemSyncResultJson.getDeterminedImportMode()
                == importMode)
            .extracting(ItemSyncResultJson::getExternalId)
            .containsAll(
                preexistingItems.stream().map(ItemSyncResultJson::getExternalId).toList());
    }

    @CanIgnoreReturnValue
    private List<ItemSyncResultJson<Schule>> importSchulenBasedataIntoEmptyDB(String schulenImportFile) {
        File firstFile = TestUtil.loadTestfile(schulenImportFile);
        // first import
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Schule>> resultFirstImport = escadaService.extractAndSyncSchulen(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .isNotEmpty().allMatch(status -> status == ImportStatus.OK);
        return resultFirstImport;
    }

    @CanIgnoreReturnValue
    private List<ItemSyncResultJson<Klasse>> importKlassenBasedataIntoEmptyDB(String klassenImportFile) {
        File firstFile = TestUtil.loadTestfile(klassenImportFile);
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Klasse>> resultFirstImport = escadaService.extractAndSyncKlassen(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .isNotEmpty().allMatch(status -> status == ImportStatus.OK);
        return resultFirstImport;
    }

    @CanIgnoreReturnValue
    private List<ItemSyncResultJson<Lehrperson>> importLehrpersonenBasedataIntoEmptyDB(String kpsImportFile) {
        File firstFile = TestUtil.loadTestfile(kpsImportFile);
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Lehrperson>> resultFirstImport =
            escadaService.extractAndSyncLehrpersonen(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .isNotEmpty()
            .allMatch(status -> status == ImportStatus.OK);
        return resultFirstImport;
    }

    @CanIgnoreReturnValue
    private List<ItemSyncResultJson<Kind>> importKinderBasedataIntoEmptyDB(String kindImportFile) {
        File file = TestUtil.loadTestfile(kindImportFile);
        Reader reader = createReaderFromPath(Path.of(file.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<Kind>> resultFirstImport = escadaService.extractAndSyncKinder(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .isNotEmpty()
            .allMatch(status -> status == ImportStatus.OK);
        return resultFirstImport;
    }

    @CanIgnoreReturnValue
    private List<ItemSyncResultJson<KontaktPersonKind>> importKontaktpersonkindKinderBasedataIntoEmptyDB(String elternFile) {
        File file = TestUtil.loadTestfile(elternFile);
        Reader reader = createReaderFromPath(Path.of(file.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<ItemSyncResultJson<KontaktPersonKind>> resultFirstImport =
            escadaService.extractAndSyncKontaktPersonKind(escadaCsvDTOS);
        Assertions.assertThat(resultFirstImport).extracting(ItemSyncResultJson::getStatus)
            .isNotEmpty()
            .allMatch(status -> status == ImportStatus.OK);
        return resultFirstImport;
    }

    private void assertLehrpersonHasKlassenExactly(@NonNull String externalLehrpersonId, String... externalKlassenIds) {
        Lehrperson multiKlasseLehrperson = lehrpersonService.getByExternalId(externalLehrpersonId);
        Set<Klasse> klassenForLp = fixtureService.findKlassenOfLehrperson(multiKlasseLehrperson.getId());
        Assertions.assertThat(klassenForLp).isNotEmpty().hasSize(externalKlassenIds.length);
        Assertions.assertThat(klassenForLp).extracting(Klasse::getExternalSystemIdOrNull)
            .containsAll(Arrays.stream(externalKlassenIds).toList());
    }

    private void assertElternHaveKinder(@NonNull String elternIdExtern, String... externalKlassenIds) {
        KontaktPersonKind elternPerson = kontaktPersonKindService.getByExternalId(elternIdExtern);
        Set<Kind> kinderOfElternperson = fixtureService.findKinderOfElternperson(elternPerson.getId());
        Assertions.assertThat(kinderOfElternperson).isNotNull().hasSize(externalKlassenIds.length);
        Assertions.assertThat(kinderOfElternperson)
            .extracting(Kind::getExternalSystemIdOrNull)
            .containsAll(Arrays.stream(externalKlassenIds).toList());
    }

    private static void assertExternalImportInfo(
        @NonNull HasExternalImportInfo entity,
        @NonNull String expectedIdInExternalSystem,
        boolean timestampsShouldBeEqual,
        @NonNull OffsetDateTime now) {
        var externalImportInfo = entity.getExternalImportInfo();
        Assertions.assertThat(externalImportInfo).isNotNull()
            .returns(expectedIdInExternalSystem, ExternalImportInfo::getIdInExternalSystem);
        var firstImport = externalImportInfo.getTimestampFirstImport();
        var latestImport = externalImportInfo.getTimestampLatestImport();
        Assertions.assertThat(firstImport).isNotNull();
        Assertions.assertThat(latestImport).isNotNull();

        Assertions.assertThat(firstImport).as("should not be in future").isBeforeOrEqualTo(now);
        Assertions.assertThat(latestImport).as("should not be in future").isBeforeOrEqualTo(now);

        if (timestampsShouldBeEqual) {
            Assertions.assertThat(latestImport)
                .as("Timestamps should be equal on initial import")
                .isEqualTo(firstImport);
        } else {
            Assertions.assertThat(latestImport)
                .as("getTimestampLatestImport should be after getTimestampFirstImport")
                .isAfter(firstImport);
        }
    }

}

