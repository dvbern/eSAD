package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import java.io.Serial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfungenNurUnvollstaendigePresentIfReq
public class UntersuchungImpfungenNurUnvollstaendige
    extends AbstractEntity<UntersuchungImpfungenNurUnvollstaendige>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7512251037224910832L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungImpfungenNurUnvollstaendige")
    private UntersuchungImpfungen untersuchungImpfungen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private ImpfausweisVollstaendig impfausweisVollstaendig;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_LONG_ENUM_LENGTH, nullable = true)
    private ImpfausweisGrundUnvollstaendig impfausweisGrundUnvollstaendig;


    @NonNull
    public static UntersuchungImpfungenNurUnvollstaendige forUntersuchung(@NonNull UntersuchungImpfungen untersuchung) {
        UntersuchungImpfungenNurUnvollstaendige impfungenNurUnvollstaendige = new UntersuchungImpfungenNurUnvollstaendige();
        impfungenNurUnvollstaendige.setUntersuchungImpfungen(untersuchung);
        untersuchung.setUntersuchungImpfungenNurUnvollstaendige(impfungenNurUnvollstaendige);
        return impfungenNurUnvollstaendige;
    }

    @NonNull
    @SuppressWarnings({
        "java:S1066", /* Code is more understandable if statements not merged */
    })
    public static UntersuchungImpfungenNurUnvollstaendige forUntersuchungAbschliessen(
        @NonNull UntersuchungImpfungen untersuchung
    ) {
        UntersuchungImpfungenNurUnvollstaendige impfungenNurUnvollstaendige = untersuchung.getUntersuchungImpfungenNurUnvollstaendige();
        if (impfungenNurUnvollstaendige == null) {
            impfungenNurUnvollstaendige = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getUntersuchung().getAnlass().getAnlassConfig();
        if (config.getImpfungenNurUnvollstaendige().isRequired()) {
            if (impfungenNurUnvollstaendige.getImpfausweisVollstaendig() == null) {
                impfungenNurUnvollstaendige.setImpfausweisVollstaendig(ImpfausweisVollstaendig.KEINE_ANGABE);
            }
        }
        return impfungenNurUnvollstaendige;
    }
}
