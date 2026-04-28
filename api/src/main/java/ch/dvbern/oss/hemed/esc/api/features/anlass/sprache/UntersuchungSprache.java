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

package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSprachePresentIfReq;
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
@CheckUntersuchungSprachePresentIfReq
public class UntersuchungSprache extends AbstractEntity<UntersuchungSprache>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSprache")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "nachsprechen", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Nachsprechen> nachsprechen = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value", column = @Column(name = "wortschatz", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Wortschatz> wortschatz = SingleValueEnhanced.of();

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "sprachverstaendnis", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private SingleValueEnhanced<Sprachverstaendnis> sprachverstaendnis = SingleValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungSprache forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSprache sprache = new UntersuchungSprache();
        sprache.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSprache(sprache);
        return sprache;
    }

    @NonNull
    public static UntersuchungSprache forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungSprache untersuchungSprache = untersuchung.getUntersuchungSprache();
        if (untersuchungSprache == null) {
            untersuchungSprache = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getAnlass().getAnlassConfig();

        untersuchungSprache.setNachsprechen(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungSpracheNachsprechen,
                untersuchungSprache,
                UntersuchungSprache::getNachsprechen,
                Nachsprechen.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        untersuchungSprache.setWortschatz(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungSpracheWortschatz,
                untersuchungSprache,
                UntersuchungSprache::getWortschatz,
                Wortschatz.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        untersuchungSprache.setSprachverstaendnis(
            ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getUntersuchungSpracheSprachverstaendnis,
                untersuchungSprache,
                UntersuchungSprache::getSprachverstaendnis,
                Sprachverstaendnis.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        return untersuchungSprache;
    }
}

