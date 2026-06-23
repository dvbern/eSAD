package ch.dvbern.oss.hemed.esc.api.features.notizen.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerNameJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelMapper;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Schlankes DTO für die Anzeige einer Notiz in der Notizen-Liste/Tabelle.
 * Enthält nur die Felder, die in der Tabelle dargestellt, sortiert oder gefiltert werden.
 */
@Value
@JaxDTO
public class NotizListItemJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private ID<Notiz> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private EntityInfoJson entityInfo;

    @NotNull
    @NotEmpty
    @NonNull
    @Schema(required = true)
    private String titel;

    @NotNull
    @NonNull
    @Schema(required = true)
    private NotizStatus status;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private BenutzerNameJson verantwortlicher;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private List<LabelJson> labels;

    @Nullable
    @Schema(required = false)
    private String kindFullName;

    @Schema(required = true)
    private boolean hasAnhang;

    @SuppressWarnings("java:S107" /*many params is the point here */)
    @QueryProjection
    public NotizListItemJson(
        @NonNull UUID id,
        @NonNull OffsetDateTime timestampErstellt,
        @NonNull OffsetDateTime timestampMutiert,
        @NonNull String userErstellt,
        @NonNull String userMutiert,
        @NonNull String titel,
        @NonNull NotizStatus status,
        @NonNull UUID verantwortlicherId,
        @NonNull String verantwortlicherFullName,
        @NonNull List<Label> labels,
        @Nullable String kindFullName,
        boolean hasAnhang
    ) {
        this.id = ID.of(id, Notiz.class);
        this.entityInfo = new EntityInfoJson(timestampErstellt.atZoneSameInstant(DateConst.ZONE_ID).toLocalDateTime(), timestampMutiert.atZoneSameInstant(DateConst.ZONE_ID).toLocalDateTime(), userErstellt, userMutiert);
        this.titel = titel;
        this.status = status;
        this.verantwortlicher = new BenutzerNameJson(ID.of(verantwortlicherId, Benutzer.class), verantwortlicherFullName);
        this.labels = labels.stream().map(LabelMapper::toJson).toList();
        this.kindFullName = kindFullName;
        this.hasAnhang = hasAnhang;
    }
}
