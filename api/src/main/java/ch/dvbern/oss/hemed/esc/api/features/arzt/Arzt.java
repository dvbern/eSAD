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

package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Fachrichtung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.HpvTeilnahme;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Organisation;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Arzt extends AbstractTenantEntity<Arzt> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7908264470040862763L;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String anrede;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Fachrichtung fachrichtung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Organisation organisation;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String vorname;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @Valid
    @Embedded
    @Nullable
    private Adresse adresse;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Convert(converter = NormalizedPhoneNumberConverter.class)
    private NormalizedPhoneNumber telefon;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private EmailAddress email;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String bemerkungen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private HpvTeilnahme hpvTeilnahme;

    @NonNull
    public String getNameVorname() {
        return name + ' ' + vorname;
    }
}
