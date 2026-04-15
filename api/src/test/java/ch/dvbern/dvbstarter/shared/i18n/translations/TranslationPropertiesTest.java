package ch.dvbern.dvbstarter.shared.i18n.translations;

import java.io.IOException;
import java.io.Serial;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TranslationPropertiesTest {

    /**
     * Recommended: use the IntelliJ Action "Sort Lines" on the properties files.
     * DISCLAIMER: Sort order might differ for different languages.
     */
    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void translation_properties_are_sorted_by_name_ignoring_case(AppLanguages appLang) throws Exception {
        var properties = loadProperties(appLang);

        var originalKeys = properties.stringLines().stream()
            .toList();

        var sorted = properties.stringLines().stream()
            .sorted()
            .toList();

        assertThat(originalKeys)
            .isEqualTo(sorted);
    }

    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void translation_properties_are_present_in_all_translations(AppLanguages appLang) throws Exception {
        var reference = loadProperties(AppLanguages.DE);

        var properties = loadProperties(appLang);

        var missingKeys = reference.stringKeys().stream()
            .filter(k -> !properties.containsKey(k))
            .toList();

        assertThat(missingKeys)
            .describedAs(
                "Add these keys to %s: \n%s\n",
                appLang,
                prettyPrintMissingKeys(missingKeys, reference, appLang))
            .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(AppLanguages.class)
    void there_are_no_surplus_properties(AppLanguages appLang) throws Exception {
        var reference = loadProperties(AppLanguages.DE);

        var properties = loadProperties(appLang);

        var surplusKeys = properties.stringKeys().stream()
            .filter(k -> !reference.containsKey(k))
            .toList();

        assertThat(surplusKeys)
            .describedAs(
                "Remove these keys from %s: \n%s\n",
                appLang,
                prettyPrintMissingKeys(surplusKeys, reference, appLang))
            .isEmpty();
    }

    private String prettyPrintMissingKeys(List<String> missingKeys, OrderedProperties reference, AppLanguages appLang) {
        return missingKeys.stream()
            .map(k -> String.format("%s=%s:%s", k, appLang, reference.getProperty(k)))
            .collect(Collectors.joining("\n"));
    }

    private String fromAppLang(AppLanguages appLang) {
        return switch (appLang) {
            case DE -> "translations/translations-bs.properties";
            case FR -> "translations/translations-bs_fr.properties";
            case IT -> "translations/translations-bs_it.properties";
            case EN -> "translations/translations-bs_en.properties";
        };
    }

    private OrderedProperties loadProperties(AppLanguages appLang) throws IOException {
        String propertyFileName = fromAppLang(appLang);
        try (var is = getClass().getClassLoader().getResourceAsStream(propertyFileName)) {
            var properties = new OrderedProperties();
            properties.load(is);

            return properties;
        }
    }

    /**
     * <p>THIS IS INTENDED FOR TESTING PURPOSES ONLY!!!</p>
     * <p>
     * It is a <strong>very</strong> limited implementation that only supports features
     * required for testing of ordering of properties.
     */
    public static class OrderedProperties extends Properties {

        @Serial
        private static final long serialVersionUID = 1L;

        private final LinkedHashMap<Object, Object> linkMap = new LinkedHashMap<>();

        public List<String> stringKeys() {
            return linkMap.keySet().stream()
                .map(Object::toString)
                .toList();
        }

        public List<String> stringLines() {
            return linkMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList();
        }

        @Override
        public Object get(Object key) {
            return linkMap.get(key);
        }

        @Override
        public String getProperty(String key) {
            return String.valueOf(linkMap.get(key));
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            throw new NotImplementedException("Not implemented");
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            return linkMap.put(key, value);
        }

        @Override
        public synchronized boolean contains(Object value) {
            return linkMap.containsValue(value);
        }

        @Override
        public boolean containsValue(Object value) {
            return linkMap.containsValue(value);
        }

        @Override
        public synchronized Enumeration<Object> elements() {
            throw new UnsupportedOperationException(
                "Enumerations are so old-school, don't use them, use keySet() or entrySet() instead"
            );
        }

        @Override
        public Set<Entry<Object, Object>> entrySet() {
            return linkMap.entrySet();
        }

        @Override
        public synchronized void clear() {
            linkMap.clear();
        }

        @Override
        public synchronized boolean containsKey(Object key) {
            return linkMap.containsKey(key);
        }

    }
}
