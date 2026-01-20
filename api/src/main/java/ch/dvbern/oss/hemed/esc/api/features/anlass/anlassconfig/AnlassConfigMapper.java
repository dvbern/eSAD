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
import java.util.Arrays;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.AnlassConfigStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public final class AnlassConfigMapper {

    private final AnlassConfigService anlassConfigService;
    private final AppClock appClock;

    @NonNull
    public AnlassConfigListJson findAll() {
        LocalDate today = appClock.getToday();
        List<AnlassConfigJson> anlassConfigList = anlassConfigService.findAll().stream()
            .map(anlassConfig -> {
                boolean usedInAtLeastOneAnlass = anlassConfigService.isUsedInAtLeastOneAnlass(anlassConfig.getId());
                return toJson(anlassConfig, usedInAtLeastOneAnlass, today);
            })
            .toList();
        return new AnlassConfigListJson(anlassConfigList);
    }

    @NonNull
    public AnlassConfigOptionListJson findAllVisible() {
        AnlassConfigListJson allConfigs = findAll();
        List<AnlassConfigOptionJson> allVisible = allConfigs.getAnlassConfigList().stream()
            .filter(AnlassConfigJson::isAnlassConfigVisible)
            .map(AnlassConfigMapper::toOptionJson)
            .toList();
        return new AnlassConfigOptionListJson(allVisible);
    }

    public void createAnlassConfig(@NonNull AnlassConfigSaveJson saveJson) {
        AnlassConfig configToSave = toEntityForCreate(saveJson);
        anlassConfigService.createAnlassConfig(configToSave);
    }

    public void updateAnlassConfig(
        @NonNull ID<AnlassConfig> anlassConfigId,
        @NonNull AnlassConfigSaveJson saveJson
    ) {
        AnlassConfig existingConfig = anlassConfigService.getById(anlassConfigId);
        anlassConfigService.updateAnlassConfig(toEntity(existingConfig, saveJson));
    }

    @NonNull
    public static AnlassConfigJson toJson(@NonNull AnlassConfig config, boolean isUsedInAtLeastOneAnlass, @NonNull LocalDate date) {
        // Daten
        boolean showTabDaten = config.isShowDatenPersonendaten()
            || config.isShowDatenPersonendatenEsadId()
            || isAnyVisible(config.getDatenUntersuchung())
            || config.isShowDatenSchule();
        // Impfen
        boolean showTabImpfungen = isAnyVisible(
            config.getImpfungenUebersicht(),
            config.getImpfungenListe(),
            config.getUntersuchungImpfungEmpfehlung())
            || config.isShowImpfungenDoppelkontrolle()
            || config.isImpfungenMassnahmeKeineMassnahmen()
            || config.isImpfungenMassnahmeGruppenFolgetermin()
            || config.isImpfungenMassnahmeImpfenFolgetermin()
            || config.isImpfungenMassnahmeEinzelFolgetermin()
            || config.isImpfungenMassnahmeInfoKinderarzt()
            || config.isImpfungenMassnahmeBeratungMPA()
            || config.isImpfungenMassnahmeInfomaterialAbgegeben()
            || config.isImpfungenMassnahmeInfoEltern()
            || config.isImpfungenMassnahmeRueckfrageKinderarzt();
        boolean isCheckListVisible = isAnyVisible(
            config.getCheckEinwilligungVorhanden(),
            config.getCheckKontraindikationenAbgeklaert(),
            config.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
            config.getUntersuchungImpfungBemerkungen()
        );
        // Sehen
        boolean isSehenBeurteilungVisible = isAnyVisible(
            config.getSehenBrilleLinsen(),
            config.getSehenFarbsehen(),
            config.getSehenTafel(),
            config.getSehenFernvisus(),
            config.getSehenStereoTest(),
            config.getSehenStrabismus(),
            config.getSehenInspektion(),
            config.getSehenKonvergenz(),
            config.getSehenMotilitaet(),
            config.getSehenNahvisus(),
            config.getSehenBemerkungen(),
            config.getSehenMassnahmen());
        boolean isSehenBemerkungenFragebogenVisible = isAnyVisible(
            config.getSehenBemerkungenFragebogen()
        );
        boolean isSehenMassnahmenVisible = isAnyVisible(
            config.getSehenMassnahmen()
        );
        boolean showTabSehen = isAnyVisible(
            isSehenBeurteilungVisible,
            isSehenBemerkungenFragebogenVisible,
            isSehenMassnahmenVisible
        );
        // Hoeren
        boolean isHoerenAudiogrammVisible = isAnyVisible(
            config.getHoerenAudiogramm(),
            config.getHoerenAudiogramm250Hz(),
            config.getHoerenAudiogramm8000Hz(),
            config.getHoerenAudiogrammBeurteilung(),
            config.getHoerenAudiogrammBemerkungen());
        boolean isHoerenOtoskopieVisible = isAnyVisible(
            config.getHoerenOtoskopie(),
            config.getHoerenOtoskopieBemerkungen());
        boolean showTabHoeren = isAnyVisible(
            isHoerenAudiogrammVisible,
            isHoerenOtoskopieVisible
        );
        // Groesse & Gewicht
        boolean showTabGroesseGewicht = isAnyVisible(
            config.getGroesseGewichtGroesse(),
            config.getGroesseGewichtGewicht(),
            config.getGroesseGewichtBmi(),
            config.getGroesseGewichtGroessePerzentile(),
            config.getGroesseGewichtGewichtPerzentile(),
            config.getGroesseGewichtBmiPerzentile(),
            config.getGroesseGewichtEmpfehlung(),
            config.getGroesseGewichtBemerkungen());
        // Triage
        boolean showTabTriage = isAnyVisible(
            config.isShowTriageTriage(),
            config.isShowTriageKindWeiterleiten()
        );
        // Läusekontrolle
        boolean showTabLaeusekontrolle = isAnyVisible(
            config.getLaeusekontrolleBeurteilung(),
            config.getLaeusekontrolleEmpfehlung(),
            config.getLaeusekontrolleBemerkungen()
        );
        // Aerztliche Untersuchung
        boolean isArztUntersuchungBeurteilungVisible = isAnyVisible(
            config.getUntersuchungBewegungsapparat(),
            config.getUntersuchungEinbeinstand(),
            config.getUntersuchungEinbeinstandRechtsLinks(),
            config.getUntersuchungEinbeinhuepfen(),
            config.getUntersuchungEinbeinhuepfenRechtsLinks(),
            config.getUntersuchungFingeropposition(),
            config.getUntersuchungHampelmann(),
            config.getUntersuchungBallFangen(),
            config.getUntersuchungBallWerfen(),
            config.getUntersuchungNameSchreiben(),
            config.getUntersuchungFigurNachmalen(),
            config.getUntersuchungStifthaltung(),
            config.getUntersuchungAussprache(),
            config.getUntersuchungLesefit(),
            config.getUntersuchungLesefitPunkte(),
            config.getUntersuchungZahlenNachsprechen4(),
            config.getUntersuchungZahlenNachsprechen5(),
            config.getUntersuchungZeichnenKreis(),
            config.getUntersuchungZeichnenViereck(),
            config.getUntersuchungZeichnenDreieck(),
            config.getUntersuchungZeichnenMensch(),
            config.getUntersuchungZeichnenMenschPunkte(),
            config.getUntersuchungKognition(),
            config.getUntersuchungKognitionPunkte(),
            config.getUntersuchungMotorik(),
            config.getUntersuchungMotorikPunkte(),
            config.getUntersuchungSprachfaehigkeitSingleValue(),
            config.getUntersuchungSprachfaehigkeitMultiValue(),
            config.getUntersuchungSprachePunkte(),
            config.getUntersuchungMundZaehne(),
            config.getUntersuchungHaut(),
            config.getUntersuchungHerz(),
            config.getUntersuchungBlutdruck(),
            config.getUntersuchungVerhalten());
        boolean isArztBemerkungVisible = isAnyVisible(
            config.getUntersuchungBemerkungen());
        boolean showTabUntersuchung = isAnyVisible(
            isArztUntersuchungBeurteilungVisible,
            isArztBemerkungVisible
        );
        // Massnahmen
        boolean showTabMassnahmen = isAnyVisible(
            config.isShowMassnahmenPathologischeBefunde(),
            config.isShowMassnahmenKeine(),
            config.isShowMassnahmenBeratungMpa(),
            config.isShowMassnahmenBeratungSchulAerztin(),
            config.isShowMassnahmenBereitsBehandelt(),
            config.isShowMassnahmenInfomaterial(),
            config.isShowMassnahmenInfoEltern(),
            config.isShowMassnahmenUeberweisungHausarzt(),
            config.isShowMassnahmenUeberweisungAugenarzt(),
            config.isShowMassnahmenNachkontrolleSaed(),
            config.isShowMassnahmenAnderes()
        );

        return new AnlassConfigJson(
            config.getId(),
            toSaveJson(config),
            toAnlassConfigStatus(config.getGueltigkeitsZeitraum(), date),
            isAnyVisible(
                showTabDaten,
                showTabImpfungen,
                showTabSehen,
                showTabHoeren,
                showTabGroesseGewicht,
                showTabTriage,
                showTabLaeusekontrolle,
                showTabUntersuchung,
                showTabMassnahmen
            ),
            isUsedInAtLeastOneAnlass,
            config.getGueltigkeitsZeitraum().getDatumBis().equals(DateConst.END_OF_TIME),
            showTabDaten,
            showTabImpfungen,
            isCheckListVisible,
            showTabSehen,
            isSehenBeurteilungVisible,
            isSehenBemerkungenFragebogenVisible,
            isSehenMassnahmenVisible,
            showTabHoeren,
            isHoerenAudiogrammVisible,
            isHoerenOtoskopieVisible,
            showTabGroesseGewicht,
            showTabTriage,
            showTabLaeusekontrolle,
            showTabUntersuchung,
            isArztUntersuchungBeurteilungVisible,
            isArztBemerkungVisible,
            showTabMassnahmen
        );
    }

    @NonNull
    public static AnlassConfigSaveJson toSaveJson(@NonNull AnlassConfig config) {
        return AnlassConfigAutoMapper.INSTANCE.toSaveJson(config);
    }

    @NonNull
    public static AnlassConfigOptionJson toOptionJson(@NonNull AnlassConfig config) {
        return new AnlassConfigOptionJson(
            config.getId(),
            config.getAnlassTyp(),
            config.getGueltigkeitsZeitraum().getDatumVon(),
            config.getGueltigkeitsZeitraum().getDatumBis(),
            config.getBezeichnung()
        );
    }

    @NonNull
    private static AnlassConfigOptionJson toOptionJson(@NonNull AnlassConfigJson config) {
        return new AnlassConfigOptionJson(
            config.getId(),
            config.getData().getAnlassTyp(),
            config.getData().getDatumVon(),
            config.getData().getDatumBis(),
            config.getData().getBezeichnung()
        );
    }

    @NonNull
    private static AnlassConfigStatus toAnlassConfigStatus(@NonNull DateRange dateRange, @NonNull LocalDate date) {
        AnlassConfigStatus status = AnlassConfigStatus.AKTIV;
        if (dateRange.getDatumBis().isBefore(date)) {
            status = AnlassConfigStatus.ABGELAUFEN;
        }
        if (dateRange.getDatumVon().isAfter(date)) {
            status = AnlassConfigStatus.GEPLANT;
        }
        return status;
    }

    @NonNull
    AnlassConfig toEntityForCreate(@NonNull AnlassConfigSaveJson saveJson) {
        AnlassConfig entityForCreate = AnlassConfigAutoMapper.INSTANCE.toEntityForCreate(saveJson);
        // we always want to create new Anlassconfigs with a gueltigkeitszeitraum END_OF_TIME
        // so we have to handle this here
        entityForCreate.setGueltigkeitsZeitraum(
            DateRange.of(
                saveJson.getDatumVon(),
                DateConst.END_OF_TIME
            ));
        return entityForCreate;
    }

    @NonNull
    private AnlassConfig toEntity(@NonNull AnlassConfig existingConfig, @NonNull AnlassConfigSaveJson saveJson) {
        // AnlassTyp, DatumVon und DatumBis koennen nicht editiert werden
        AnlassConfigAutoMapper.INSTANCE.updateAnlassConfigFromAnlassConfigSaveJson(existingConfig, saveJson);

        return existingConfig;
    }

    private static boolean isAnyVisible(@NonNull RequirementSetting... settings) {
        return Arrays.stream(settings).anyMatch(RequirementSetting::isVisible);
    }

    private static boolean isAnyVisible(Boolean... settings) {
        return Arrays.stream(settings).anyMatch(Boolean::booleanValue);
    }
}
