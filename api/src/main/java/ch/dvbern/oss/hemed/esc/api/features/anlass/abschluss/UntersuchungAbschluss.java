package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungAbschlussPresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@CheckUntersuchungAbschlussPresentIfReq
public class UntersuchungAbschluss extends AbstractEntity<UntersuchungAbschluss>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7537304981837013143L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungAbschluss")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "ueberweisungErfolgt", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<AbschlussResultat> ueberweisungErfolgt = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "aerztlicheValidierung", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<AbschlussResultat> aerztlicheValidierungErforderlich = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @NonNull
    @NotNull
    @AttributeOverride(name = "value",
        column = @Column(name = "beratungDurchgefuehrt", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<AbschlussResultat> beratungDurchgefuehrt = SingleValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String beurteilungEmpfohleneMassnahmen;

    @NonNull
    public static UntersuchungAbschluss forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungAbschluss abschluss = new UntersuchungAbschluss();
        abschluss.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungAbschluss(abschluss);
        return abschluss;
    }

    @NonNull
    public static UntersuchungAbschluss forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungAbschluss abschluss = untersuchung.getUntersuchungAbschluss();
        if (abschluss == null) {
            abschluss = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();

        abschluss.setUeberweisungErfolgt(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getAbschlussUeberweisungErfolgt,
                abschluss,
                UntersuchungAbschluss::getUeberweisungErfolgt,
                AbschlussResultat.KEINE_ANGABE,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );

        abschluss.setAerztlicheValidierungErforderlich(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getAbschlussAerztlicheValidierungErforderlich,
                abschluss,
                UntersuchungAbschluss::getAerztlicheValidierungErforderlich,
                AbschlussResultat.KEINE_ANGABE,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );

        abschluss.setBeratungDurchgefuehrt(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getAbschlussBeratungDurchgefuehrt,
                abschluss,
                UntersuchungAbschluss::getBeratungDurchgefuehrt,
                AbschlussResultat.KEINE_ANGABE,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );

        return abschluss;
    }
}
