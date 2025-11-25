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

package ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungDurchfuehrungUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungDurchfuehrungPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(
    indexes = {
        @Index(name = "ix_untersuchungdurchfuehrung_benutzer_arzt", columnList = "arzt_id, id"),
        @Index(name = "ix_untersuchungdurchfuehrung_benutzer_mpa", columnList = "mpa_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungDurchfuehrungPresentIfReq
public class UntersuchungDurchfuehrung extends AbstractTenantEntity<UntersuchungDurchfuehrung>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 4719335518255035134L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungDurchfuehrung")
    private Untersuchung untersuchung;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungdurchfuehrung_benutzer_arzt"), nullable = true)
    private Benutzer arzt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungdurchfuehrung_benutzer_mpa"), nullable = true)
    private Benutzer mpa;

    @Column(nullable = false)
    private boolean nachkontrolle = false;

    @Column(nullable = false)
    private boolean nachzuegler = false;

    @Column(nullable = false)
    private boolean neueintritt = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @NonNull
    public static UntersuchungDurchfuehrung defaultsForNextUntersuchung(
        @NonNull Untersuchung currentUntersuchung,
        @Nullable Einladung urspruenglicheEinladungForNachzuegler,
        @NonNull Nachkontrolle nachkontrolle
    ) {
        UntersuchungDurchfuehrung durchfuehrung = new UntersuchungDurchfuehrung();
        durchfuehrung.setUntersuchung(currentUntersuchung);
        durchfuehrung.setNachzuegler(urspruenglicheEinladungForNachzuegler != null);
        durchfuehrung.setNachkontrolle(nachkontrolle.getAnlassFlags().hasAnyNachkontrolle());
        durchfuehrung.setArzt(currentUntersuchung.getEinladung().getAnlass().getArzt());
        durchfuehrung.setMpa(currentUntersuchung.getEinladung().getAnlass().getMpa());
        return durchfuehrung;
    }

    @NonNull
    public static UntersuchungDurchfuehrung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungDurchfuehrung durchfuehrung = new UntersuchungDurchfuehrung();
        durchfuehrung.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungDurchfuehrung(durchfuehrung);
        return durchfuehrung;
    }

    @NonNull
    public static UntersuchungDurchfuehrung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        @Nullable Benutzer defaultArzt,
        @Nullable Benutzer defaultMpa
    ) {
        UntersuchungDurchfuehrung durchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        if (durchfuehrung == null) {
            durchfuehrung = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();
        durchfuehrung.setArzt(UntersuchungDurchfuehrungUtil.getArzt(
            config, durchfuehrung, defaultArzt));
        durchfuehrung.setMpa(UntersuchungDurchfuehrungUtil.getMpa(
            config, durchfuehrung, defaultMpa));
        return durchfuehrung;
    }
}
