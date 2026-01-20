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
import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
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
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungSehenUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSehenBeurteilungPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private BrilleLinsenResultat brilleLinsen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StereotestResultat stereotest;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private FarbsehenResultat farbsehen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private VerwendetTafelResultat verwendeteTafel;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String fernvisus;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal fernvisusLinks;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String nahvisus;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    @Column(nullable = true)
    private BigDecimal nahvisusLinks;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private MotilitaetResultat motilitaet;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String strabismus;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StrabismusAuspraegungResultat strabismusAuspraegung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private StrabismusAuftretenResultat strabismusAuftreten;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InspektionKonvergenzResultat inspektion;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InspektionKonvergenzResultat konvergenz;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String empfehlung;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    // Fernvisus
    public void setFernvisus(@Nullable EnumSet<DistanzSehenResultat> distanzSehenResultate) {
        this.fernvisus = EnumSetUtil.toString(distanzSehenResultate);
    }

    @NonNull
    public EnumSet<DistanzSehenResultat> getFernvisus() {
        return EnumSetUtil.fromString(DistanzSehenResultat.class, fernvisus);
    }

    // Nahvisus
    public void setNahvisus(@Nullable EnumSet<DistanzSehenResultat> distanzSehenResultate) {
        this.nahvisus = EnumSetUtil.toString(distanzSehenResultate);
    }

    @NonNull
    public EnumSet<DistanzSehenResultat> getNahvisus() {
        return EnumSetUtil.fromString(DistanzSehenResultat.class, nahvisus);
    }

    // Strabismus
    public void setStrabismus(@Nullable EnumSet<StrabismusResultat> strabismusResultate) {
        this.strabismus = EnumSetUtil.toString(strabismusResultate);
    }

    @NonNull
    public EnumSet<StrabismusResultat> getStrabismus() {
        return EnumSetUtil.fromString(StrabismusResultat.class, strabismus);
    }

    // Empfehlung
    public void setEmpfehlung(@Nullable EnumSet<SehenEmpfehlung> empfehlungList) {
        this.empfehlung = EnumSetUtil.toString(empfehlungList);
    }

    @NonNull
    public EnumSet<SehenEmpfehlung> getEmpfehlung() {
        return EnumSetUtil.fromString(SehenEmpfehlung.class, empfehlung);
    }

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBeurteilung sehen = new UntersuchungSehenBeurteilung();
        sehen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSehenBeurteilung(sehen);
        return sehen;
    }

    @NonNull
    public static UntersuchungSehenBeurteilung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungSehenBeurteilung sehen = untersuchung.getUntersuchungSehenBeurteilung();
        if (sehen == null) {
            sehen = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        sehen.setBrilleLinsen(UntersuchungSehenUtil.getBrilleLinsen(
            config, sehen, BrilleLinsenResultat.NICHT_GEPRUEFT));
        sehen.setStereotest(UntersuchungSehenUtil.getStereoTest(
            config, sehen, StereotestResultat.NICHT_GEPRUEFT));
        sehen.setFarbsehen(UntersuchungSehenUtil.getFarbsehen(
            config, sehen, FarbsehenResultat.NICHT_GEPRUEFT));
        sehen.setVerwendeteTafel(UntersuchungSehenUtil.getVerwendeteTafel(
            config, sehen, VerwendetTafelResultat.NICHT_GEPRUEFT));
        sehen.setFernvisus(UntersuchungSehenUtil.getFernvisus(
            config, sehen, DistanzSehenResultat.NICHT_GEPRUEFT));
        sehen.setNahvisus(UntersuchungSehenUtil.getNahvisus(
            config, sehen, DistanzSehenResultat.NICHT_GEPRUEFT));
        sehen.setMotilitaet(UntersuchungSehenUtil.getMotilitaet(
            config, sehen, MotilitaetResultat.NICHT_GEPRUEFT));
        sehen.setStrabismus(UntersuchungSehenUtil.getStrabismus(
            config, sehen, StrabismusResultat.NICHT_GEPRUEFT));
        sehen.setStrabismusAuspraegung(UntersuchungSehenUtil.getStrabismusAuspraegung(
            config, sehen, StrabismusAuspraegungResultat.NICHT_GEPRUEFT));
        sehen.setStrabismusAuftreten(UntersuchungSehenUtil.getStrabismusAuftreten(
            config, sehen, StrabismusAuftretenResultat.NICHT_GEPRUEFT));
        sehen.setInspektion(UntersuchungSehenUtil.getInspektion(
            config, sehen, InspektionKonvergenzResultat.NICHT_GEPRUEFT));
        sehen.setKonvergenz(UntersuchungSehenUtil.getKonvergenz(
            config, sehen, InspektionKonvergenzResultat.NICHT_GEPRUEFT));
        return sehen;
    }
}

