package ch.dvbern.dvbstarter.shared.i18n.translations;

import com.ibm.icu.util.ULocale;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AppLanguagesTest {

    @Test
    void ensure_enum_is_setup_in_order_and_with_matching_locales() {
        var actual = AppLanguages.values();

        assertThat(actual)
            .extracting(identity(), AppLanguages::locale)
            .containsExactly(
                Tuple.tuple(AppLanguages.DE, ULocale.forLanguageTag("de-CH")),
                Tuple.tuple(AppLanguages.FR, ULocale.forLanguageTag("fr-CH")),
                Tuple.tuple(AppLanguages.IT, ULocale.forLanguageTag("it-CH")),
                Tuple.tuple(AppLanguages.EN, ULocale.forLanguageTag("en-US"))
            );
    }

}
