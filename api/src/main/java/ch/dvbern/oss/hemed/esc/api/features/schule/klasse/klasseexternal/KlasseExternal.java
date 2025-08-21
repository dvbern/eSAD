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

package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal;

import java.io.Serial;
import java.util.Comparator;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.escada.HasExternalId;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_klasseexternal_klasseid", columnList = "klasseid, id"),
        // klasseid is the external id from the csv
        @Index(name = "ix_klasseexternal_klasse", columnList = "klasse_id, id"),
        // klasse_id is fk reference id to our internal klasse
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_klasseexternal_klasseid", columnNames = "klasseId"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KlasseExternal extends AbstractTenantEntity<KlasseExternal> implements HasExternalId {

    public static final Comparator<KlasseExternal> KLASSE_EXTERNAL_DATA_COMPARATOR = Comparator
        .comparing(KlasseExternal::getKlasseBezeichnung, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KlasseExternal::getKlasseStufe, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KlasseExternal::getKlasseSchulart, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KlasseExternal::getKlasseProgrammjahr, Comparator.nullsFirst(String::compareTo));

    public static final Comparator<KlasseExternal> KLASSE_EXTERNAL_STUFE = Comparator
        .comparing(KlasseExternal::getKlasseStufe, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KlasseExternal::getKlasseProgrammjahr, Comparator.nullsFirst(String::compareTo));

    @Serial
    private static final long serialVersionUID = 5789313132588213034L;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_klasseexternal_klasse"), nullable = true)
    private Klasse klasse;

    @Column(nullable = false, unique = true)
    @NonNull
    @NotNull
    @IgnoreForIdClassTest
    private String klasseId;

    @Column(nullable = true)
    @Nullable
    private String klasseBezeichnung;

    //<editor-fold defaultstate="collapsed" desc="KlasseStufe doc">
    /**
     * Escada Klasse Stufe
     * <ul>
     *     <li>1. Kindergarten</li>
     *     <li>2. Primarstufe</li>
     *     <li>3. Sekundarstufe</li>
     *     <li>4. Sekundarstufe II</li>
     * </ul>
     */
    //</editor-fold>
    @Column(nullable = true)
    @Nullable
    private String klasseStufe;

    //<editor-fold defaultstate="collapsed" desc="KlasseArt doc">
    /**
     * KlasseSchulart from Escada
     * <ul>
     *     <li>RK</li>
     *     <li>BM</li>
     *     <li>KG</li>
     *     <li>SEKE</li>
     *     <li>MAR</li>
     *     <li>SEKP</li>
     *     <li>HK</li>
     *     <li>SpAES</li>
     *    </ul>
     */
    //    </editor-fold>
    @Column(nullable = true)
    @Nullable
    private String klasseSchulart;

    @Column(nullable = true)
    @Nullable
    private String klasseProgrammjahr;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_UUID_LENGTH)
    @IgnoreForIdClassTest
    private String escadaSchuleId; //id of the connected schule in the external system

    @Nullable
    @Column(nullable = true)
    private String schulkreisBezeichnung; // since some klassen have no schule we store the Schulkreis Bezeichnung from escada

    /**
     * As discussed on 26.2 we do not import Schule für Gestaltung as it is a Sekunderstufe II Schule
     * that leads to Schuler having multiple klassen wich we can not handle
     * we identify the schule by the missing Klasse Schulart*
     */
    @Transient
    public  boolean isSchuleFuerGestaltung() {
        return StringUtils.isEmpty(this.klasseSchulart);
    }

    @Override
    @NonNull
    public String getExternalId() {
        return klasseId;
    }
}
