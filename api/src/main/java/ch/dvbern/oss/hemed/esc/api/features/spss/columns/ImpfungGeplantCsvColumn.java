/*
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

package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ImpfungGeplantCsvColumn extends AbstractCsvColumn {

	public ImpfungGeplantCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
		super(config, overrideHeader);
	}

	@Override
	@NonNull
	public String extract(@NonNull Einladung einladung) {
		List<UntersuchungImpfungKrankheitImmunitaet> untersuchungImpfungInternList =
				ExtractorForCsvUtil.untersuchungImpfungtImmunitaetList(einladung, config);

		boolean hasPlannedImpfungen = untersuchungImpfungInternList.stream()
				.map(UntersuchungImpfungKrankheitImmunitaet::getImpfung)
				.anyMatch(impfung -> impfung.getImpfStatus() == ImpfStatus.GEPLANT);

		return hasPlannedImpfungen ? "1" : "2";
	}

	@Override
	@NonNull
	protected String getDefaultHeaderKey() {
		return "CsvColumn.impfungGeplant";
	}
}
