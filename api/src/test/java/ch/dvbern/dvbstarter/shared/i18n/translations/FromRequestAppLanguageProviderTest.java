package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.util.Optional;

import ch.dvbern.oss.commons.i18nl10n.AppLanguage;
import ch.dvbern.oss.commons.i18nl10n.AppLanguageDTO;
import com.ibm.icu.util.ULocale;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("EmptyClass")
class FromRequestAppLanguageProviderTest {

    @RequiredArgsConstructor
    private static class FakeFilter extends HttpAcceptLanguageRequestFilter {
        private final @Nullable AppLanguage locale;

        @Override
        public Optional<AppLanguage> getAppLanguage() {
            return Optional.ofNullable(locale);
        }
    }

    @Nested
    class ForEmptyLocales {

        private final FromRequestAppLanguageProvider sut = new FromRequestAppLanguageProvider(
            new FakeFilter(null)
        );

        @Test
        void current_returns_the_default_AppLanguage() {
            var actual = sut.current();

            assertThat(actual)
                .isSameAs(AppLanguages.DEFAULT);
        }

    }

    @Nested
    class ForUnsupportedLocales {
        private final AppLanguageDTO klingon = new AppLanguageDTO(ULocale.forLanguageTag("tlh"));

        private final FromRequestAppLanguageProvider sut = new FromRequestAppLanguageProvider(
            // Klingon
            new FakeFilter(klingon)
        );

        @Test
        void current_returns_the_given_AppLanguage() {
            var actual = sut.current();

            assertThat(actual)
                .isSameAs(klingon);
        }
    }

    @Nested
    class ForASupportedLocale {
        private final FromRequestAppLanguageProvider sut = new FromRequestAppLanguageProvider(
            new FakeFilter(AppLanguages.FR)
        );

        @Test
        void current_returns_the_first_supported_AppLanguage() {
            var actual = sut.current();

            assertThat(actual)
                .isSameAs(AppLanguages.FR);
        }
    }
}
