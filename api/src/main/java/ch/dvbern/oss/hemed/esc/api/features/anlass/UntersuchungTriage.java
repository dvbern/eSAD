package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

import java.io.Serial;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungTriage extends AbstractTenantEntity<UntersuchungTriage> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -2131698447532258358L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungTriage")
    private Untersuchung untersuchung;

    @Column(nullable = false)
    private boolean triageSu = false; // triage wurde waehrend der Schuluntersuchung gemacht

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String triageSuBemerkungen;

    @Column(nullable = false)
    private boolean triageMpa = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String triageMpaBemerkungen;

    @Column(nullable = false)
    private boolean befundSu = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String befundSuBemerkungen;

    @Column(nullable = false)
    private boolean wunschSchueler = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String wunschSchuelerBemerkungen;

    @Column(nullable = false)
    private boolean vorinformationLehrperson = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String vorinformationLehrpersonBemerkungen;

    @NonNull
    public static UntersuchungTriage forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungTriage triage = new UntersuchungTriage();
        triage.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungTriage(triage);
        return triage;
    }

    @NonNull
    public static UntersuchungTriage forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungTriage triage = untersuchung.getUntersuchungTriage();
        if (triage == null) {
            triage = forUntersuchung(untersuchung);
        }
        return triage;
    }
}
