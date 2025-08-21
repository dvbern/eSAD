/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuezt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere für Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.testutil.restassured.helper;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.InfomaterialApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.InfomaterialListJsonSpec;
import io.restassured.response.ResponseBody;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * helper for testing
 */
@RequestScoped
@RequiredArgsConstructor
public class InfomaterialResourceHelper {

    private final InfomaterialApiSpec api = InfomaterialApiSpec.infomaterial(RequestSpecUtil.quarkusSpec());


    @NonNull
    public List<InfomaterialJsonSpec> findAllInfomaterialien() {
        InfomaterialListJsonSpec result = api
            .infomaterialResourceFindAllInfomaterialien()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(InfomaterialListJsonSpec.class);
        return result.getInfomaterialienList();
    }
}
