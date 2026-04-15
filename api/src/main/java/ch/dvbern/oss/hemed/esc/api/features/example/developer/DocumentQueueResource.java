package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueCsvListSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueCsvSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueIDListJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueMassenbriefSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueSpssSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/documentqueue")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_DOCUMENT_QUEUE)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Slf4j
public class DocumentQueueResource {

    private final DocumentQueueService documentQueueService;
    private final DocumentQueueMapper documentQueueMapper;
    private final FileStoreService fileStoreService;
    private final AuthorizerService authorizerService;

    @POST
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.SPSS_EXPORT, BerechtigungName.INDIVIDUELLE_REPORTS })
    public Response create(@NotNull @NonNull @Valid DocumentQueueSpssSaveJson saveJson) {
        documentQueueMapper.create(saveJson);
        return Response.status(Status.CREATED.getStatusCode()).build();
    }

    @GET
    @Path("/self")
    @RolesAllowed({
        BerechtigungName.SUPPORT,
        BerechtigungName.SPSS_EXPORT,
        BerechtigungName.INDIVIDUELLE_REPORTS,
        BerechtigungName.VORLAGEN_ADMIN_LESEN })
    public DocumentQueueListJson findAllByCurrentBenutzer() {
        return documentQueueMapper.findAllByCurrentBenutzer();
    }

    @GET
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.SPSS_EXPORT, BerechtigungName.INDIVIDUELLE_REPORTS })
    public DocumentQueueListJson findAll() {
        return documentQueueMapper.findAll();
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{documentQueueId}/sync")
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.SPSS_EXPORT, BerechtigungName.INDIVIDUELLE_REPORTS })
    @NonNull
    @Blocking
    public Response downloadFile(
        @PathParam("documentQueueId") @NotNull @NonNull ID<DocumentQueue> documentQueueId
    ) {
        authorizerService.requireReadAuthorizationDocument(documentQueueId);
        FileInfo fileInfo = documentQueueService.findUploadedFileInfo(documentQueueId);
        var content = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }

    @POST
    @Path("/create-multiple-spss")
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.SPSS_EXPORT })
    public Response createMultipleSpss(@NotNull @NonNull @Valid DocumentQueueListSaveJson saveJsonList) {
        for (DocumentQueueSpssSaveJson saveJson : saveJsonList.getList()) {
            documentQueueMapper.create(saveJson);
        }
        return Response.status(Status.CREATED.getStatusCode()).build();
    }

    @POST
    @Path("/create-multiple-csv")
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.INDIVIDUELLE_REPORTS })
    public Response createMultipleCsv(@NotNull @NonNull @Valid DocumentQueueCsvListSaveJson saveJsonList) {
        for (DocumentQueueCsvSaveJson saveJson : saveJsonList.getList()) {
            documentQueueMapper.createCsv(saveJson);
        }
        return Response.status(Status.CREATED.getStatusCode()).build();
    }

    @POST
    @Path("/create-massenbrief")
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.VORLAGEN_LESEN })
    public Response createMassenbrief(
        @NotNull @NonNull @Valid DocumentQueueMassenbriefSaveJson saveJson
    ) {
        documentQueueMapper.createMassenbrief(saveJson);
        return Response.status(Status.CREATED.getStatusCode()).build();
    }

    @DELETE
    @NonNull
    @RolesAllowed({ BerechtigungName.SUPPORT, BerechtigungName.SPSS_EXPORT, BerechtigungName.INDIVIDUELLE_REPORTS })
    public DocumentQueueListJson removeDocumentQueueItems(
        @NotNull @NonNull @Valid DocumentQueueIDListJson docIdsToDelete
    ) {
        List<ID<DocumentQueue>> filterd = new ArrayList<>();
        for (ID<DocumentQueue> docId : docIdsToDelete.getList()) {
            if (authorizerService.checkReadAuthorizationDocument(docId)) {
                filterd.add(docId);
            } else {
                LOG.warn("User has no Berechtigung to delete document {}", docId.getId());
            }
        }
        documentQueueMapper.removeDocumentQueueItems(filterd);
        return findAll();
    }
}
