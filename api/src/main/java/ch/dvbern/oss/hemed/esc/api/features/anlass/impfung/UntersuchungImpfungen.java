package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfungenPresentIfReq;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import java.io.Serial;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_untersuchungimpfungen_visum_1", columnList = "visum1_id, id"),
        @Index(name = "ix_untersuchungimpfungen_visum_2", columnList = "visum2_id, id"),
        @Index(name = "ix_untersuchungimpfungen_untersuchungimpfungenmassnahmen", columnList = "untersuchungimpfungenmassnahmen_id, id"),
        @Index(name = "ix_untersuchungimpfungen_untersuchungimpfungendataminimal", columnList = "untersuchungimpfungendataminimal_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfungenPresentIfReq
public class UntersuchungImpfungen extends AbstractEntity<UntersuchungImpfungen>
        implements EntityWithOptimisticLocking {

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
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
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

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungen_untersuchungimpfungendataminimal"), nullable = true)
    private UntersuchungImpfungenDataMinimal untersuchungImpfungenDataMinimal;


    @NonNull
    public static UntersuchungImpfungen forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungImpfungen impfungen = new UntersuchungImpfungen();
        impfungen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungImpfungen(impfungen);
        return impfungen;
    }
}
