package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.io.Serial;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfstatusKrankheitPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_untersuchungimpfstatuskrankheit_untersuchungimpfungen", columnList = "untersuchungImpfungen_id, id"),
        @Index(name = "ix_untersuchungimpfstatuskrankheit_untersuchungimpfungen_krankh", columnList = "untersuchungImpfungen_id, krankheit, id"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_untersuchungimpfstatuskrankheit_untersuchungimpfungen_krankh", columnNames = {"untersuchungImpfungen_id", "krankheit"})
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfstatusKrankheitPresentIfReq
public class UntersuchungImpfstatusKrankheit extends AbstractTenantEntity<UntersuchungImpfstatusKrankheit>
        implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 4719335518255035134L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfstatuskrankheit_untersuchungimpfungen"), nullable = false)
    private UntersuchungImpfungen untersuchungImpfungen;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Krankheit krankheit;

    @Nullable
    @Column(nullable = true)
    private Integer anzahlImpfungen;

    @Nullable
    @Column(nullable = true)
    private LocalDate datumLetzteImpfung;

    @Column(nullable = false)
    private boolean impfschutzGegeben = false;

    @Column(nullable = false)
    private boolean impfempfehlung = false;

    @Column(nullable = false)
    private boolean impfentscheid = false;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampLastManualChange;


    @NonNull
    public static UntersuchungImpfstatusKrankheit forKrankheit(
        @NonNull UntersuchungImpfungen untersuchungImpfungen,
        @NonNull Krankheit krankheit
    ) {
        UntersuchungImpfstatusKrankheit impfstatus = new UntersuchungImpfstatusKrankheit();
        impfstatus.setUntersuchungImpfungen(untersuchungImpfungen);
        impfstatus.setKrankheit(krankheit);
        return impfstatus;
    }
}
