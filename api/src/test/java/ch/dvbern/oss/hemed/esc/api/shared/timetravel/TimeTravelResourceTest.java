package ch.dvbern.oss.hemed.esc.api.shared.timetravel;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.TimetravelApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TimeTravelJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TimeTravelResourceTest {

    private final TimetravelApiSpec api = TimetravelApiSpec.timetravel(RequestSpecUtil.quarkusSpec());

    @Test
    @TestAsSupportBS
    void getInitialNow() {

        // initially, we should not be timetravelling and the now date should be today
        getNowAndCheckResult(LocalDate.now(ZONE_ID), false);

    }

    @Test
    @TestAsSupportBS
    void timetravelAndBack() {

        // timetravel back 22 days and check the new now date and timetravelling=true
        LocalDate minus22days = LocalDate.now(ZONE_ID).minusDays(22);
        updateNowAndCheckResult(minus22days, true);
        getNowAndCheckResult(minus22days, true);

        // timetravel to future 99 days and check the new now date and timetravelling=true
        LocalDate plus99days = LocalDate.now(ZONE_ID).plusDays(99);
        updateNowAndCheckResult(plus99days, true);
        getNowAndCheckResult(plus99days, true);

        // timetravel back to today, we should not be timetravelling and the now date should be today
        resetAndCheckResult(LocalDate.now(ZONE_ID));
        getNowAndCheckResult(LocalDate.now(ZONE_ID), false);

    }

    private void getNowAndCheckResult(LocalDate expectedDate, boolean expectedTimetravelling) {
        TimeTravelJsonSpec result = api
            .timeTravelResourceGetNow()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TimeTravelJsonSpec.class);

        assertThat(result).isNotNull();
        assertThat(result.getNow()).isEqualTo(expectedDate);
        assertThat(result.getTimetravelling()).isEqualTo(expectedTimetravelling);
    }

    private void updateNowAndCheckResult(LocalDate expectedDate, boolean expectedTimetravelling) {

        TimeTravelJsonSpec putJson = new TimeTravelJsonSpec(
            expectedDate,
            expectedTimetravelling
        );

        TimeTravelJsonSpec result = api
            .timeTravelResourceSetNow()
            .body(putJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TimeTravelJsonSpec.class);

        assertThat(result).isNotNull();
        assertThat(result.getNow()).isEqualTo(expectedDate);
        assertThat(result.getTimetravelling()).isEqualTo(expectedTimetravelling);
    }

    private void resetAndCheckResult(LocalDate expectedDate) {

        TimeTravelJsonSpec result = api
            .timeTravelResourceResetNow()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TimeTravelJsonSpec.class);

        assertThat(result).isNotNull();
        assertThat(result.getNow()).isEqualTo(expectedDate);
        assertThat(result.getTimetravelling()).isFalse();
    }
}
