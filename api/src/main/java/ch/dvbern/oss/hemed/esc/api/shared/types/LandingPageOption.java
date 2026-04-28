/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.types;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Used as a setting to configure what our startpage should be
 */
@Getter
public enum LandingPageOption {

    APP_MODE_SELECTION(Path.APP_MODE_SELECTION),
    DASHBOARD(Path.DASHBOARD),
    ANLASS(Path.ANLASS),
    KINDER(Path.KINDER),
    AUFGABEN(Path.AUFGABEN);

    @NonNull
    private final Path path;

    LandingPageOption(@NonNull Path path) {
        this.path = path;
    }
}
