package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = @Index(name = "ix_impfstoffcharge_impfstoff_id", columnList = "impfstoff_id, id"),
    uniqueConstraints = @UniqueConstraint(name = "uc_impfstoffcharge_name_impfstoff_id",
        columnNames = { "name", "impfstoff_id" })
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImpfstoffCharge extends AbstractTenantEntity<ImpfstoffCharge> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_impfstoffcharge_impfstoff"), nullable = false, updatable = false)
    private Impfstoff impfstoff;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private boolean aktiv;
}
