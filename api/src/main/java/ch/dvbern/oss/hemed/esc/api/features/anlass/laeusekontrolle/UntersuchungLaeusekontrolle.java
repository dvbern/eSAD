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

package ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle;

import java.io.Serial;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungLaeusekontrolleUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungLaeusekontrollePresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungLaeusekontrollePresentIfReq
public class UntersuchungLaeusekontrolle extends AbstractTenantEntity<UntersuchungLaeusekontrolle>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895849L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungLaeusekontrolle")
    private Untersuchung untersuchung;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String beurteilung;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungLaeusekontrolle forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungLaeusekontrolle laeusekontrolle = new UntersuchungLaeusekontrolle();
        laeusekontrolle.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungLaeusekontrolle(laeusekontrolle);
        return laeusekontrolle;
    }

    @NonNull
    public static UntersuchungLaeusekontrolle forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungLaeusekontrolle laeusekontrolle = untersuchung.getUntersuchungLaeusekontrolle();
        if (laeusekontrolle == null) {
            laeusekontrolle = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        laeusekontrolle.setBeurteilung(UntersuchungLaeusekontrolleUtil.getLaeusekontrolleBeurteilung(
            config, laeusekontrolle, LaeusekontrolleBeurteilung.NICHT_GEPRUEFT));
        return laeusekontrolle;
    }

    // Empfehlung
    public void setBeurteilung(@Nullable EnumSet<LaeusekontrolleBeurteilung> beurteilungList) {
        this.beurteilung = EnumSetUtil.toString(beurteilungList);
    }

    @NonNull
    public EnumSet<LaeusekontrolleBeurteilung> getBeurteilung() {
        return EnumSetUtil.fromString(LaeusekontrolleBeurteilung.class, beurteilung);
    }
}

