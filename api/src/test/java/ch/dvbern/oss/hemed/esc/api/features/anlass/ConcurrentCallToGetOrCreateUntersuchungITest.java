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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.EinladungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungImpfungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungMassnahmenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungImpfungenDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTabsSavedJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;

/**
 * We can run into situations where getOrCreateUntersuchung is called from multiple parallel requests.
 * This can lead to optimistic locking expcetions on the Einladung.
 * This test should check if such a scenario can work
 * The secenario is, that Einladung exists but not untersuchung
 */
@QuarkusTest
@RequiredArgsConstructor
class ConcurrentCallToGetOrCreateUntersuchungITest extends AbstractITest {

    private final FixtureService fixtureService;

    private final UntersuchungMassnahmenApiSpec massnahmenApi =
        UntersuchungMassnahmenApiSpec.untersuchungMassnahmen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungImpfungenApiSpec impfungenApi =
        UntersuchungImpfungenApiSpec.untersuchungImpfungen(RequestSpecUtil.quarkusSpec());

    private final EinladungenApiSpec einladungApi = EinladungenApiSpec.einladungen(RequestSpecUtil.quarkusSpec());
    private final ManagedExecutor concurrentExecutor;

    @Test
    @TestAsAdminBS
    void concurrentCallToGetOrCreateUntersuchung_should_work()
        throws TimeoutException, ExecutionException, InterruptedException {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Anlass anlass = einladung.getAnlass();

        // ARRANGE some problematic calls to run in parallel, they all access untersuchung thorough laz loading

        Runnable taskMassnahmen = () ->
            massnahmenApi
                .untersuchungMassnahmenResourceGetUntersuchungMassnahmen()
                .anlassIdPath(anlass.getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.NO_CONTENT.getStatusCode());

        Callable<UntersuchungImpfungenDataJsonSpec> taskImpfungen = () ->
            impfungenApi
                .untersuchungImpfungenResourceGetUntersuchungImpfungenData()
                .anlassIdPath(anlass.getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungImpfungenDataJsonSpec.class);

        Callable<UntersuchungTabsSavedJsonSpec> taskTabsSavedByEinladungId = () ->
            einladungApi
                .einladungResourceGetUntersuchungTabsSavedByEinladungId()
                .anlassIdPath(anlass.getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungTabsSavedJsonSpec.class);
        // ACT
        // Submit tasks concurrently
        Future<?> futureMassnahmen = concurrentExecutor.submit(taskMassnahmen);
        Future<UntersuchungImpfungenDataJsonSpec> futureImpfungen = concurrentExecutor.submit(taskImpfungen);
        Future<UntersuchungTabsSavedJsonSpec> futureTabs = concurrentExecutor.submit(taskTabsSavedByEinladungId);

        // Wait for completion
        futureMassnahmen.get(8, TimeUnit.SECONDS);
        UntersuchungImpfungenDataJsonSpec impfungenResult = futureImpfungen.get(8, TimeUnit.SECONDS);
        UntersuchungTabsSavedJsonSpec tabsResult = futureTabs.get(8, TimeUnit.SECONDS);

        // ASSERT (optional: add assertions to verify the content if needed)
        Assertions.assertThat(tabsResult).isNotNull();
        Assertions.assertThat(impfungenResult).isNotNull();

    }

}

