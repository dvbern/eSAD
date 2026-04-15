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
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungHoerenOtoskopiePresentIfReq;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
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
@Table(
    indexes = {
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungHoerenOtoskopiePresentIfReq
public class UntersuchungHoerenOtoskopie extends AbstractEntity<UntersuchungHoerenOtoskopie>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungHoerenOtoskopie")
    private Untersuchung untersuchung;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "otoskopieRechts", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private MultiValueEnhanced<OtoskopieResultat> otoskopieRechts = MultiValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String otoskopieRechtsResultatAndere;

    @Valid
    @Embedded
    @Nullable
    @AttributeOverride(name = "value",
        column = @Column(name = "otoskopieLinks", length = DbConst.DB_DEFAULT_MAX_LENGTH))
    private MultiValueEnhanced<OtoskopieResultat> otoskopieLinks = MultiValueEnhanced.of();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String otoskopieLinksResultatAndere;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungHoerenOtoskopie forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie = new UntersuchungHoerenOtoskopie();
        hoerenOtoskopie.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungHoerenOtoskopie(hoerenOtoskopie);
        return hoerenOtoskopie;
    }

    @NonNull
    public static UntersuchungHoerenOtoskopie forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie();
        if (hoerenOtoskopie == null) {
            hoerenOtoskopie = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        hoerenOtoskopie.setOtoskopieRechts(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getHoerenOtoskopie,
                hoerenOtoskopie,
                UntersuchungHoerenOtoskopie::getOtoskopieRechts,
                OtoskopieResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );
        hoerenOtoskopie.setOtoskopieLinks(
            ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
                config,
                AnlassConfig::getHoerenOtoskopie,
                hoerenOtoskopie,
                UntersuchungHoerenOtoskopie::getOtoskopieLinks,
                OtoskopieResultat.NICHT_GEPRUEFT,
                nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
            )
        );

        return hoerenOtoskopie;
    }
}

