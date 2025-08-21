package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.io.Serial;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(
    indexes = {
        @Index(name = "ix_documentqueue_filedata", columnList = "result_id, id"),
        @Index(name = "ix_documentqueue_benutzer", columnList = "benutzer_id, id"),
    }
)
public class DocumentQueue extends AbstractTenantEntity<DocumentQueue> {
    @Serial
    private static final long serialVersionUID = 2378252098966219560L;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private FileTyp type;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private DocumentQueueStatus status = DocumentQueueStatus.NEW;

    @NotNull
    @Column(nullable = false)
    private int errorCount;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_FREITEXT_LENGTH)
    private String parameters;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampResult;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_FREITEXT_LENGTH)
    private String lastError;

    @Nullable
    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_documentqueue_filedata"), nullable = true, updatable = true)
    private FileData result;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_documentqueue_benutzer"), nullable = false, updatable = false)
    private Benutzer benutzer;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AppLanguages language;

    public boolean isInProcessableStatus() {
        return DocumentQueueStatus.processableStatus().stream().anyMatch(s -> s == status);
    }
}
