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

package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.time.format.DateTimeFormatter;
import java.util.List;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DocumentQueueApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SuperAdminRunnerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueMassenbriefSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentQueueResourceAuthorizationITest extends AbstractITest {

    private final DocumentQueueApiSpec api = DocumentQueueApiSpec.documentQueue(RequestSpecUtil.quarkusSpec());
    private final SuperAdminRunnerApiSpec systemApi =
        SuperAdminRunnerApiSpec.superAdminRunner(RequestSpecUtil.quarkusSpec());
    private final DocumentQueueMapper documentQueueMapper;
    private final FixtureService fixtureService;
    private ID<DocumentQueue> documentQueueID = null;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Order(1)
    @Test
    @TestAsSupportBS
    void createAndDownloadMassenbriefAsAdmin() {
        // ARRANGE
        // Prepare the data to create a Massenbrief
        Kind kind1 = fixtureService.createKind("Muster", "Max");
        Kind kind2 = fixtureService.createKind("Muster", "Moritz");
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJsonSpec saveJsonSpec = new DocumentQueueMassenbriefSaveJsonSpec(
            briefvorlage.getId().getId(),
            null,
            List.of(kind1.getId().getId(), kind2.getId().getId())
        );
        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Massenbrief-" + briefvorlage.getName() + ".zip";

        // ACT
        // Create the entry for the Queue
        api.documentQueueResourceCreateMassenbrief()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());
        // trigger queue
        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // get and set ID of DocumentQueue
        documentQueueID = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.MASSENBRIEF_ZIP)
            .findAny()
            .orElseThrow()
            .getId();

        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(documentQueueID.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
        assertThat(documentQueueID).isNotNull();
    }

    @Order(2)
    @Test
    @TestAsAdminBS
    void downloadMassenbriefZIPFromAdmin_should_throw() {
        // Act & assert
        api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(documentQueueID.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
