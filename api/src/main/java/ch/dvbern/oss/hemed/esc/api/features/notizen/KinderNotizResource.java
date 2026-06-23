package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizListJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.stream.Streams;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Slf4j
@Path("/dossiers/{kindId}/notizen")
@JaxResource
@Tag(name = OpenApiConst.Tags.TAG_NOTIZEN)
@RequiredArgsConstructor
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
public class KinderNotizResource {

    private final NotizMapper notizMapper;
    private final DossierService dossierService;
    private final AuthorizerService authorizerService;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;

    @Context
    private HttpServerResponse response;


    @GET
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    public NotizListJson findAllForKind(
        @PathParam("kindId") @NotNull @NonNull ID<Kind> kindId
    ) {
        boolean isZentraleOrganisation = tenantSettingsService.isZentraleOrganisation();

        return new NotizListJson(notizMapper.findAllForKindBerechtigt(
            kindId,
            notiz -> {
                // Wird bewusst nur für diese Schnittstelle angewendet, die einzelnen Endpoints für Details und die schreibenden Endpoints sollen weiterhin geprüft werden.
                if (isZentraleOrganisation) {
                    return true;
                }
                return authorizerService.hasReadAuthorizationNotiz(
                    notiz,
                    List.of(
                        Berechtigung.KINDER_ALLE_NOTIZEN_LESEN,
                        Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN));
            }
        ));
    }

    @POST
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public NotizJson create(
        @PathParam("kindId") @NotNull @NonNull ID<Kind> kindId,
        @Nullable @RestForm("file") final FileUpload fileUpload,
        @Nullable @RestForm("file2") final FileUpload fileUpload2,
        @Nullable @RestForm("file3") final FileUpload fileUpload3,
        @Nullable @RestForm("file4") final FileUpload fileUpload4,
        @NotNull @NonNull @Valid @RestForm("notizCreateJson") @PartType(MediaType.APPLICATION_JSON) final NotizSaveJson createJson
    ) {
        ID<Dossier> dossierId = dossierService.getDossierIdByKind(kindId);
        List<FileUpload> fileUploads = Streams.of(fileUpload, fileUpload2, fileUpload3, fileUpload4)
            .filter(Objects::nonNull)
            .toList();
        NotizJson notizJson = notizMapper.create(
            createJson,
            dossierId,
            fileUploads,
            notiz -> authorizerService.requireWriteAuthorizationNotiz(
                notiz,
                List.of(Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)));
        response.setStatusCode(Status.CREATED.getStatusCode());
        return notizJson;
    }
}
