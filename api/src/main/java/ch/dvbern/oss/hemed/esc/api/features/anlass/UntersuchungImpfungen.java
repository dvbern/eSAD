package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
        @Index(name = "ix_untersuchungimpfungen_visum_1", columnList = "visum1_id, id"),
        @Index(name = "ix_untersuchungimpfungen_visum_2", columnList = "visum2_id, id"),
        @Index(name = "ix_untersuchungimpfungen_untersuchungimpfungenmassnahmen", columnList = "untersuchungimpfungenmassnahmen_id, id")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungImpfungen extends AbstractTenantEntity<UntersuchungImpfungen> {

    @Serial
    private static final long serialVersionUID = 4719335518255035134L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungImpfungen")
    private Untersuchung untersuchung;

    @NotNull
    @Column(nullable = false)
    private boolean impfausweisUnvollstaendig;

    @NotNull
    @Column(nullable = false)
    private boolean impfausweisNichtVorhanden;

    @NotNull
    @Column(nullable = false)
    private boolean impfausweisZweiPlusEinsSchema;

    @NotNull
    @Column(nullable = false)
    private boolean checkEinwilligungVorhanden;

    @NotNull
    @Column(nullable = false)
    private boolean checkKontraindikationenAbgeklaert;

    @NotNull
    @Column(nullable = false)
    private boolean checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String bemerkungen;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungen_visum_1"), nullable = true)
    private Visum visum1;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungen_visum_2"), nullable = true)
    private Visum visum2;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungen_untersuchungimpfungenmassnahmen"), nullable = true)
    private UntersuchungImpfungenMassnahmen untersuchungImpfungenMassnahmen;


    @NonNull
    public static UntersuchungImpfungen forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        impfungen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungImpfungen(impfungen);
        return impfungen;
    }
}
