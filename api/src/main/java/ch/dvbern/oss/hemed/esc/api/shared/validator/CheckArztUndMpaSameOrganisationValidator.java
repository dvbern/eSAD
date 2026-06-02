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

import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckArztUndMpaSameOrganisationValidator extends AbstractValidator<CheckArztUndMpaSameOrganisation, HasZustaendigkeit> {

    @Override
    public boolean isEntityValid(@NonNull HasZustaendigkeit entityWithZustaendigkeit) {
        // Nur pruefen, wenn sowohl Arzt wie MPA gesetzt sind
        if (entityWithZustaendigkeit.getZustaendigArzt() == null || entityWithZustaendigkeit.getZustaendigMPA() == null) {
            return true;
        }

        // Die Mpa der Organisation muss einer der Organisationen des Artzes angehören
        Organisation organisationMPA = Optional.ofNullable(entityWithZustaendigkeit.getZustaendigMPA())
            .map(Benutzer::getSingleOrganisation)
            .orElse(null);

        Organisation organisationArzt = entityWithZustaendigkeit.getSingleOrganisation();
        return organisationMPA != null && organisationMPA.equals(organisationArzt);
    }
}
