/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.sehen;

import java.io.Serial;
import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSehenBeurteilungPresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
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
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungSehenBeurteilungPresentIfReq
public class UntersuchungSehenBeurteilung extends AbstractEntity<UntersuchungSehenBeurteilung>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSehenBeurteilung")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "brilleLinsen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<BrilleLinsenResultat> brilleLinsen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "stereotest", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<StereotestResultat> stereotest = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "farbsehen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<FarbsehenResultat> farbsehen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "verwendeteTafel", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<VerwendetTafelResultat> verwendeteTafel = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "fernvisus", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<DistanzSehenResultat> fernvisus = MultiValueEnhanced.of();

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusLinks;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "fernvisusaugelinks", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<DistanzSehenAugeEinzelnResultat> fernvisusAugeLinks = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "fernvisusaugerechts", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<DistanzSehenAugeEinzelnResultat> fernvisusAugeRechts = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "nahvisus", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<DistanzSehenResultat> nahvisus = MultiValueEnhanced.of();

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusLinks;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "motilitaet", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<MotilitaetResultat> motilitaet = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "strabismus", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<StrabismusResultat> strabismus = MultiValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "strabismusAuspraegung", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<StrabismusAuspraegungResultat> strabismusAuspraegung = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "strabismusAuftreten", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<StrabismusAuftretenResultat> strabismusAuftreten = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "inspektion", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<InspektionKonvergenzResultat> inspektion = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "konvergenz", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<InspektionKonvergenzResultat> konvergenz = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "empfehlung", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<SehenEmpfehlung> empfehlung = MultiValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung sehen = new UntersuchungSehenBeurteilung();
        sehen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSehenBeurteilung(sehen);
        return sehen;
    }

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungSehenBeurteilung sehen = untersuchung.getUntersuchungSehenBeurteilung();
        if (sehen == null) {
            sehen = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();

        sehen.setBrilleLinsen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenBrilleLinsen,
                sehen,
                UntersuchungSehenBeurteilung::getBrilleLinsen,
                BrilleLinsenResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setStereotest(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenStereoTest,
                sehen,
                UntersuchungSehenBeurteilung::getStereotest,
                StereotestResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setFarbsehen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenFarbsehen,
                sehen,
                UntersuchungSehenBeurteilung::getFarbsehen,
                FarbsehenResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setVerwendeteTafel(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenTafel,
                sehen,
                UntersuchungSehenBeurteilung::getVerwendeteTafel,
                VerwendetTafelResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setFernvisus(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenFernvisus,
                sehen,
                UntersuchungSehenBeurteilung::getFernvisus,
                DistanzSehenResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setFernvisusAugeLinks(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenFernvisusAugeEinzeln,
                sehen,
                UntersuchungSehenBeurteilung::getFernvisusAugeLinks,
                DistanzSehenAugeEinzelnResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setFernvisusAugeRechts(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenFernvisusAugeEinzeln,
                sehen,
                UntersuchungSehenBeurteilung::getFernvisusAugeRechts,
                DistanzSehenAugeEinzelnResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setNahvisus(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenNahvisus,
                sehen,
                UntersuchungSehenBeurteilung::getNahvisus,
                DistanzSehenResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setMotilitaet(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenMotilitaet,
                sehen,
                UntersuchungSehenBeurteilung::getMotilitaet,
                MotilitaetResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setStrabismus(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenStrabismus,
                sehen,
                UntersuchungSehenBeurteilung::getStrabismus,
                StrabismusResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setStrabismusAuspraegung(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenStrabismusAuspraegung,
                sehen,
                UntersuchungSehenBeurteilung::getStrabismusAuspraegung,
                StrabismusAuspraegungResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setStrabismusAuftreten(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenStrabismusAuftreten,
                sehen,
                UntersuchungSehenBeurteilung::getStrabismusAuftreten,
                StrabismusAuftretenResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setInspektion(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenInspektion,
                sehen,
                UntersuchungSehenBeurteilung::getInspektion,
                InspektionKonvergenzResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        sehen.setKonvergenz(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenKonvergenz,
                sehen,
                UntersuchungSehenBeurteilung::getKonvergenz,
                InspektionKonvergenzResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return sehen;
    }
}

