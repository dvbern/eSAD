package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.function.Predicate;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.DossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.EsadIdJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindListJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveResultJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSchuleAndKlasseSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSucheOptionsJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindListJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KindSaveModeDuplikat;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;

import io.micrometer.core.annotation.Timed;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/kinder")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KINDER)
@RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("KindResourceMS")
public class KindResource {

    private static final List<Berechtigung> KINDER_SCHREIBEN_BERECHTIGUNGEN =
        List.of(Berechtigung.KINDER_SCHREIBEN_MEDIZINISCH, Berechtigung.KINDER_SCHREIBEN_NICHT_MEDIZINISCH);

    private final KindMapper kindMapper;
    private final KontaktPersonKindMapper kontaktPersonKindMapper;
    private final DossierMapper dossierMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @POST
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindSaveResultJson createKindAndDossier(
        @NotNull @NonNull @Valid KindSaveJson kindJson,
        @NotNull @QueryParam("kindSaveMode") KindSaveModeDuplikat kindSaveMode,
        @Nullable @QueryParam("arztId") ID<Benutzer> zustaendigerArztId,
        @Nullable @QueryParam("mpaId") ID<Benutzer> zustaendigeMpaId
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        KindSaveResultJson kindSaveResultJson = kindMapper.create(kindJson, kindSaveMode);
        return createDossierAndSaveZustaendigePersonen(kindSaveResultJson, zustaendigerArztId, zustaendigeMpaId);
    }

    @PUT
    @Path("/{kindId}/schule-klasse")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindSaveResultJson updateSchuleAndKlasse(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindID,
        @NotNull @NonNull @Valid KindSchuleAndKlasseSaveJson kindSchuleAndKlasseSaveJson
    ) {
        // Schulwechsel ist ein Zustaendigkeits-Handoff, daher hier eine sprechende Validation-Meldung (400)
        // statt der generischen Berechtigungs-Verletzung
        if (!authorizerService.hasWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN)) {
            throw new AppValidationException(AppValidationMessage.zustaendigkeitCanOnlyBeChangedByZustaendiger());
        }
        return kindMapper.updateSchuleAndKlasse(kindID, kindSchuleAndKlasseSaveJson);
    }

    @PUT
    @Path("/{kindId}")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindSaveResultJson updateKindAndVerantwortlichePersonen(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindID,
        @NotNull @NonNull @Valid KindSaveJson kindJson,
        @NotNull @QueryParam("kindSaveMode") KindSaveModeDuplikat kindSaveMode,
        @Nullable @QueryParam("arztId") ID<Benutzer> zustaendigerArztId,
        @Nullable @QueryParam("mpaId") ID<Benutzer> zustaendigeMpaId
    ) {
        authorizerService.requireWriteAuthorizationKind(kindID, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        KindSaveResultJson kindSaveResultJson = kindMapper.update(kindID, kindJson, kindSaveMode);
        return createDossierAndSaveZustaendigePersonen(kindSaveResultJson, zustaendigerArztId, zustaendigeMpaId);
    }

    @NonNull
    private KindSaveResultJson createDossierAndSaveZustaendigePersonen(@NonNull KindSaveResultJson kindSaveResultJson, @Nullable ID<Benutzer> zustaendigerArztId, @Nullable ID<Benutzer> zustaendigeMpaId) {
        // Wenn das KindSaveResult null ist, war es ein Duplikat und es wurde kein Kind gespeichert
        if (kindSaveResultJson.getKindJson() == null) {
            return kindSaveResultJson;
        }
        Predicate<Dossier> canReadMedizinischeDaten = predicateReadMedizinischeDaten();
        DossierJson dossierJson = dossierMapper.getOrCreateByKind(
            kindSaveResultJson.getKindJson().getId(),
            canReadMedizinischeDaten);
        DossierJson savedDossierJson = dossierMapper.updateDossierVerantwortlicherArztUndMpa(
            dossierJson.getId(),
            zustaendigerArztId,
            zustaendigeMpaId,
            canReadMedizinischeDaten);
        return new KindSaveResultJson(
            kindSaveResultJson.getIdsOfPotentialDuplicates(),
            savedDossierJson.getKind(),
            savedDossierJson
        );
    }

    @PUT
    @Path("/{kindId}/kindaktivstatus")
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindJson updateKindAktivStatus(
        @NonNull @NotNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @Valid EntityAktivStatusSaveJson saveJson
    ) {
        authorizerService.requireWriteAuthorizationKind(kindId, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        return kindMapper.changeKindAktivStatus(kindId, saveJson);
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
    public KindListJson findAllKinder() {
        return new KindListJson(kindMapper.findAll());
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
    public KindListJson findKinderBySchuleOrKlasse(
        @Nullable @QueryParam("schuleId") ID<Schule> schuleId,
        @Nullable @QueryParam("klasseId") ID<Klasse> klasseId
    ) {
        KindSucheOptionsJson options = new KindSucheOptionsJson(schuleId, klasseId);
        return new KindListJson(kindMapper.findByOptions(options));
    }

    @GET
    @Path("/{kindId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
    public KindJson getKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        return kindMapper.getById(kindId);
    }

    @GET
    @Path("/{kindId}/kontaktpersonen")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH })
    public KontaktPersonKindListJson findAktivKindKontaktPersonen(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        return new KontaktPersonKindListJson(kontaktPersonKindMapper.findAktivByKind(kindId));
    }

    @PUT
    @Path("/{kindId}/esad-id-generated")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindJson generateEsadIdForKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        authorizerService.requireWriteAuthorizationKind(kindId, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        return kindMapper.generateEsadIdForKind(kindId);
    }

    @PUT
    @Path("/{kindId}/esad-id-manual")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
    public KindJson setEsadIdForKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @Valid EsadIdJson esadIdJson
    ) {
        authorizerService.requireWriteAuthorizationKind(kindId, KINDER_SCHREIBEN_BERECHTIGUNGEN);
        return kindMapper.setEsadIdForKind(kindId, esadIdJson);
    }

    @NonNull
    private Predicate<Dossier> predicateReadMedizinischeDaten() {
        return dossier -> authorizerService.hasReadAuthorizationDossier(
            dossier,
            List.of(Berechtigung.KINDER_LESEN_MEDIZINISCH),
            null);
    }
}
