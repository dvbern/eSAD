/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import java.time.LocalDate;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnonymerEintragBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnonymerEintragApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassTypSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnonymerEintragCreateJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnonymerEintragJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.GeschlechtSpec;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class AnonymerEintragResourceITest extends AbstractITest {

    private final AnonymerEintragApiSpec api = AnonymerEintragApiSpec.anonymerEintrag(RequestSpecUtil.quarkusSpec());

    private final AnonymerEintragService anonymerEintragService;
    private final AnlassService anlassService;
    private final UntersuchungService untersuchungService;
    private final FixtureService fixtureService;
    private final TenantProducer tenantProducer;

    @Test
    @TestAsAdminBS
    void getAnonymerEintrag() {
        // ARRANGE
        var anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_4_ANONYM);
        AnonymerEintrag anonymerEintrag = anonymerEintragService.persist(
            AnonymerEintragBuilder.create(builder -> builder
                .withHash("hash-for-get-test")
                .withAnlass(anlass))
        );

        // ACT
        AnonymerEintragJsonSpec result = api.anonymerEintragResourceGetAnonymerEintrag()
            .anonymerEintragIdPath(anonymerEintrag.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnonymerEintragJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(anonymerEintrag.getId().getId());
        assertThat(result.getHash()).isEqualTo("hash-for-get-test");
        assertThat(result.getAnlassJson()).isNotNull();
        assertThat(result.getAnlassJson().getId()).isEqualTo(anonymerEintrag.getAnlass().getId().getId());
    }

    @Test
    @TestAsAdminBS
    void createAnonymerEintrag() {
        // ARRANGE
        LocalDate anlassDatum = LocalDate.now().plusDays(10);
        LocalDate kindGeburtsdatum = LocalDate.of(2016, 2, 12);
        String kindName = "Dummy";
        String kindVorname = "Kid";
        AnonymerEintragCreateJsonSpec createJson = new AnonymerEintragCreateJsonSpec()
            .anlassTyp(AnlassTypSpec.KLASSE_4_ANONYM)
            .anlassDatum(anlassDatum)
            .kindName(kindName)
            .kindVorname(kindVorname)
            .kindGeschlecht(GeschlechtSpec.MAENNLICH)
            .kindGeburtsdatum(kindGeburtsdatum);

        // ACT
        AnonymerEintragJsonSpec result = api.anonymerEintragResourceCreateAnonymerEintrag()
            .body(createJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnonymerEintragJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getAnlassJson()).isNotNull();
        assertThat(result.getAnlassJson().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_4_ANONYM);
        assertThat(result.getAnlassJson().getData().getDatum()).isEqualTo(anlassDatum);
        assertThat(result.getHash()).isEqualTo(
            HashValueGeneratorUtil.generateHashValueForKindValues(
                kindName,
                kindVorname,
                Geschlecht.MAENNLICH,
                kindGeburtsdatum
            )
        );
        assertThat(result.getAnlassJson().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_4_ANONYM);
    }

    @Test
    @TestAsAdminBE
    void createAnonymerEintrag_dezentral_support_not_allowed() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        LocalDate anlassDatum = LocalDate.now().plusDays(10);
        LocalDate kindGeburtsdatum = LocalDate.of(2016, 2, 12);
        String kindName = "Dummy";
        String kindVorname = "Kid";
        AnonymerEintragCreateJsonSpec createJson = new AnonymerEintragCreateJsonSpec()
            .anlassTyp(AnlassTypSpec.KLASSE_4_ANONYM)
            .anlassDatum(anlassDatum)
            .kindName(kindName)
            .kindVorname(kindVorname)
            .kindGeschlecht(GeschlechtSpec.MAENNLICH)
            .kindGeburtsdatum(kindGeburtsdatum);

        // ACT
        AppFailureErrorJsonSpec response = api.anonymerEintragResourceCreateAnonymerEintrag()
            .body(createJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract()
            .as(AppFailureErrorJsonSpec.class);

        assertThat(response.getSummary()).contains("Keine Arzt- oder Delegierte Berechtigung vorhanden.");
    }

    @Test
    @TestAsSchularztBE
    void createAnonymerEintrag_dezentral_must_be_arzt() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        LocalDate anlassDatum = LocalDate.now().plusDays(10);
        LocalDate kindGeburtsdatum = LocalDate.of(2016, 2, 12);
        String kindName = "Dummy";
        String kindVorname = "Kid";
        AnonymerEintragCreateJsonSpec createJson = new AnonymerEintragCreateJsonSpec()
            .anlassTyp(AnlassTypSpec.KLASSE_4_ANONYM)
            .anlassDatum(anlassDatum)
            .kindName(kindName)
            .kindVorname(kindVorname)
            .kindGeschlecht(GeschlechtSpec.MAENNLICH)
            .kindGeburtsdatum(kindGeburtsdatum);

        // ACT
        AnonymerEintragJsonSpec result = api.anonymerEintragResourceCreateAnonymerEintrag()
            .body(createJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnonymerEintragJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getAnlassJson()).isNotNull();
        assertThat(result.getAnlassJson().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_4_ANONYM);
        assertThat(result.getAnlassJson().getData().getDatum()).isEqualTo(anlassDatum);
        assertThat(result.getHash()).isEqualTo(
            HashValueGeneratorUtil.generateHashValueForKindValues(
                kindName,
                kindVorname,
                Geschlecht.MAENNLICH,
                kindGeburtsdatum
            )
        );
        assertThat(result.getAnlassJson().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_4_ANONYM);
    }

    @Test
    @TestAsSchularztBE
    void abschlussAnonymerEintrag_empty_untersuchung() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        // ACT
        api.anonymerEintragResourceAbschlussAnonymerEintrag()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .anonymerEintragIdPath(anonymerEintrag.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Untersuchung untersuchungAbgeschlossen =
            untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true);
        Anlass anlassAbgeschlossen = anlassService.getById(anonymerEintrag.getAnlass().getId());
        assertThat(anlassAbgeschlossen.getAnlassStatus()).isEqualTo(AnlassStatus.ABGESCHLOSSEN);
        // Hoeren
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchungAbgeschlossen.getUntersuchungHoerenAudiogramm();
        assertThat(hoerenAudiogramm).isNotNull();
        assertThat(hoerenAudiogramm.getAudiogrammBeurteilung()).isNotNull();
        assertThat(hoerenAudiogramm.getAudiogrammBeurteilung().getEnumValues(AudiogrammBeurteilung.class))
            .containsExactly(AudiogrammBeurteilung.NICHT_GEPRUEFT);
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchungAbgeschlossen.getUntersuchungHoerenOtoskopie();
        assertThat(hoerenOtoskopie).isNotNull();
        assertThat(hoerenOtoskopie.getOtoskopieLinks()).isNotNull();
        assertThat(hoerenOtoskopie.getOtoskopieLinks().getEnumValues(OtoskopieResultat.class))
            .containsExactly(OtoskopieResultat.NICHT_GEPRUEFT);
        // Sehen
        UntersuchungSehenBeurteilung sehen = untersuchungAbgeschlossen.getUntersuchungSehenBeurteilung();
        assertThat(sehen).isNotNull();
        assertThat(sehen.getBrilleLinsen()).isNotNull();
        assertThat(sehen.getBrilleLinsen().getEnumValue(BrilleLinsenResultat.class))
            .isEqualTo(BrilleLinsenResultat.NICHT_GEPRUEFT);
        // GroesseGewicht
        UntersuchungGroesseGewicht groesseGewicht = untersuchungAbgeschlossen.getUntersuchungGroesseGewicht();
        assertThat(groesseGewicht).isNotNull();
        assertThat(groesseGewicht.getGroessePerzentileSingleValue()).isNotNull();
        assertThat(groesseGewicht.getGroessePerzentileSingleValue().getEnumValue(PerzentileGroesse.class))
            .isEqualTo(PerzentileGroesse.NICHT_GEPRUEFT);
    }
}
