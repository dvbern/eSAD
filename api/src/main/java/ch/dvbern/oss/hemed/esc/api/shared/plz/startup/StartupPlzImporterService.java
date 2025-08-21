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

package ch.dvbern.oss.hemed.esc.api.shared.plz.startup;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.plz.PLZData;
import ch.dvbern.oss.hemed.esc.api.shared.plz.PLZService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@Service
@RequiredArgsConstructor
public class StartupPlzImporterService {

    public static final String PLZ_OF_BERN = "3014";
    private final PLZService plzService;

    @RunAs(username = "dev-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    void onStart(@Observes StartupEvent ev) {

            List<PLZData> orteForPLZ = plzService.findOrteForPLZ(PLZ_OF_BERN);
            if (orteForPLZ.isEmpty()) {
                LOG.info("Could not find any imported PLZ Data on Startup, triggering import...");
                plzService.importIntoDatabase();
            } else {
                LOG.info("PLZ data probably already imported since we found an Ort for {}, skipping import.", PLZ_OF_BERN);
            }
    }
}
