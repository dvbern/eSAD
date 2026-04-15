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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.skripthelper;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.util.AnlassConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public final class AnlassConfigSkriptMapper {

    private final AnlassConfigService anlassConfigService;

    @NonNull
    public static AnlassConfigSkriptJson toJson(@NonNull AnlassConfig config) {
        // Daten
        return new AnlassConfigSkriptJson(
            config.getId(),
            AnlassConfigUtil.createSqlInsertQuery(config),
            AnlassConfigUtil.createSqlUpdateQuery(config)
        );
    }

    @NonNull
    public AnlassConfigSkriptJson getSqlSkripts(@NonNull ID<AnlassConfig> anlassConfigId) {
        AnlassConfig config = anlassConfigService.getById(anlassConfigId);
        return toJson(config);
    }

    @NonNull
    public String getLiquibaseSkript() {
        List<AnlassConfig> anlassConfigs = anlassConfigService.findAll();

        String template = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
            + "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:pro=\"http://www.liquibase.org/xml/ns/pro\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n"
            + "  <changeSet author=\"dvbern\" id=\"{{timestamp}}\">\n"
            + "    <sql>\n"
            + "{{script}}\n"
            + "    </sql>\n"
            + "  </changeSet>\n"
            + "</databaseChangeLog>\n";
        template = template.replace("{{timestamp}}", "" + System.currentTimeMillis());

        StringBuilder sb = new StringBuilder();
        for (AnlassConfig config : anlassConfigs) {
            sb.append(AnlassConfigUtil.createSqlInsertQuery(config));
            sb.append(AnlassConfigUtil.createSqlUpdateQuery(config));
        }
        template = template.replace("{{script}}", sb.toString());

        return template;
    }
}
