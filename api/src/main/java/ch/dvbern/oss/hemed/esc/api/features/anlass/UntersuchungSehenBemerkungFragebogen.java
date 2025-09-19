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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungSehenBemerkungFragebogenPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@CheckUntersuchungSehenBemerkungFragebogenPresentIfReq
public class UntersuchungSehenBemerkungFragebogen extends AbstractTenantEntity<UntersuchungSehenBemerkungFragebogen> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 2262757170782081496L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungSehenBemerkungFragebogen")
    private Untersuchung untersuchung;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungenFragebogen;


    @NonNull
    public static UntersuchungSehenBemerkungFragebogen forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungSehenBemerkungFragebogen bemerkungFragebogen = new UntersuchungSehenBemerkungFragebogen();
        bemerkungFragebogen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungSehenBemerkungFragebogen(bemerkungFragebogen);
        return bemerkungFragebogen;
    }
}
