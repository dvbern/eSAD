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

package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungGroesseGewichtPresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;

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
@CheckUntersuchungGroesseGewichtPresentIfReq
public class UntersuchungGroesseGewicht extends AbstractEntity<UntersuchungGroesseGewicht>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungGroesseGewicht")
    private Untersuchung untersuchung;

    @Nullable
    @Column(nullable = true)
    private Integer groesse;

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "groessePerzentileSingleValue", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<PerzentileGroesse> groessePerzentileSingleValue = SingleValueEnhanced.of();

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "groessePerzentileMultiValue", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<PerzentileGroesse> groessePerzentileMultiValue = MultiValueEnhanced.of();

    @Nullable
    @Column(nullable = true, precision = DbConst.DB_DEFAULT_PRECISION, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal gewicht;

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "gewichtPerzentileSingleValue", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<PerzentileGewicht> gewichtPerzentileSingleValue = SingleValueEnhanced.of();

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "gewichtPerzentileMultiValue", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<PerzentileGewicht> gewichtPerzentileMultiValue = MultiValueEnhanced.of();

    @Nullable
    @Column(nullable = true)
    private BigDecimal bmi;

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "bmiPerzentileSingleValue", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<PerzentileBmi> bmiPerzentileSingleValue = SingleValueEnhanced.of();

    @Nullable
    @Embedded
    @Valid
    @AttributeOverride(name = "value",
        column = @Column(name = "bmiPerzentileMultiValue", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<PerzentileBmi> bmiPerzentileMultiValue = MultiValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    public void setGewicht(@Nullable BigDecimal newGewicht) {
        this.gewicht = GroesseGewichtUtil.roundScaleOne(newGewicht, RoundingMode.HALF_UP);
    }

    @NonNull
    public BigDecimal getAlter() {
        return DateUtil.calculateAlter(
            untersuchung.getEinladungRequired().getDossier().getKind().getGeburtsdatum(),
            untersuchung.getAnlass().getDatum()
        );
    }

    @NonNull
    public String getAlterDuodezimal() {
        return DateUtil.calculateAlterDuodezimal(
            untersuchung.getEinladungRequired().getDossier().getKind().getGeburtsdatum(),
            untersuchung.getAnlass().getDatum()
        );
    }

    @NonNull
    public static UntersuchungGroesseGewicht forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungGroesseGewicht groesseGewicht = new UntersuchungGroesseGewicht();
        groesseGewicht.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungGroesseGewicht(groesseGewicht);
        return groesseGewicht;
    }

    @NonNull
    public static UntersuchungGroesseGewicht forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungGroesseGewicht groesseGewicht = untersuchung.getUntersuchungGroesseGewicht();
        if (groesseGewicht == null) {
            groesseGewicht = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();

        groesseGewicht.setGroessePerzentileSingleValue(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtGroessePerzentileSingleValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getGroessePerzentileSingleValue,
                PerzentileGroesse.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        groesseGewicht.setGroessePerzentileMultiValue(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtGroessePerzentileMultiValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getGroessePerzentileMultiValue,
                PerzentileGroesse.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        groesseGewicht.setGewichtPerzentileSingleValue(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtGewichtPerzentileSingleValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getGewichtPerzentileSingleValue,
                PerzentileGewicht.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        groesseGewicht.setGewichtPerzentileMultiValue(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtGewichtPerzentileMultiValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getGewichtPerzentileMultiValue,
                PerzentileGewicht.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        groesseGewicht.setBmiPerzentileSingleValue(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtBmiPerzentileSingleValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getBmiPerzentileSingleValue,
                PerzentileBmi.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        groesseGewicht.setBmiPerzentileMultiValue(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getGroesseGewichtBmiPerzentileMultiValue,
                groesseGewicht,
                UntersuchungGroesseGewicht::getBmiPerzentileMultiValue,
                PerzentileBmi.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return groesseGewicht;
    }
}

