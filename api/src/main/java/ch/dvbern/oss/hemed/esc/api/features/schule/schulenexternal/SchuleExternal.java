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

package ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal;

import java.io.Serial;
import java.util.Comparator;

import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_schuleexternal_schulhausid", columnList = "schulhausId, id"),
        @Index(name = "ix_schuleexternal_schule", columnList = "schule_id, id"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_schuleexternal_schulhausid", columnNames = "schulhausId"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SchuleExternal extends AbstractEntity<SchuleExternal> implements HasExternalId {

    public static final Comparator<SchuleExternal> SCHULE_EXTERNAL_DATA_COMPARATOR = Comparator
        .comparing(SchuleExternal::getSchulhausBezeichnung, Comparator.nullsFirst(String::compareTo))
        .thenComparing(SchuleExternal::getSchulkreisBezeichnung, Comparator.nullsFirst(String::compareTo))
        .thenComparing(SchuleExternal::getSchulanlageBezeichnung, Comparator.nullsFirst(String::compareTo))
        .thenComparing(SchuleExternal::getSchulanlageId, Comparator.nullsFirst(String::compareTo)); // this is not the id we group over

    @Serial
    private static final long serialVersionUID = 7219898032299116866L;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_schuleexternal_schule"), nullable = true)
    private Schule schule;

    @Column(nullable = false, unique = true)
    @NonNull
    @NotNull
    @IgnoreForIdClassTest
    private String schulhausId;

    @Column(nullable = true)
    @Nullable
    private String schulhausBezeichnung;

    @Column(nullable = true)
    @Nullable
    private String schulkreisBezeichnung;

    @Column(nullable = true)
    @Nullable
    @IgnoreForIdClassTest
    private String schulanlageId;

    @Column(nullable = true)
    @Nullable
    private String schulanlageBezeichnung;

    @Override
    @NonNull
    public String getExternalId() {
        return schulhausId;
    }
}
