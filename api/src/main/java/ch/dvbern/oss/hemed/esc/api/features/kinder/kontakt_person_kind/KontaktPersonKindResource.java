package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindListJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/kinder/{kindID}/kontaktpersonen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KONTAKTPERSON_KIND)
@RolesAllowed(BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("KontaktPersonKindResourceMS")
public class KontaktPersonKindResource {

    private static final List<Berechtigung> KINDER_SCHREIBEN_BERECHTIGUNGEN =
        List.of(Berechtigung.KINDER_SCHREIBEN_NICHT_MEDIZINISCH);

    private final KontaktPersonKindMapper kontaktPersonKindMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse respone;

    @GET
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH)
    public KontaktPersonKindListJson findAktivByKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID
    ) {
        return new KontaktPersonKindListJson(kontaktPersonKindMapper.findAktivByKind(kindID));
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public KontaktPersonKindOptionListJson findAllOptionsActive(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindId,
        @NotNull @QueryParam("excludeKind") boolean excludeKind
    ) {
        ID<Kind> kindToExclude = excludeKind ? kindId : null;
        return new KontaktPersonKindOptionListJson(kontaktPersonKindMapper.findAllOptionsByKind(kindToExclude));
    }

    @GET
    @Path("/{kontaktPersonKindID}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH)
    @NonNull
    public KontaktPersonKindJson getKontaktPersonKind(
        @NonNull @NotNull @PathParam("kindID") ID<Kind> kindID,
        @NonNull @NotNull @PathParam("kontaktPersonKindID") ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        kontaktPersonKindMapper.requireConsistentPathParams(kontaktPersonKindID, kindID);
        return kontaktPersonKindMapper.getById(kontaktPersonKindID);
    }

    @POST
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    @NonNull
    public KontaktPersonKindJson createKontaktPersonKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID,
        @NotNull @NonNull @Valid KontaktPersonKindSaveJson kontaktPersonKindSaveJson
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        respone.setStatusCode(Status.CREATED.getStatusCode());
        return kontaktPersonKindMapper.create(kindID, kontaktPersonKindSaveJson);
    }

    @PUT
    @Path("/{kontaktPersonKindID}")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public KontaktPersonKindJson updateKontaktPersonKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID,
        @NotNull @NonNull @PathParam("kontaktPersonKindID") ID<KontaktPersonKind> kontaktPersonKindID,
        @NotNull @NonNull @Valid KontaktPersonKindSaveJson kontaktPersonKindSaveJson
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        kontaktPersonKindMapper.requireConsistentPathParams(kontaktPersonKindID, kindID);
        return kontaktPersonKindMapper.update(kindID, kontaktPersonKindID, kontaktPersonKindSaveJson);
    }

    @DELETE
    @Path("/{kontaktPersonKindID}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public void deleteKontaktPersonKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID,
        @NotNull @NonNull @PathParam("kontaktPersonKindID") ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        kontaktPersonKindMapper.requireConsistentPathParams(kontaktPersonKindID, kindID);
        kontaktPersonKindMapper.delete(kindID, kontaktPersonKindID);
    }

    @PUT
    @Path("/schule/{kontaktPersonKindID}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public KontaktPersonKindJson addKontaktPersonKindToKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID,
        @NotNull @NonNull @PathParam("kontaktPersonKindID") ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        return kontaktPersonKindMapper.addToKind(kindID, kontaktPersonKindID);
    }

    @DELETE
    @Path("/schule/{kontaktPersonKindID}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public void removeKontaktPersonKindFromKind(
        @NotNull @NonNull @PathParam("kindID") ID<Kind> kindID,
        @NotNull @NonNull @PathParam("kontaktPersonKindID") ID<KontaktPersonKind> kontaktPersonKindID
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        kontaktPersonKindMapper.requireConsistentPathParams(kontaktPersonKindID, kindID);
        kontaktPersonKindMapper.removeFromKind(kindID, kontaktPersonKindID);
    }
}
