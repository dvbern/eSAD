package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReq;
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
@CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReq
public class UntersuchungSchulrelevanteBeeintraechtigung
    extends AbstractEntity<UntersuchungSchulrelevanteBeeintraechtigung>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = -9182133885971122939L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSchulrelevanteBeeintraechtigung")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "freiesGehen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<FreiesGehen> freiesGehen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "ohneStuetzradVelofahren", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<OhneStuetzradVelofahren> ohneStuetzradVelofahren = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "grafomotorik", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Grafomotorik> grafomotorik = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "feinmotorik", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Feinmotorik> feinmotorik = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "grobmotorikEinbeinhuepfen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<GrobmotorikEinbeinhuepfen> grobmotorikEinbeinhuepfen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "gleichgewichtEinbeinstand", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<GleichgewichtEinbeinstand> gleichgewichtEinbeinstand = SingleValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            new UntersuchungSchulrelevanteBeeintraechtigung();
        beeintraechtigung.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSchulrelevanteBeeintraechtigung(beeintraechtigung);
        return beeintraechtigung;
    }

    @NonNull
    public static UntersuchungSchulrelevanteBeeintraechtigung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung();
        if (beeintraechtigung == null) {
            beeintraechtigung = forUntersuchung(untersuchung);
        }

        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        beeintraechtigung.setFreiesGehen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungFreiesGehen,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getFreiesGehen,
                FreiesGehen.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        beeintraechtigung.setOhneStuetzradVelofahren(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungOhneStuetzradVelofahren,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getOhneStuetzradVelofahren,
                OhneStuetzradVelofahren.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        beeintraechtigung.setGrafomotorik(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungGrafomotorik,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getGrafomotorik,
                Grafomotorik.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        beeintraechtigung.setFeinmotorik(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungFeinmotorik,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getFeinmotorik,
                Feinmotorik.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        beeintraechtigung.setGrobmotorikEinbeinhuepfen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungGrobmotorik,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getGrobmotorikEinbeinhuepfen,
                GrobmotorikEinbeinhuepfen.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        beeintraechtigung.setGleichgewichtEinbeinstand(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungBeeintraechtigungGleichgewicht,
                beeintraechtigung,
                UntersuchungSchulrelevanteBeeintraechtigung::getGleichgewichtEinbeinstand,
                GleichgewichtEinbeinstand.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return beeintraechtigung;
    }
}
