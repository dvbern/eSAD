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

package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.LocalDate;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
public class AnlassConfigBuilder {

    private final AnlassConfig anlassConfig;

    private AnlassConfigBuilder() {
        this.anlassConfig = defaultAnlassConfig();
        TestUtil.setEntityInfo(anlassConfig);
    }

    @NonNull
    public static AnlassConfig defaultAnlassConfig() {
        return new AnlassConfig(
            AnlassTyp.KLASSE_4,
            DateRange.ofFullRange(),
            true,
            RequirementSetting.OPTIONAL,
            true,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            true,
            true,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            RequirementSetting.OPTIONAL,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
        );
    }

    @NonNull
    public static AnlassConfig allSameRequirementSetting(RequirementSetting requirementSetting) {
        boolean showBooleans = requirementSetting != RequirementSetting.NOT_VISIBLE;
        return new AnlassConfig(
            AnlassTyp.KLASSE_4,
            DateRange.ofFullRange(),
            showBooleans,
            requirementSetting,
            showBooleans,
            requirementSetting,
            requirementSetting,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            showBooleans,
            showBooleans,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            requirementSetting,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans,
            showBooleans
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withID(@NonNull ID<AnlassConfig> id) {
        this.anlassConfig.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withAnlassTyp(@NonNull AnlassTyp anlassTyp) {
        this.anlassConfig.setAnlassTyp(anlassTyp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withGueltigkeitsZeitraum(@NonNull LocalDate datumVon, @NonNull LocalDate datumBis) {
        this.anlassConfig.setGueltigkeitsZeitraum(DateRange.of(datumVon, datumBis));
        return this;
    }

    @NonNull
    public static AnlassConfig create(@NonNull Consumer<AnlassConfigBuilder> block) {
        AnlassConfigBuilder builder = new AnlassConfigBuilder();
        block.accept(builder);
        return builder.anlassConfig;
    }
}
