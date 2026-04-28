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
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class KrankheitImpfstoffNameCsvColumnTest extends AbstractBaseColumnTest {
	record ImpfMockParams(String name, ImpfStatus status, Anlass anlass) {
	}

	@ParameterizedTest
	@EnumSource(Krankheit.class)
	void extractor(Krankheit krankheit) {
		// ARRANGE
		KrankheitImpfstoffNameCsvColumn column = new KrankheitImpfstoffNameCsvColumn(config, null, krankheit);

		Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
		Einladung einladung = untersuchung.getEinladungRequired();
		einladung.setUntersuchung(untersuchung);

		List<UntersuchungImpfungIntern> impfungen = Stream.of(
						new ImpfMockParams("impf-verabreicht-1", ImpfStatus.DURCHGEFUEHRT, einladung.getAnlass()),
						new ImpfMockParams("impf-verabreicht-2", ImpfStatus.DURCHGEFUEHRT, einladung.getAnlass()),
						new ImpfMockParams("impf-nicht-verabreicht", ImpfStatus.NICHT_DURCHGEFUEHRT,
								einladung.getAnlass()),
						new ImpfMockParams("impf-geplant", ImpfStatus.GEPLANT, einladung.getAnlass()),
						new ImpfMockParams(
								"impf-anlass-anderer",
								ImpfStatus.DURCHGEFUEHRT,
								AnlassBuilder.defaultAnlass(LocalDate.now())))
				.map(impfMockParams -> createImpfung(
						impfMockParams.name(),
						impfMockParams.anlass(),
						impfMockParams.status(),
						krankheit))
				.toList();

		when(untersuchungImpfungInternServiceMock.findByUntersuchung(any(ID.class))).thenReturn(impfungen);

		String expected = "impf-verabreicht-1-" + krankheit.name() + ",impf-verabreicht-2-" + krankheit.name();

		// ACT
		String actual = column.extract(einladung);

		// ASSERT
		assertThat(actual).isEqualTo(expected);
	}

	@ParameterizedTest
	@EnumSource(Krankheit.class)
	void extractor_empty(Krankheit krankheit) {
		// ARRANGE
		KrankheitImpfstoffNameCsvColumn column = new KrankheitImpfstoffNameCsvColumn(config, null, krankheit);

		Untersuchung untersuchung = UntersuchungBuilder.defaultUntersuchung(LocalDate.now());
		Einladung einladung = untersuchung.getEinladungRequired();
		einladung.setUntersuchung(untersuchung);

		when(untersuchungImpfungInternServiceMock.findByUntersuchung(any(ID.class))).thenReturn(List.of());

		String expected = "";

		// ACT
		String actual = column.extract(einladung);

		// ASSERT
		assertThat(actual).isEqualTo(expected);
	}

	private UntersuchungImpfungIntern createImpfung(
			String impfStoffName,
			Anlass anlass,
			ImpfStatus impfStatus,
			Krankheit krankheit) {
		UntersuchungImpfungIntern impfungIntern =
				UntersuchungImpfungInternBuilder.create(untersuchungImpfungInternBuilder -> untersuchungImpfungInternBuilder.withAnlassImpfungDurchgefuehrt(
								anlass)
						.withImpfStatus(impfStatus)
						.withImpfstoff(ImpfstoffBuilder.create(impfstoffBuilder -> impfstoffBuilder.withName(
								impfStoffName + '-' + krankheit.name()))));

		UntersuchungImpfungKrankheitImmunitaet impfungKrankheitImmunitaet =
				new UntersuchungImpfungKrankheitImmunitaet();
		impfungKrankheitImmunitaet.setKrankheit(krankheit);
		impfungKrankheitImmunitaet.setImpfung(impfungIntern);

		impfungIntern.setKrankheitenImmunitaeten(Set.of(impfungKrankheitImmunitaet));

		return impfungIntern;
	}
}
