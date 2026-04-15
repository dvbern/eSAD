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
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
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
            .map(anlassConfig -> toJson(anlassConfig, today))
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
    public static AnlassConfigJson toJson(
        @NonNull AnlassConfig config,
        @NonNull LocalDate date
    ) {
        // Daten
        boolean showTabDaten = AnlassConfigUtil.showTabDaten(config);
        // Vorabklaerungen
        boolean showTabVorabklaerungen = AnlassConfigUtil.showTabVorabklaerungen(config);
        // Impfen
        boolean showCardImpfungenHeader = AnlassConfigUtil.showCardImpfungenHeader(config);
        boolean showCardImpfungenDataMinimal = AnlassConfigUtil.showCardImpfungDataMinimal(config);
        boolean showCardImpfungenImpfstatus = config.getImpfungenUebersicht().isVisible();
        boolean showCardImpfungenListe = config.getImpfungenListe().isVisible();
        boolean showCardImpfungenChecklist = AnlassConfigUtil.showCardImpfungenChecklist(config);
        boolean showCardImpfungenDoppelkontrolle = config.isShowImpfungenDoppelkontrolle();
        boolean showCardImpfungenMassnahmen = AnlassConfigUtil.isImpfungenMassnahmenVisible(config);
        boolean showTabImpfungen = AnlassConfigUtil.isAnyVisible(
            showCardImpfungenHeader,
            showCardImpfungenDataMinimal,
            showCardImpfungenImpfstatus,
            showCardImpfungenListe,
            showCardImpfungenChecklist,
            showCardImpfungenDoppelkontrolle,
            showCardImpfungenMassnahmen);
        // Sehen
        boolean isSehenBeurteilungVisible = AnlassConfigUtil.isSehenBeurteilungVisible(config);
        boolean isSehenBemerkungenFragebogenVisible = AnlassConfigUtil.isSehenBemerkungenFragebogenVisible(config);
        boolean isSehenMassnahmenVisible = AnlassConfigUtil.isSehenMassnahmenVisible(config);
        boolean showTabSehen = AnlassConfigUtil.showTabSehen(config);
        // Hoeren
        boolean isHoerenAudiogrammVisible = AnlassConfigUtil.isHoerenAudiogrammVisible(config);
        boolean isHoerenOtoskopieVisible = AnlassConfigUtil.isHoerenOtoskopieVisible(config);
        boolean showTabHoeren = AnlassConfigUtil.showTabHoeren(config);
        // Groesse & Gewicht
        boolean showTabGroesseGewicht = AnlassConfigUtil.showTabGroesseGewicht(config);
        // Triage
        boolean showTabTriage = AnlassConfigUtil.showTabTriage(config);
        // Läusekontrolle
        boolean showTabLaeusekontrolle = AnlassConfigUtil.showTabLaeusekontrolle(config);
        // Aerztliche Untersuchung
        boolean isArztUntersuchungBeurteilungVisible = AnlassConfigUtil.isArztUntersuchungBeurteilungVisible(config);
        boolean isArztBemerkungVisible = AnlassConfigUtil.isArztUntersuchungBemerkungVisible(config);
        boolean showTabUntersuchung = AnlassConfigUtil.showTabArztUntersuchung(config);
        // Sprache
        boolean showTabSprache = AnlassConfigUtil.showTabSprache(config);
        // Bewegungsapparat
        boolean showTabBewegungsapparat = AnlassConfigUtil.showTabBewegungsapparat(config);

        // Schulrelevante Beeinträchtigung
        boolean showTabSchulrelevanteBeeintraechtigung = AnlassConfigUtil.showTabSchulrelevanteBeeintraechtigung(config);

        // Abschluss
        boolean showCardAbschlussAbschluss = AnlassConfigUtil.showCardAbschlussAbschluss(config);
        boolean showTabAbschluss = AnlassConfigUtil.showTabAbschluss(config);
        boolean isSignaturVisible = AnlassConfigUtil.isSignaturVisible(config);

        // Massnahmen
        boolean showTabMassnahmen = AnlassConfigUtil.showTabMassnahmen(config);

        return new AnlassConfigJson(
            config.getId(),
            toSaveJson(config),
            toAnlassConfigStatus(config.getGueltigkeitsZeitraum(), date),
            AnlassConfigUtil.isAnyVisible(
                showTabDaten,
                showTabVorabklaerungen,
                showTabImpfungen,
                showTabSehen,
                showTabHoeren,
                showTabGroesseGewicht,
                showTabTriage,
                showTabLaeusekontrolle,
                showTabUntersuchung,
                showTabBewegungsapparat,
                showTabSchulrelevanteBeeintraechtigung,
                showTabAbschluss,
                showTabMassnahmen
            ),
            config.getGueltigkeitsZeitraum().getDatumBis().equals(DateConst.END_OF_TIME),
            showTabDaten,
            showTabVorabklaerungen,
            showTabImpfungen,
            showCardImpfungenHeader,
            showCardImpfungenDataMinimal,
            showCardImpfungenImpfstatus,
            showCardImpfungenListe,
            showCardImpfungenChecklist,
            showCardImpfungenDoppelkontrolle,
            showCardImpfungenMassnahmen,
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
            showTabSprache,
            showTabBewegungsapparat,
            showTabSchulrelevanteBeeintraechtigung,
            showTabAbschluss,
            showCardAbschlussAbschluss,
            isSignaturVisible,
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
}
