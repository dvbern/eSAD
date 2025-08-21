package ch.dvbern.oss.hemed.esc.api.externalimportinfojson;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalImportInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -3712635907225593561L;

    /**
     * Id in external System. e.g. Escada
     */
    @IgnoreForIdClassTest
    @Nullable
    @Column(nullable = true, length = DbConst.DB_UUID_LENGTH)
    private String idInExternalSystem;

    /**
     * true if the Kind is aktiv in the 'external' system.
     */
    @NotNull
    @Column(nullable = false)
    private boolean aktivInExternalSystem = true;

    /**
     * Timestamp of the first time the Kind was synchronized with the external system.
     */
    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampFirstImport;

    /**
     * Timestamp of the last time the Kind was synchronized with the external system.
     */
    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampLatestImport;
}
