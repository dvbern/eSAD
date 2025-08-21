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
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfstoffNameCsvColumn extends AbstractCsvColumn {

	private final Krankheit krankheit;

	public KrankheitImpfstoffNameCsvColumn(
			@NonNull CsvColumnConfig config,
			@Nullable String overrideHeader,
			@NonNull Krankheit krankheit) {
		super(config, overrideHeader);
		this.krankheit = krankheit;
	}

	@Override
	@NonNull
	public String extract(@NonNull Einladung einladung) {
		List<UntersuchungImpfungKrankheitImmunitaet> krankheitImmunitaetList =
				ExtractorForCsvUtil.untersuchungImpfungKrankheitImmunitaetList(einladung, krankheit, config);

		return krankheitImmunitaetList.stream()
				.map(UntersuchungImpfungKrankheitImmunitaet::getImpfung)
				.filter(impfung -> impfung.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT)
				.filter(impfung -> einladung.getAnlass().equals(impfung.getAnlassImpfungDurchgefuehrt()))
				.map(UntersuchungImpfungIntern::getImpfstoff)
				.map(Impfstoff::getName)
				.collect(Collectors.joining(","));
	}

	@Override
	@NonNull
	protected String getDefaultHeaderKey() {
		return "CsvColumn.krankheitImpfstoffName";
	}
}
