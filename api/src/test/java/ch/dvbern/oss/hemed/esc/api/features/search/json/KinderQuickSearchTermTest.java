package ch.dvbern.oss.hemed.esc.api.features.search.json;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.search.types.KinderQuickSearchTerm;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KinderQuickSearchTermTest {

    @Test
    void searchTermShouldBeSplit() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("some search term");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isNotEmpty()
            .hasSize(3)
            .containsExactlyInAnyOrder("some", "search", "term");
        Assertions.assertThat(someSearchTerm.getDateSearchTerms()).isEmpty();
        Assertions.assertThat(someSearchTerm.getEdokidIdSearchTerms()).isEmpty();
    }

    @Test
    void searchTermShouldRecognizeDates() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("01.01.2021 myName 2022-01-01 12.12.1980");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getDateSearchTerms())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder(
                LocalDate.of(2021, 1, 1),
                LocalDate.of(1980, 12, 12)
            );
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder("myname", "2022-01-01");
    }

    @Test
    void searchTermShouldIgnoreDigitOnlySearchesForThePurposeOfNamesaerch() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("99 88 77 66 55 44 33");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getDateSearchTerms())
            .isEmpty();
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isEmpty();
        Assertions.assertThat(someSearchTerm.getEdokidIdSearchTerms())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder("99 88 77 66", "55 44 33");
    }

    @Test
    void searchTermShouldWorkForEsadIdYear() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("24");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getDateSearchTerms())
            .isEmpty();
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isEmpty();
        Assertions.assertThat(someSearchTerm.getEdokidIdSearchTerms())
            .isNotEmpty()
            .hasSize(1)
            .containsExactlyInAnyOrder("24");
    }

    @Test
    void searchTermShouldRecognizeEsadIdPatterns() {
        // ARRANGE / ACT
        KinderQuickSearchTerm edokidIdSearchTerm = new KinderQuickSearchTerm("A0 01 000 24");
        // ASSERT
        Assertions.assertThat(edokidIdSearchTerm.getEdokidIdSearchTerms())
            .isNotEmpty()
            .hasSize(1)
            .containsExactlyInAnyOrder("A0 01 000 24");
        Assertions.assertThat(edokidIdSearchTerm.getNameSearchTerms())
            .isEmpty();
        Assertions.assertThat(edokidIdSearchTerm.getDateSearchTerms())
            .isEmpty();
    }

    @Test
    void namesWithNumbersAreAllowed() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("myName2 01.01.2024 A0 01 123 24");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getDateSearchTerms())
            .isNotEmpty()
            .hasSize(1)
            .containsExactlyInAnyOrder(LocalDate.of(2024, 1, 1));
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isNotEmpty()
            .hasSize(1)
            .containsExactlyInAnyOrder("myname2");
        Assertions.assertThat(someSearchTerm.getEdokidIdSearchTerms())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder(
                "e2",
                "A0 01 123 24"
            );
    }

    @Test
    void keepQuotedNameStringsUntokenized() {
        // ARRANGE / ACT
        KinderQuickSearchTerm someSearchTerm = new KinderQuickSearchTerm("\"Rainer Friederich\" Rilke");
        // ASSERT
        Assertions.assertThat(someSearchTerm.getDateSearchTerms())
            .isEmpty();
        Assertions.assertThat(someSearchTerm.getNameSearchTerms())
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyInAnyOrder("rainer friederich", "rilke");
        Assertions.assertThat(someSearchTerm.getEdokidIdSearchTerms())
            .isEmpty();
    }
}
