package ch.dvbern.oss.hemed.esc.api.features.schule;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseMapper;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
import io.smallrye.faulttolerance.api.RateLimit;
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
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/schulen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_SCHULEN)
@RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("SchuleResourceMS")
public class SchuleResource {

    private final SchuleMapper schuleMapper;
    private final KlasseMapper klasseMapper;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public SchuleListJson findAllSchulen() {
        return new SchuleListJson(schuleMapper.findAll());
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public SchuleOptionListJson findAllOptions(
        @NotNull @QueryParam("onlyActive") boolean onlyActive
    ) {
        return new SchuleOptionListJson(schuleMapper.findAllOptions(onlyActive));
    }

    @GET
    @Path("/{schuleId}/klassen")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public KlasseListJson findSchuleKlassen(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId
    ) {
        return new KlasseListJson(klasseMapper.findBySchule(schuleId));
    }

    @GET
    @Path("/{schuleId}/klassen/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public KlasseOptionListJson findSchuleKlassenOptions(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId,
        @NotNull @QueryParam("onlyActive") boolean onlyActive
    ) {
        return new KlasseOptionListJson(klasseMapper.findOptionsBySchule(schuleId, onlyActive));
    }

    @GET
    @Path("/{schuleId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public SchuleJson getSchule(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId
    ) {
        return schuleMapper.getById(schuleId);
    }

    @POST
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public SchuleJson createSchule(
        @NotNull @NonNull @Valid SchuleSaveJson schuleSaveJson
    ) {
        return schuleMapper.create(schuleSaveJson);
    }

    @PUT
    @NonNull
    @Path("/{schuleId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public SchuleJson updateSchule(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId,
        @NotNull @NonNull @Valid SchuleSaveJson schuleSaveJson
    ) {
        return schuleMapper.update(schuleId, schuleSaveJson);
    }

    @PUT
    @Path("/{schuleId}/schuleaktivstatus")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public SchuleJson updateSchuleAktivStatus(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId,
        @NotNull @NonNull @Valid EntityAktivStatusSaveJson saveJson
    ) {
        return schuleMapper.changeSchuleAktivStatus(schuleId, saveJson);
    }

    @DELETE
    @Path("/{schuleId}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public void removeSchule(
        @NotNull @NonNull @PathParam("schuleId") ID<Schule> schuleId
    ) {
        schuleMapper.remove(schuleId);
    }
}
