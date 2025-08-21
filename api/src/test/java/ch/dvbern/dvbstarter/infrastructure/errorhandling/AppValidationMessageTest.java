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
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AppValidationMessageTest {

    @Test
    void invalidEmail() {
        AppValidationMessage message = AppValidationMessage.invalidEmail("nobody");

        assertThat(message.getClientKey())
            .isEqualTo("InvalidEmail");

        assertThatI18nMessage(message.getI18nMessage())
            .usingTranslatorFactory(TL_FACTORY)
            .translatesTo(DE, "Ungültige Emailadresse: nobody.")
            .translatesTo(FR, "Adresse e-mail non valide: nobody.")
            .translatesTo(IT, "Indirizzo email non valido: nobody.")
            .translatesTo(EN, "Invalid email address: nobody.")
            .hasAllLanguagesTranslated(AppLanguages.values());
    }

    @Test
    void invalidPhoneNumber() {
        AppValidationMessage message = AppValidationMessage.invalidPhoneInSwissRegion("whatever");

        assertThat(message.getClientKey())
            .isEqualTo("InvalidPhoneInSwissRegion");

        assertThatI18nMessage(message.getI18nMessage())
            .usingTranslatorFactory(TL_FACTORY)
            .translatesTo(DE, "Ungültige Telefonnummer: whatever.")
            .translatesTo(FR, "Numéro de téléphone non valide: whatever.")
            .translatesTo(IT, "Numero di telefono non valido: whatever.")
            .translatesTo(EN, "Invalid phone number: whatever.")
            .hasAllLanguagesTranslated(AppLanguages.values());
    }

    @Test
    void clientVersionVerificationFailed() {
        AppValidationMessage message = AppValidationMessage.clientVersionVerificationFailed();

        assertThat(message.getClientKey())
            .isEqualTo("ClientVersionVerificationFailed");

        assertThatI18nMessage(message.getI18nMessage())
            .usingTranslatorFactory(TL_FACTORY)
            //@formatter:off
            .translatesTo(DE, "Versions- und Environment Header sind nicht verfügbar oder stimmen nicht mit dem Server überein.")
            .translatesTo(FR, "En-têtes de version et d'environnement non disponibles ou ne correspondent pas au serveur.")
            .translatesTo(IT, "Intestazioni di versione e ambiente non disponibili o non corrispondenti al server.")
            .translatesTo(EN, "Version and environment headers not available or mismatch with server.")
            //@formatter:on
            .hasAllLanguagesTranslated(AppLanguages.values());
    }

    @Test
    void invalidSozialversicherungsnummer() {
        AppValidationMessage message = AppValidationMessage.invalidSozialversicherungsnummer("123");

        assertThat(message.getClientKey())
            .isEqualTo("InvalidSozialversicherungsnummer");

        assertThatI18nMessage(message.getI18nMessage())
            .usingTranslatorFactory(TL_FACTORY)
            .translatesTo(DE, "Ungültige Sozialversicherungsnummer: 123.")
            .translatesTo(FR, "Numéro AVS non valide: 123.")
            .translatesTo(IT, "Numero AVS non valido: 123.")
            .translatesTo(EN, "Invalid social security number: 123.")
            .hasAllLanguagesTranslated(AppLanguages.values());
    }

}
