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

package ch.dvbern.oss.hemed.esc.api.features.search.types;

import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterCombination;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterFieldJson;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SearchColumnsTest {

	@Test
	void splitSearchTermWithOr() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "Max Friederich Robert",
				FilterCombination.OR);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder("max", "friederich", "robert");
	}

	@Test
	void splitSearchTermWithOrEmpty() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "",
				FilterCombination.OR);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder();
	}

	@Test
	void splitSearchTermWithAnd() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "Anna Maria Monika",
				FilterCombination.AND);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder("anna", "maria", "monika");
	}

	@Test
	void splitSearchTermWithAndEmpty() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "",
				FilterCombination.AND);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder();
	}

	@Test
	void splitSearchTermWhitespaceOnly() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, " ",
				FilterCombination.AND);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder();
	}

	@Test
	void splitSearchTermWithSingle() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "Anna Maria Monika",
				FilterCombination.SINGLE);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder("Anna Maria Monika");
	}

	@Test
	void splitSearchTermWithSingleEmpty() {
		FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
				SearchColumns.NAME, "",
				FilterCombination.SINGLE);
		List<String> strings = SearchColumns.prepareStringFilterValues(filter);
		Assertions.assertThat(strings).containsExactlyInAnyOrder("");
	}
    @Test
    void createNameFilterValue_withoutPercentPrefix() {
        // Test that searching without % prefix only matches from beginning
        FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
                SearchColumns.NAME, "Anna",
                FilterCombination.OR);

        // Mock the StringExpression for testing
        StringExpression mockExpression = Mockito.mock(StringExpression.class);
        // the return type is irrelevant, therefore this is an unrealistic mock
        Mockito.when(mockExpression.likeIgnoreCase(Mockito.anyString()))
            .thenReturn(Mockito.mock(BooleanExpression.class));

        // ACT
        SearchColumns.createNameFilterValue(filter, mockExpression);
        // ASSERT
        // Verify it was called with the correct pattern (only % at end)
        Mockito.verify(mockExpression).likeIgnoreCase("anna%");
    }

    @Test
    void createNameFilterValue_withPercentPrefix() {
        // ARRANGE
        // Test that searching with % prefix matches anywhere
        FilterFieldJson<SearchColumns> filter = new FilterFieldJson<>(
                SearchColumns.NAME, "%Anna",
                FilterCombination.OR);

        // Mock the StringExpression for testing
        StringExpression mockExpression = Mockito.mock(StringExpression.class);

        // the return type is irrelevant, therefore this is an unrealistic mock
        Mockito.when(mockExpression.likeIgnoreCase(Mockito.anyString()))
            .thenReturn(Mockito.mock(BooleanExpression.class));

        // ACT
        SearchColumns.createNameFilterValue(filter, mockExpression);
        // ASSERT
        // Verify it was called with the correct pattern (% at beginning and end)
        Mockito.verify(mockExpression).likeIgnoreCase("%anna%");
    }
}
