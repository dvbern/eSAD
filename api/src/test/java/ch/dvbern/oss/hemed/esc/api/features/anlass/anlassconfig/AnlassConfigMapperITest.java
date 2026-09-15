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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import java.time.LocalDate;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.AnlassConfigStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;

import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@RequiredArgsConstructor
class AnlassConfigMapperITest extends AbstractITest {

    private final FixtureService fixtureService;
    private final AppClock appClock;

    private final AnlassConfigMapper anlassConfigMapper;

    @Test
    @TestAsAdminBS
    @SuppressWarnings("java:S5961" /* Keep number of assertions */)
    void toJson_shouldMapAllFields() {
        // ARRANGE
        AnlassConfig entity =
            fixtureService.createAnlassConfigAllOptional(appClock.localDateTime().now().getYear(), AnlassTyp.KLASSE_4);

        // ACT
        AnlassConfigJson json = AnlassConfigMapper.toJson(entity, appClock.getToday());

        // ASSERT Fields in Json
        assertNotNull(json);
        assertEquals(json.getId(), entity.getId());
        assertThat(json.getAnlassConfigStatus()).isEqualTo(AnlassConfigStatus.AKTIV);
        assertThat(json.isAnlassConfigVisible()).isTrue();
        assertThat(json.isShowTabDaten()).isTrue();
        assertThat(json.isShowTabImpfungen()).isTrue();
        assertThat(json.isShowCardImpfungenChecklist()).isTrue();
        assertThat(json.isShowTabSehen()).isTrue();
        assertThat(json.isShowTabSehenBeurteilung()).isTrue();
        assertThat(json.isShowTabSehenBemerkungenFragenbogen()).isTrue();
        assertThat(json.isShowTabSehenMassnahmen()).isTrue();
        assertThat(json.isShowTabHoeren()).isTrue();
        assertThat(json.isShowTabHoerenAudiogramm()).isTrue();
        assertThat(json.isShowTabHoerenOtoskopie()).isTrue();
        assertThat(json.isShowTabGroesseGewicht()).isTrue();
        assertThat(json.isShowTabTriage()).isTrue();
        assertThat(json.isShowTabUntersuchung()).isTrue();
        assertThat(json.isShowTabUntersuchungBeurteilung()).isTrue();
        assertThat(json.isShowTabUntersuchungBemerkung()).isTrue();
        assertThat(json.isShowTabMassnahmen()).isTrue();
        // ASSERT Fields in SaveJson
        AnlassConfigSaveJson saveJson = json.getData();
        // ASSERT Typ & DateRange
        assertEquals(entity.getAnlassTyp(), saveJson.getAnlassTyp());
        assertEquals(entity.getGueltigkeitsZeitraum().getDatumVon(), saveJson.getDatumVon());
        assertEquals(entity.getGueltigkeitsZeitraum().getDatumBis(), saveJson.getDatumBis());
        // ASSERT Daten
        assertThat(saveJson.isShowDatenPersonendaten()).isTrue();
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getDatenUntersuchung());
        assertThat(saveJson.isShowDatenSchule()).isTrue();
        // ASSERT Impfungen
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getImpfungenUebersicht());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getImpfungenListe());
        assertThat(saveJson.isImpfungenMassnahmeGruppenFolgetermin()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeEinzelFolgetermin()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeInfoKinderarzt()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeBeratungMPA()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeInfomaterialAbgegeben()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeInfoEltern()).isTrue();
        assertThat(saveJson.isImpfungenMassnahmeRueckfrageKinderarzt()).isTrue();
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getCheckEinwilligungVorhanden());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getCheckKontraindikationenAbgeklaert());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungImpfungBemerkungen());
        // ASSERT SEHEN
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenBrilleLinsen());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenFarbsehen());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenTafel());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenFernvisus());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenStereoTest());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenStrabismus());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenInspektion());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenKonvergenz());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenMotilitaet());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenNahvisus());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenBemerkungen());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenBemerkungenFragebogen());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getSehenMassnahmen());
        // ASSERT Hoeren Audiogramm
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogramm());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogramm250Hz());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogramm8000Hz());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogrammBeurteilung());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogrammZusaetzlicheAngaben());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenAudiogrammBemerkungen());
        // ASSERT Hoeren Otoskopie
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenOtoskopie());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getHoerenOtoskopieBemerkungen());
        // ASSERT Groesse & Gewicht
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGroesse());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGewicht());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtBmi());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGroessePerzentileSingleValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGroessePerzentileMultiValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGewichtPerzentileSingleValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtGewichtPerzentileMultiValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtBmiPerzentileSingleValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtBmiPerzentileMultiValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getGroesseGewichtBemerkungen());
        // ASSERT Triage
        assertThat(saveJson.isShowTriageTriage()).isTrue();
        assertThat(saveJson.isShowTriageKindWeiterleiten()).isTrue();
        // ASSERT Aerztliche Untersuchung
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungBewegungsapparat());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungEinbeinstand());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungEinbeinhuepfen());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungFingeropposition());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungMotorik());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungSprachfaehigkeitMultiValue());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungMundZaehne());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungHaut());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungHerz());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungBlutdruck());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungVerhalten());
        assertEquals(RequirementSetting.OPTIONAL, saveJson.getUntersuchungBemerkungen());
        // Massnahmen
        assertThat(saveJson.isShowMassnahmenPathologischeBefunde()).isTrue();
        assertThat(saveJson.isShowMassnahmenKeine()).isTrue();
        assertThat(saveJson.isShowMassnahmenBeratungMpa()).isTrue();
        assertThat(saveJson.isShowMassnahmenBeratungSchulAerztin()).isTrue();
        assertThat(saveJson.isShowMassnahmenBereitsBehandelt()).isTrue();
        assertThat(saveJson.isShowMassnahmenInfomaterial()).isTrue();
        assertThat(saveJson.isShowMassnahmenInfoEltern()).isTrue();
        assertThat(saveJson.isShowMassnahmenUeberweisungHausarzt()).isTrue();
        assertThat(saveJson.isShowMassnahmenUeberweisungAugenarzt()).isTrue();
        assertThat(saveJson.isShowMassnahmenNachkontrolleSaed()).isTrue();
        assertThat(saveJson.isShowMassnahmenAnderes()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void toEntityForCreate_should_use_end_of_time_guetligkeit() {
        // ARRANGE
        var von = LocalDate.of(1998, 1, 1);
        var bis = LocalDate.of(1998, 12, 31);
        var configWithGueltigkeitThatShouldBeIgnored = AnlassConfigBuilder.createJax(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(von, bis)
            .withGroesseGewichtBmiPerzentileValues(EnumSet.allOf(PerzentileBmi.class))
        );

        // ACT
        AnlassConfig entityForCreate =
            anlassConfigMapper.toEntityForCreate(configWithGueltigkeitThatShouldBeIgnored.getData());
        // ASSERT
        assertThat(entityForCreate.getGueltigkeitsZeitraum().getDatumVon()).isEqualTo(von);
        assertThat(entityForCreate.getGueltigkeitsZeitraum().getDatumBis()).isEqualTo(DateConst.END_OF_TIME);

    }
}
