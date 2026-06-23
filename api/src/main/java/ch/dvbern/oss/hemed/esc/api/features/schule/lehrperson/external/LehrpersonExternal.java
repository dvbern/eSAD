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

package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.external;

import java.io.Serial;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.HasExternalId;
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
        @Index(name = "ix_lehrpersonexternal_klasselehrpersonid", columnList = "klasselehrpersonid, id"),
        // lehrpersonid is the external id from the csv
        @Index(name = "ix_lehrpersonexternal_lehrperson", columnList = "lehrperson_id, id"),
        // lehrperson_id is fk reference id to our internal lehrperson
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_lehrpersonexternal_klasselehrpersonid",
            columnNames = "klasselehrpersonid"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LehrpersonExternal extends AbstractEntity<LehrpersonExternal> implements HasExternalId {

    public static final Comparator<LehrpersonExternal> KP_SCHULE_EXTERNAL_DATA_COMPARATOR = Comparator
        .comparing(LehrpersonExternal::getKlasseLehrpersonAnrede, Comparator.nullsFirst(String::compareTo))
        .thenComparing(LehrpersonExternal::getKlasseLehrpersonName, Comparator.nullsFirst(String::compareTo))
        .thenComparing(
            LehrpersonExternal::getKlasseLehrpersonVorname,
            Comparator.nullsFirst(String::compareTo));

    @Serial
    private static final long serialVersionUID = 6432798040338473441L;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_lehrpersonexternal_lehrperson"), nullable = true)
    private Lehrperson lehrperson;

    @Column(nullable = false, unique = true)
    @NonNull
    @NotNull
    @IgnoreForIdClassTest
    private String klasseLehrpersonId;

    @Column(nullable = true)
    @Nullable
    private String klasseLehrpersonAnrede;

    @Column(nullable = true)
    @Nullable
    private String klasseLehrpersonName;

    @Column(nullable = true)
    @Nullable
    private String klasseLehrpersonVorname;

    @Transient
    private int lehrpersonNumber;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @IgnoreForIdClassTest
    private String escadaKlasseIds; // Kommaseparierte Liste von KlassenIds

    /**
     * Es werden zuerst alle Rows von Lehrpersonen in ein LehrpersonExternal
     * geschrieben. Danach erst werden die doppelten zusammengefasst und die IDs unter
     * escadaKlasseIds gemappt. escadaKlasseId brauchen wir daher nur temporär, bis wir
     * alle gleichen gefunden haben.
     */
    @Transient
    @IgnoreForIdClassTest
    @Nullable
    private String escadaKlasseId;

    @Override
    @NonNull
    public String getExternalId() {
        return klasseLehrpersonId;
    }

    @NonNull
    public String getFullName() {
        return Stream.of(klasseLehrpersonVorname, klasseLehrpersonName)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(" "));
    }

    @Nullable
    public String getFullNameIfAllowed(boolean redactFullName) {
        if (redactFullName) {
            return null;
        }
        return this.getFullName();
    }
}
