/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckOrganisationVerantwortlichePersonIsSchularztValidator extends AbstractValidator<CheckOrganisationVerantwortlichePersonIsSchularzt, Organisation> {

    @Override
    public boolean isEntityValid(@NonNull Organisation organisation) {
        // Die Organisation wird gespeichert, BEVOR dem verantwortlichen Arzt die OrganistionRollen
        // zugewiesen werden (welche wiederum den Benutzer und die Organisation brauchen)
        // Darum kann beim initialen Speichern der Organisation noch keine OrganisationRollen existieren
        if (organisation.getVersion() < 1) {
            return true;
        }
        OrganisationRollen rollenInThisOrganisation = organisation.getVerantwortlicherArzt().getOrganisationRollen().stream()
            .filter(r -> organisation.equals(r.getOrganisation()))
            .findFirst()
            .orElse(null);
        return rollenInThisOrganisation != null && rollenInThisOrganisation.getFachRollen().contains(FachRolle.SCHULAERZTIN);
    }
}
