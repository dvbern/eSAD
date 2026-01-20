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

package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.KlasseExternal;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
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
        @Index(name = "ix_kindexternal_schuelerid", columnList = "schuelerid, id"),
        // kindid is the external id from the csv
        @Index(name = "ix_kindexternal_kind", columnList = "kind_id, id"),
        // kind_id is fk reference id to our internal kind
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_kindexternal_schuelerid", columnNames = "schuelerid"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KindExternal extends AbstractEntity<KindExternal> implements HasExternalId {

    // Null-safe comparator for all fields except schuelerId
    public static final Comparator<KindExternal> KIND_EXTERNAL_DATA_COMPARATOR = Comparator
        .comparing(KindExternal::getWohntyp, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerName, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerVorname, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerGeburtsdatum, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerGeschlecht, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerSozialversicherung, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerAdresse, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerAdresse1, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerAdresse2, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerPLZ, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerOrt, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerMobile, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerEMail, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerNationalitaet, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerStaatcodeISO, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerSprache, Comparator.nullsFirst(String::compareTo))
        .thenComparing(KindExternal::getSchuelerErstsprache, Comparator.nullsFirst(String::compareTo));

    @Serial
    private static final long serialVersionUID = -1011207272056032827L;

    @Nullable
    @OneToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_kindexternal_kind"), nullable = true)
    private Kind kind;

    @Column(nullable = false, unique = true)
    @NonNull
    @NotNull
    @IgnoreForIdClassTest
    private String schuelerId;

    @Column(nullable = true)
    @Nullable
    private String wohntyp;

    @Column(nullable = true)
    @Nullable
    private String schuelerName;

    @Column(nullable = true)
    @Nullable
    private String schuelerVorname;

    @Column(nullable = true)
    @Nullable
    private String schuelerGeburtsdatum;

    @Column(nullable = true)
    @Nullable
    private String schuelerGeschlecht;

    @Column(nullable = true)
    @Nullable
    private String schuelerSozialversicherung;

    @Column(nullable = true)
    @Nullable
    private String schuelerAdresse;

    @Column(nullable = true)
    @Nullable
    private String schuelerAdresse1;

    @Column(nullable = true)
    @Nullable
    private String schuelerAdresse2;

    @Column(nullable = true)
    @Nullable
    private String schuelerPLZ;

    @Column(nullable = true)
    @Nullable
    private String schuelerOrt;

    @Column(nullable = true)
    @Nullable
    private String schuelerMobile;

    @Column(nullable = true)
    @Nullable
    private String schuelerEMail;

    @Column(nullable = true)
    @Nullable
    private String schuelerNationalitaet;

    @Column(nullable = true)
    @Nullable
    private String schuelerStaatcodeISO;

    @Column(nullable = true)
    @Nullable
    private String schuelerSprache;

    @Column(nullable = true)
    @Nullable
    private String schuelerErstsprache;

    @Column(nullable = true)
    @Nullable
    @IgnoreForIdClassTest
    private String schulhausId;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_UUID_LENGTH)
    @IgnoreForIdClassTest
    private String escadaKlasseId;

    // to maybe drop an entry if it belongs to Schule fuer Gestaltung we save this information temporarily
    @Nullable
    @Transient
    private KlasseExternal escadaKlasseExternal;

    @Override
    @NonNull
    public String getExternalId() {
        return schuelerId;
    }

    @Nullable
    public String getFullName() {
        if (schuelerVorname == null && schuelerName == null) {
            return null;
        }
        var strings = Stream.of(schuelerVorname, schuelerName).filter(Objects::nonNull).toList();
        return String.join(" ", strings);
    }

    @Nullable
    public String getFullNameIfAllowed(boolean escadaRedactPersonalInfo) {
        if (escadaRedactPersonalInfo) {
            return null;
        }
        return this.getFullName();
    }

}
