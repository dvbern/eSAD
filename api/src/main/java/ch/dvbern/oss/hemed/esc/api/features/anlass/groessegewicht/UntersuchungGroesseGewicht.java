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

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungGroesseGewichtUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungGroesseGewichtPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private PerzentileGroesse groessePerzentile;

    @Nullable
    @Column(nullable = true, precision = DbConst.DB_DEFAULT_PRECISION, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal gewicht;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private PerzentileGewicht gewichtPerzentile;

    @Nullable
    @Column(nullable = true)
    private BigDecimal bmi;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private PerzentileBmi bmiPerzentile;

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
            untersuchung.getEinladung().getDossier().getKind().getGeburtsdatum(),
            untersuchung.getEinladung().getAnlass().getDatum()
        );
    }

    @NonNull
    public String getAlterDuodezimal() {
        return DateUtil.calculateAlterDuodezimal(
            untersuchung.getEinladung().getDossier().getKind().getGeburtsdatum(),
            untersuchung.getEinladung().getAnlass().getDatum()
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
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungGroesseGewicht groesseGewicht = untersuchung.getUntersuchungGroesseGewicht();
        if (groesseGewicht == null) {
            groesseGewicht = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        groesseGewicht.setGroessePerzentile(UntersuchungGroesseGewichtUtil.getGroessePerzentile(
            config, groesseGewicht, PerzentileGroesse.NICHT_GEPRUEFT));
        groesseGewicht.setGewichtPerzentile(UntersuchungGroesseGewichtUtil.getGewichtPerzentile(
            config, groesseGewicht, PerzentileGewicht.NICHT_GEPRUEFT));
        groesseGewicht.setBmiPerzentile(UntersuchungGroesseGewichtUtil.getBmiPerzentile(
            config, groesseGewicht, PerzentileBmi.NICHT_GEPRUEFT));
        return groesseGewicht;
    }
}

