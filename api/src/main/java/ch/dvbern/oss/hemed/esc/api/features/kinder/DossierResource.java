package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/dossiers")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_DOSSIER)
@RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "DossierResourceMS", unit = MetricUnits.MILLISECONDS)
public class DossierResource {

    private final DossierMapper dossierMapper;

    @POST
    @Path("/kind/{kindId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public DossierJson getOrCreateDossier(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindID
    ) {
        return dossierMapper.getOrCreateByKind(kindID);
    }

    @PUT
    @Path("/{dossierId}")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public DossierJson updateDossier(
        @NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID,
        @NotNull @NonNull @Valid DossierSaveJson dossierJson
    ) {
        return dossierMapper.update(dossierID, dossierJson);
    }

    @DELETE
    @Path("/{dossierId}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public void removeDossier(@NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID) {
        dossierMapper.remove(dossierID);
    }

}
