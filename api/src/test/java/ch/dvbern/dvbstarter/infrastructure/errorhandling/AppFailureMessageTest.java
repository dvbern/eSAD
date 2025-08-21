package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages.DE;
import static ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages.EN;
import static ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages.FR;
import static ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages.IT;
import static ch.dvbern.oss.commons.i18nl10n.I18nMessageAssertions.assertThatI18nMessage;
import static ch.dvbern.oss.hemed.esc.api.testutil.i18n.TestTranslations.TL_FACTORY;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AppFailureMessageTest {

    @Test
    void internalError() {
        assertThatI18nMessage(AppFailureMessage.internalError("hello exception").getI18nMessage())
            .usingTranslatorFactory(TL_FACTORY)
            .translatesTo(DE, "Interner Server-Fehler: \"hello exception\".")
            .translatesTo(FR, "Erreur interne: \"hello exception\".")
            .translatesTo(IT, "Errore interno del server: \"hello exception\".")
            .translatesTo(EN, "Internal server error: \"hello exception\".")
            .hasAllLanguagesTranslated(AppLanguages.values());
    }
}
