package ch.dvbern.dvbstarter.shared.i18n.translations;

import ch.dvbern.oss.commons.i18nl10n.AppLanguage;

@FunctionalInterface
public interface AppLanguageProvider {
    AppLanguage current();
}
