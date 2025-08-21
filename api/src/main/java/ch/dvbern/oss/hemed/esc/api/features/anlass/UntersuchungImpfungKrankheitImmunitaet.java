package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

/**
 * This Entity represents Impfungen that are planned or have been done during an Untersuchung
 */
@Entity
@Table(
    indexes = {
        @Index(name = "ix_untersuchungimpfungkrankheitimmunitaet_untersuchungimpfungin", columnList = "impfung_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungImpfungKrankheitImmunitaet extends AbstractTenantEntity<UntersuchungImpfungKrankheitImmunitaet> {

    @Serial
    private static final long serialVersionUID = 8686021517090828592L;

    @NotNull
    @NonNull
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungkrankheitimmunitaet_untersuchungimpfungintern"), nullable = false, updatable = false)
    private UntersuchungImpfungIntern impfung;

    @NotNull
    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Krankheit krankheit;

    @NotNull
    @Column(nullable = false)
    private boolean immunitaetGenuegend = false;
}
