package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.json;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.EscadaSyncEvent;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ImportStatus;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ItemSyncResultJson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Data
public class EscadaImportResultJson {

    @Schema(nullable = false)
    private final ID<EscadaSyncEvent> escadaSyncEventID;

    @Schema(nullable = false)
    private final LocalDateTime importTimestamp;

    @Schema(nullable = false)
    private final Duration importDuration;

    @Schema(nullable = false)
    private final List<ItemSyncResultJson<Schule>> schuleImportResultJsons;

    @Schema(nullable = false)
    private final List<ItemSyncResultJson<Klasse>> klasseImportResultJsons;

    @Schema(nullable = false)
    private final List<ItemSyncResultJson<Lehrperson>> lehrpersonResultJsons;

    @Schema(nullable = false)
    private final List<ItemSyncResultJson<Kind>> kindImportResultJsons;

    @Schema(nullable = false)
    private final List<ItemSyncResultJson<KontaktPersonKind>> kontaktPersonenKindImportResultJsons;

    /**
     * This copies the import result but only keeps the items that have an error.
     */
    @NonNull
    public EscadaImportResultJson toErrorFilteredEscadaImportResultJson() {
        return new EscadaImportResultJson(
            escadaSyncEventID,
            importTimestamp,
            importDuration,
            schuleImportResultJsons.stream().filter(ItemSyncResultJson::isError).toList(),
            klasseImportResultJsons.stream().filter(ItemSyncResultJson::isError).toList(),
            lehrpersonResultJsons.stream().filter(ItemSyncResultJson::isError).toList(),
            kindImportResultJsons.stream().filter(ItemSyncResultJson::isError).toList(),
            kontaktPersonenKindImportResultJsons.stream().filter(ItemSyncResultJson::isError).toList()
        );
    }

    /**
     * This checks if any of the import results contains an error.
     */
    @JsonIgnore
    public boolean hasErrors() {
        return schuleImportResultJsons.stream().anyMatch(ItemSyncResultJson::isError)
            || klasseImportResultJsons.stream().anyMatch(ItemSyncResultJson::isError)
            || lehrpersonResultJsons.stream().anyMatch(ItemSyncResultJson::isError)
            || kindImportResultJsons.stream().anyMatch(ItemSyncResultJson::isError)
            || kontaktPersonenKindImportResultJsons.stream().anyMatch(ItemSyncResultJson::isError);
    }

    @JsonIgnore
    @NonNull
    public String getImportSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("ESCADA Import Finished at: ")
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(importTimestamp))
            .append(". ");
        sb.append("Duration: ").append(importDuration.toSeconds()).append(" seconds.\n");
        sb.append("ImportEventID: ").append(escadaSyncEventID.getUUID().toString()).append('\n');
        constructSummaryForItemList(Schule.class, schuleImportResultJsons, sb);
        constructSummaryForItemList(Klasse.class, klasseImportResultJsons, sb);
        constructSummaryForItemList(Lehrperson.class, lehrpersonResultJsons, sb);
        constructSummaryForItemList(Kind.class, kindImportResultJsons, sb);
        constructSummaryForItemList(KontaktPersonKind.class, kontaktPersonenKindImportResultJsons, sb);
        return sb.toString();
    }

    private <T extends AbstractEntity<T>> void constructSummaryForItemList(
        @NonNull Class<T> clazz,
        @NonNull List<ItemSyncResultJson<T>> itemSyncResults,
        @NonNull StringBuilder sb
    ) {
        sb.append("Importresult for ").append(clazz.getSimpleName()).append(": ")
            .append(itemSyncResults.size()).append(" items processed. ");
        Map<ImportStatus, Long> statusCount = itemSyncResults.stream()
            .collect(Collectors.groupingBy(ItemSyncResultJson::getStatus, Collectors.counting()));
        sb.append("OK: ").append(statusCount.getOrDefault(ImportStatus.OK, 0L)).append(", ");
        sb.append("ERROR: ").append(statusCount.getOrDefault(ImportStatus.ERROR, 0L)).append('\n');
    }
}
