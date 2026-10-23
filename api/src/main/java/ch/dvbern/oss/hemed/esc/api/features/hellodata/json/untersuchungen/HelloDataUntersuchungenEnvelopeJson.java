package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataUntersuchungenEnvelopeJson {

    /**
     * All exported Untersuchungen. Empty rather than absent when the tenant has none.
     */
    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    List<HelloDataUntersuchungenJson> data;

    /**
     * Always the <b>last</b> member of the document: {@code rowCount} is only known once the
     * cursor is exhausted (see {@code JsonEnvelopeWriter}). JSON members are unordered, so
     * consumers must not depend on the position - only on the key being present.
     * <p>
     * If it is missing, the response was truncated and the load must be treated as failed.
     */
    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    @JsonProperty(HelloDataSentinelJson.META_KEY)
    HelloDataMetaJson meta;
}
