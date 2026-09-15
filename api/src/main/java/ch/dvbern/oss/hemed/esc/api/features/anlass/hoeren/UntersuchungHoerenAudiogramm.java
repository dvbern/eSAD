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

package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungHoerenAudiogrammPresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
@Table(
    indexes = {
        @Index(name = "ix_untersuchunghoerenaudiogramm_audiogrammrechts", columnList = "audiogrammrechts_id, id"),
        @Index(name = "ix_untersuchunghoerenaudiogramm_audiogrammlinks", columnList = "audiogrammlinks_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungHoerenAudiogrammPresentIfReq
public class UntersuchungHoerenAudiogramm extends AbstractEntity<UntersuchungHoerenAudiogramm>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungHoerenAudiogramm")
    private Untersuchung untersuchung;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchunghoerenaudiogramm_audiogramm_rechts"), nullable = true)
    private Audiogramm audiogrammRechts;

    @Nullable
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchunghoerenaudiogramm_audiogramm_links"), nullable = true)
    private Audiogramm audiogrammLinks;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "audiogrammSimpel", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<AudiogrammSimpel> audiogrammSimpel;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "hoerhilfe", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<HoerenHoerhilfe> hoerhilfe;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "audiogrammBeurteilung", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<AudiogrammBeurteilung> audiogrammBeurteilung = MultiValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "zusaetzlicheAngaben", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<HoerenZusaetzlicheAngaben> zusaetzlicheAngaben = MultiValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "empfehlung", length = DbConst.DB_ENUM_LIST_LENGTH))
    private MultiValueEnhanced<HoerenEmpfehlung> empfehlung = MultiValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungHoerenAudiogramm forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenAudiogramm hoerenAudiogramm = new UntersuchungHoerenAudiogramm();
        hoerenAudiogramm.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungHoerenAudiogramm(hoerenAudiogramm);
        return hoerenAudiogramm;
    }

    @NonNull
    public static UntersuchungHoerenAudiogramm forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungHoerenAudiogramm hoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm();
        if (hoerenAudiogramm == null) {
            hoerenAudiogramm = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();

        hoerenAudiogramm.setAudiogrammSimpel(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getSehenFernvisusAugeEinzeln,
                hoerenAudiogramm,
                UntersuchungHoerenAudiogramm::getAudiogrammSimpel,
                AudiogrammSimpel.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        hoerenAudiogramm.setHoerhilfe(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getHoerenHoerhilfe,
                hoerenAudiogramm,
                UntersuchungHoerenAudiogramm::getHoerhilfe,
                HoerenHoerhilfe.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        hoerenAudiogramm.setAudiogrammBeurteilung(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getHoerenAudiogrammBeurteilung,
                hoerenAudiogramm,
                UntersuchungHoerenAudiogramm::getAudiogrammBeurteilung,
                AudiogrammBeurteilung.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        hoerenAudiogramm.setZusaetzlicheAngaben(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getHoerenAudiogrammZusaetzlicheAngaben,
                hoerenAudiogramm,
                UntersuchungHoerenAudiogramm::getZusaetzlicheAngaben,
                HoerenZusaetzlicheAngaben.KEINE,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return hoerenAudiogramm;
    }
}

