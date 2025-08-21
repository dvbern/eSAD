package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfungIntern;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

/**
 * This Entity represents Impfungen that are planned or have been done during an Untersuchung
 */
@Entity
@Table(indexes = { @Index(name = "ix_untersuchungimpfungintern_untersuchungimpfungen",
    columnList = "untersuchungImpfungen_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_impfstoff", columnList = "impfstoff_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_impfstoffcharge", columnList = "charge_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_benutzer_erfasst", columnList = "userErfasst_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_benutzer_durchgefuehrt", columnList = "userDurchgefuehrt_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_anlass", columnList = "anlassImpfungErfasst_id, id"),
    @Index(name = "ix_untersuchungimpfungintern_anlassdurchgefuehrt", columnList = "anlassImpfungDurchgefuehrt_id, id"),
})
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfungIntern
public class UntersuchungImpfungIntern extends AbstractTenantEntity<UntersuchungImpfungIntern> {

    @Serial
    private static final long serialVersionUID = 4719335518255035134L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_untersuchungimpfungen"), nullable =
        false)
    private UntersuchungImpfungen untersuchungImpfungen;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_impfstoff"), nullable = false)
    private Impfstoff impfstoff;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_impfstoffcharge"), nullable = true)
    private ImpfstoffCharge charge;

    @Nullable
    @Column(nullable = true)
    private LocalDate datumImpfung;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_benutzer_erfasst"), nullable = false)
    private Benutzer userErfasst;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_benutzer_durchgefuehrt"), nullable =
        true)
    private Benutzer userDurchgefuehrt;

    @NotNull
    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImpfStatus impfStatus = ImpfStatus.GEPLANT;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String grundNichtDurchfuehrung;

    @NonNull
    @Valid
    @OneToMany(mappedBy = "impfung", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UntersuchungImpfungKrankheitImmunitaet> krankheitenImmunitaeten = new HashSet<>();

    @NotNull
    @NonNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_anlass"),
        nullable = false,
        updatable = false)
    private Anlass anlassImpfungErfasst;

    @Nullable
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = @ForeignKey(name = "fk_untersuchungimpfungintern_anlass_durchgefuehrt"),
        nullable = true,
        updatable = true
    )
    private Anlass anlassImpfungDurchgefuehrt;

    @Nullable
    @Column(nullable = true)
    private OffsetDateTime timestampLastManualChange;

    @NonNull
    public static UntersuchungImpfungIntern forUntersuchungImpfung(
        @NonNull UntersuchungImpfungen untersuchungImpfung) {
        UntersuchungImpfungIntern impfung = new UntersuchungImpfungIntern();
        impfung.setUntersuchungImpfungen(untersuchungImpfung);
        return impfung;
    }
}
