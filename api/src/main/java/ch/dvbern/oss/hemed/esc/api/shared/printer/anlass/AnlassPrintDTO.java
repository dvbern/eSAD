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

package ch.dvbern.oss.hemed.esc.api.shared.printer.anlass;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.util.EinladungUtil;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Data Transfer Object for printing an Anlass  with its Einladungen children.
 */
@Value
@AllArgsConstructor
public class AnlassPrintDTO {
    private final Anlass anlass;
    private final List<Einladung> einladungen;
    private final LocalDate now;
    private final TenantEnum tenant;
    private final AppLanguages language;
    private final List<Nachkontrolle> nachkontrollen;

    @NonNull
    public String getSchulenCommaSeparated() {
        return EinladungUtil.extractSchulenCommaSeparated(einladungen);
    }

    @NonNull
    public String getKlassenCommaSeparated() {
        return EinladungUtil.extracKlassenCommaSeparated(einladungen);
    }

    @NonNull
    public String getLehrpersonenCommaSeparated(@NonNull BasePrinter basePrinter) {
        return EinladungUtil.extracLehrpersonenCommaSeparated(einladungen, basePrinter);
    }
}
