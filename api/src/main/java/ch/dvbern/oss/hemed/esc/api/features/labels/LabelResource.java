package ch.dvbern.oss.hemed.esc.api.features.labels;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelListJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/labels")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_LABELS)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("LabelResourceMS")
@Slf4j
public class LabelResource {

    private final LabelMapper labelMapper;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LABELS_LESEN, BerechtigungName.LABELS_ADMIN_LESEN })
    public LabelListJson findAllLabels() {
        return new LabelListJson(labelMapper.findAll());
    }

    @GET
    @NonNull
    @Path("/filter")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LABELS_LESEN)
    public LabelListJson filterLabels(@NotNull @NonNull @QueryParam("type") LabelTyp type) {
        return new LabelListJson(labelMapper.filterByType(type));
    }

    @GET
    @NonNull
    @Path("/{labelID}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LABELS_LESEN)
    public LabelJson getById(@NotNull @NonNull @PathParam("labelID") ID<Label> labelID) {
        return (labelMapper.getById(labelID));
    }


    @POST
    @NonNull
    @Counted("createLabelCount")
    @Timed("createLabelMS")
    @RolesAllowed(BerechtigungName.LABELS_SCHREIBEN)
    public LabelJson createLabel(
        @NotNull @NonNull @Valid LabelSaveJson labelJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return labelMapper.create(labelJson);
    }

    @PUT
    @Path("/{labelId}")
    @NonNull
    @RolesAllowed(BerechtigungName.LABELS_SCHREIBEN)
    public LabelJson updateLabel(
        @NotNull @NonNull @PathParam("labelId") ID<Label> labelID,
        @NotNull @NonNull @Valid LabelSaveJson labelJson
    ) {
        return labelMapper.update(labelID, labelJson);
    }

    @DELETE
    @Path("/{labelId}")
    @RolesAllowed(BerechtigungName.LABELS_SCHREIBEN)
    public void removeLabel(@NotNull @NonNull @PathParam("labelId") ID<Label> labelID) {
        labelMapper.remove(labelID);
    }
}
