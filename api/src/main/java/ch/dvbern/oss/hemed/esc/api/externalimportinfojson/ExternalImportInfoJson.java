package ch.dvbern.oss.hemed.esc.api.externalimportinfojson;

import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
@Schema(description = "Information about Object coming from External System")
public class ExternalImportInfoJson {

    @IgnoreForIdClassTest
    @NonNull
    @NotNull
    @Schema(required = true)
    private String idInExternalSystem;

    @NotNull
    @Schema(required = true)
    private boolean aktivInExternalSystem;

    @NonNull
    @NotNull
    @Schema(required = true)
    private OffsetDateTime timestampFirstImport;

    @NonNull
    @NotNull
    @Schema(required = true)
    private OffsetDateTime timestampLatestImport;

    @NonNull
    @NotNull
    @Schema(required = true)
    private ImportTyp importTyp;

    @Nullable
    public static ExternalImportInfoJson toJson(@Nullable ExternalImportInfo entity) {
        if (entity == null ||
            entity.getIdInExternalSystem() == null ||
            entity.getTimestampFirstImport() == null ||
            entity.getTimestampLatestImport() == null ||
            entity.getAktivInExternalSystem() == null ||
            entity.getImportTyp() == null
        ) {
            return null;
        }
        return new ExternalImportInfoJson(
            entity.getIdInExternalSystem(),
            entity.getAktivInExternalSystem(),
            entity.getTimestampFirstImport(),
            entity.getTimestampLatestImport(),
            entity.getImportTyp()
        );
    }

    @NonNull
    public static ExternalImportInfo toEntityOrCreate(
        @Nullable ExternalImportInfo externalImportInfo,
        @NonNull String idInExternalSystem,
        @NonNull OffsetDateTime now,
        @NonNull ImportTyp importTyp
    ) {
        if (externalImportInfo == null || externalImportInfo.getTimestampFirstImport() == null) {
            externalImportInfo = new ExternalImportInfo();
            externalImportInfo.setTimestampFirstImport(now); // if the import info is null, this is the first import
        }
        externalImportInfo.setIdInExternalSystem(idInExternalSystem);
        externalImportInfo.setAktivInExternalSystem(true);
        externalImportInfo.setTimestampLatestImport(now);
        externalImportInfo.setImportTyp(importTyp);
        return externalImportInfo;
    }
}

