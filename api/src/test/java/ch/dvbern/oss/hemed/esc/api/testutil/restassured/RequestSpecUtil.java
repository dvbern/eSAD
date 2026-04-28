/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuezt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere für Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.testutil.restassured;

import java.util.function.Supplier;

import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import io.restassured.builder.RequestSpecBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class RequestSpecUtil {

    private RequestSpecUtil() {
    }

    public static Supplier<RequestSpecBuilder> quarkusSpec() {
        return () -> new RequestSpecBuilder()
            .setBaseUri("http://localhost:8081")
            .setBasePath("/");
    }

    public static String toHasUntersuchungIdWrapper(@NonNull ID<Einladung> einladungId) {

        return HasUntersuchungIDWrapper.fromEinladungId(einladungId).toExternalForm();
    }
}
