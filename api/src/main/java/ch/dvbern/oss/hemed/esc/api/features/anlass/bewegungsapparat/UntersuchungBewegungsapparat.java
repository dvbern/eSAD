package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungBewegungsapparatPresentIfReq;
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
@CheckUntersuchungBewegungsapparatPresentIfReq
public class UntersuchungBewegungsapparat extends AbstractEntity<UntersuchungBewegungsapparat>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1120347899064440853L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungBewegungsapparat")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "skoliose", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Skoliose> skoliose = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "haltungsschwaeche", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Haltungsschwaeche> haltungsschwaeche = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "verkuerzungHamstringmuskulatur", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<VerkuerzungHamstringmuskulatur> verkuerzungHamstringmuskulatur =
        SingleValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungBewegungsapparat forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungBewegungsapparat bewegungsapparat = new UntersuchungBewegungsapparat();
        bewegungsapparat.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungBewegungsapparat(bewegungsapparat);
        return bewegungsapparat;
    }

    @NonNull
    public static UntersuchungBewegungsapparat forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungBewegungsapparat bewegungsapparat = untersuchung.getUntersuchungBewegungsapparat();
        if (bewegungsapparat == null) {
            bewegungsapparat = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        bewegungsapparat.setSkoliose(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getBewegungsapparatSkoliose,
                bewegungsapparat,
                UntersuchungBewegungsapparat::getSkoliose,
                Skoliose.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        bewegungsapparat.setHaltungsschwaeche(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getBewegungsapparatHaltungsschwaeche,
                bewegungsapparat,
                UntersuchungBewegungsapparat::getHaltungsschwaeche,
                Haltungsschwaeche.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        bewegungsapparat.setVerkuerzungHamstringmuskulatur(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getBewegungsapparatVerkuerzungHamstringmuskulatur,
                bewegungsapparat,
                UntersuchungBewegungsapparat::getVerkuerzungHamstringmuskulatur,
                VerkuerzungHamstringmuskulatur.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );

        return bewegungsapparat;
    }
}
