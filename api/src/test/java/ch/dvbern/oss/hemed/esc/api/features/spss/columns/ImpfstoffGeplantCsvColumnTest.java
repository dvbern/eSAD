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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ImpfstoffGeplantCsvColumnTest extends AbstractBaseColumnTest {
	@ParameterizedTest
	@CsvSource({ "1, GEPLANT", "2, DURCHGEFUEHRT", "2, NICHT_DURCHGEFUEHRT", })
	void extractor(String expected, ImpfStatus impfStatus) {
		// ARRANGE
		ImpfungGeplantCsvColumn column = new ImpfungGeplantCsvColumn(config, null);

		Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
		Einladung einladung = untersuchung.getEinladung();
		einladung.setUntersuchung(untersuchung);

		when(untersuchungImpfungInternServiceMock.findByUntersuchung(any(ID.class))).thenReturn(List.of(createImpfung(
				impfStatus)));

		// ACT
		String actual = column.extract(einladung);

		// ASSERT
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void extractor_empty() {
		// ARRANGE
		ImpfungGeplantCsvColumn column = new ImpfungGeplantCsvColumn(config, null);

		Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
		Einladung einladung = untersuchung.getEinladung();
		einladung.setUntersuchung(untersuchung);

		when(untersuchungImpfungInternServiceMock.findByUntersuchung(any(ID.class))).thenReturn(List.of());

		String expected = "2";

		// ACT
		String actual = column.extract(einladung);

		// ASSERT
		assertThat(actual).isEqualTo(expected);
	}

	private UntersuchungImpfungIntern createImpfung(
			ImpfStatus impfStatus) {
		UntersuchungImpfungIntern impfungIntern =
				UntersuchungImpfungInternBuilder.create(untersuchungImpfungInternBuilder -> untersuchungImpfungInternBuilder.withImpfStatus(
						impfStatus));

		UntersuchungImpfungKrankheitImmunitaet impfungKrankheitImmunitaet =
				new UntersuchungImpfungKrankheitImmunitaet();
		impfungKrankheitImmunitaet.setImpfung(impfungIntern);

		impfungIntern.setKrankheitenImmunitaeten(Set.of(impfungKrankheitImmunitaet));

		return impfungIntern;
	}
}
