package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierMedizinischSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/dossiers")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_DOSSIER)
@RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("DossierResourceMS")
public class DossierResource {

    private final DossierMapper dossierMapper;

    @POST
    @Path("/kind/{kindId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH })
    public DossierJson getOrCreateDossier(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindID
    ) {
        return dossierMapper.getOrCreateByKind(kindID);
    }

    @PUT
    @Path("/{dossierId}/medizinisch")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH)
    public DossierJson updateDossierMedizinisch(
        @NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID,
        @NotNull @NonNull @Valid DossierMedizinischSaveJson dossierJson
    ) {
        return dossierMapper.updateMedizinisch(dossierID, dossierJson);
    }

    @PUT
    @Path("/{dossierId}/verantwortlich-arzt")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public DossierJson updateDossierVerantwortlichArzt(
        @NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID,
        @NotNull @NonNull @Valid BenutzerOptionJson benutzerJson
    ) {
        return dossierMapper.updateDossierVerantwortlicherArzt(dossierID, benutzerJson);
    }

    @PUT
    @Path("/{dossierId}/verantwortlich-mpa")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public DossierJson updateDossierVerantwortlichMpa(
        @NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID,
        @NotNull @NonNull @Valid BenutzerOptionJson dossierJson
    ) {
        return dossierMapper.updateDossierVerantwortlicheMpa(dossierID, dossierJson);
    }

    @PUT
    @Path("/{dossierId}/papieraktestatus/{status}")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public DossierJson updateDossierPapierakteStatus(
        @NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID,
        @NotNull @NonNull @PathParam("status") PapierakteStatus status
    ) {
        return dossierMapper.updateDossierPapierakteStatus(dossierID, status);
    }

    @DELETE
    @Path("/{dossierId}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH)
    public void removeDossier(@NotNull @NonNull @PathParam("dossierId") ID<Dossier> dossierID) {
        dossierMapper.remove(dossierID);
    }

}
