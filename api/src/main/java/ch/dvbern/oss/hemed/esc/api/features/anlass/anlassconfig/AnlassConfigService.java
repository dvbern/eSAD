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

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.util.AnlassConfigUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class AnlassConfigService {

    private final AnlassConfigRepo anlassConfigRepo;
    private final ConfigService configService;

    @NonNull
    public List<AnlassConfig> findAll() {
        List<AnlassConfig> allConfigs = anlassConfigRepo.findAll();
        // Wir validieren hier, da wir bei jedem Update/Create die Liste neu Laden
        AnlassConfigUtil.requireAnlassConfigsNoGapNoOverlap(allConfigs);
        return allConfigs;
    }

    @NonNull
    public AnlassConfig getById(@NonNull ID<AnlassConfig> anlassConfigId) {
        return anlassConfigRepo.getById(anlassConfigId);
    }

    @NonNull
    public AnlassConfig getByAnlassTypAndStichtag(@NonNull AnlassTyp anlassTyp, @NonNull LocalDate stichtag) {
        return anlassConfigRepo.getByAnlassTypAndStichtag(anlassTyp, stichtag);
    }

    @NonNull
    private AnlassConfig persist(@NonNull AnlassConfig anlassConfig) {
        anlassConfigRepo.persist(anlassConfig);
        return anlassConfig;
    }

    @NonNull
    public AnlassConfig createAnlassConfig(@NonNull AnlassConfig newAnlassConfig) {
        AnlassConfig latestAnlassConfig = getByAnlassTypAndStichtag(newAnlassConfig.getAnlassTyp(), DateConst.END_OF_TIME);
        AnlassConfigUtil.requireAnlassConfigValidDatumVon(latestAnlassConfig, newAnlassConfig);
        latestAnlassConfig.getGueltigkeitsZeitraum().setDatumBis(
            newAnlassConfig.getGueltigkeitsZeitraum().getDatumVon().minusDays(1)
        );
        persist(latestAnlassConfig);
        return persist(newAnlassConfig);
    }

    @NonNull
    public AnlassConfig updateAnlassConfig(@NonNull AnlassConfig anlassConfig) {
        boolean usedInAtLeastOneAnlass = isUsedInAtLeastOneAnlass(anlassConfig.getId());
        boolean alwaysEditable = configService.getEnvironment() != DeploymentEnvironment.UAT
            && configService.getEnvironment() != DeploymentEnvironment.PROD;
        if (!alwaysEditable) {
            AnlassConfigUtil.requireAnlassConfigNotYetUsedInAnlass(usedInAtLeastOneAnlass);
        }
        return persist(anlassConfigRepo.getById(anlassConfig.getId()));
    }

    public boolean isUsedInAtLeastOneAnlass(@NonNull ID<AnlassConfig> anlassConfigId) {
        return anlassConfigRepo.isUsedInAtLeastOneAnlass(anlassConfigId);
    }
}
