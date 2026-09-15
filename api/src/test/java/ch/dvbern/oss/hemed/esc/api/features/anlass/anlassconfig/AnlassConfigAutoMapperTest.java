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

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class AnlassConfigAutoMapperTest {

    @Test
    void mapping_to_anlassConfigSaveJson_works_for_special_properties() {

        var von = LocalDate.of(1998, 1, 1);
        var bis = LocalDate.of(1998, 12, 31);
        AnlassConfig configKlasse4 = AnlassConfigBuilder.create(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(von, bis)
            .withGroesseGewichtBmiPerzentileValues(EnumSet.allOf(PerzentileBmi.class))
        );
        AnlassConfigSaveJson result = AnlassConfigAutoMapper.INSTANCE.toSaveJson(configKlasse4);
        Assertions.assertThat(result).isNotNull()
            .returns(von, AnlassConfigSaveJson::getDatumVon)
            .returns(bis, AnlassConfigSaveJson::getDatumBis)
            .returns(
                configKlasse4.getSehenBrilleLinsenDefaultValue(),
                AnlassConfigSaveJson::getSehenBrilleLinsenDefaultValue)
            .returns(
                configKlasse4.getSehenFernvisusDefaultValue(),
                AnlassConfigSaveJson::getSehenFernvisusDefaultValue)
            .returns(
                configKlasse4.getHoerenAudiogrammDefaultValue(),
                AnlassConfigSaveJson::getHoerenAudiogrammDefaultValue)
            .returns(
                configKlasse4.getHoerenAudiogramm250HzDefaultValue(),
                AnlassConfigSaveJson::getHoerenAudiogramm250HzDefaultValue)
            .returns(
                configKlasse4.getHoerenAudiogramm8000HzDefaultValue(),
                AnlassConfigSaveJson::getHoerenAudiogramm8000HzDefaultValue)
            .extracting(AnlassConfigSaveJson::getGroesseGewichtBmiPerzentileValues)
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .containsExactly(PerzentileBmi.values());

    }

}
