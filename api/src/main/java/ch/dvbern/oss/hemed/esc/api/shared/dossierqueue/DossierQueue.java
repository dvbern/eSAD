package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table
public class DossierQueue extends AbstractEntity<DossierQueue> {

    @Serial
    private static final long serialVersionUID = 2378252098966219560L;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private DossierQueueTyp dossierQueueTyp;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private DossierQueueStatus status = DossierQueueStatus.NEW;

    @NotNull
    @Column(nullable = false)
    private int errorCount;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private UUID dossierID;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_FREITEXT_LENGTH)
    private String parameters;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampResult;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_FREITEXT_LENGTH)
    private String lastError;

    @NonNull
    public static DossierQueue of(
        @NonNull UUID dossierUUID,
        @NonNull DossierQueueTyp dossierQueueTyp,
        @Nullable String parameters
    ) {
        return new DossierQueue(
            dossierQueueTyp,
            DossierQueueStatus.NEW,
            0,
            dossierUUID,
            parameters,
            null,
            null
        );
    }

    public boolean isInProcessableStatus() {
        return DossierQueueStatus.processableStatus().stream().anyMatch(s -> s == status);
    }
}
