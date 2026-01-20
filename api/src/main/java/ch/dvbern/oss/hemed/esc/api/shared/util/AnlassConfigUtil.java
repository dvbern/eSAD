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

package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.types.EnumSetConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public final class AnlassConfigUtil {

    public static final String START_STRING = "'";
    private static final Pattern AUDIT_PATTERN =
        Pattern.compile("timestampErstellt|timestampMutiert|userErstellt|userMutiert");

    private AnlassConfigUtil() {
	}

	public static void requireAnlassConfigNotYetUsedInAnlass(
        boolean usedInAtLeastOneAnlass
    ) {
        if (usedInAtLeastOneAnlass) {
            throw new AppValidationException(AppValidationMessage.anlassConfigUsedInAtLeastOneAnlassCannotBeEdited());
        }
	}

    public static void requireAnlassConfigValidDatumVon(
        @NonNull AnlassConfig existingAnlassConfig,
        @NonNull AnlassConfig newAnlassConfig
    ) {
        if (!existingAnlassConfig.getGueltigkeitsZeitraum().getDatumVon()
            .isBefore(newAnlassConfig.getGueltigkeitsZeitraum().getDatumVon())) {
            throw new AppValidationException(AppValidationMessage.anlassConfigCannotStartBeforeLastAnlassConfig(existingAnlassConfig.getGueltigkeitsZeitraum().getDatumVon().plusDays(1)));
        }
    }

    public static void requireAnlassConfigsNoGapNoOverlap(
        @NonNull List<AnlassConfig> allConfigs
    ) {
        Map<AnlassTyp, List<AnlassConfig>> configsByAnlassTyp = allConfigs.stream()
            .collect(Collectors.groupingBy(AnlassConfig::getAnlassTyp));
        for (Map.Entry<AnlassTyp, List<AnlassConfig>> entry : configsByAnlassTyp.entrySet()) {
            List<AnlassConfig> configForThisAnlassTyp = entry.getValue();
            configForThisAnlassTyp.sort(Comparator.comparing(c -> c.getGueltigkeitsZeitraum().getDatumVon()));
            for (int i = 0; i < configForThisAnlassTyp.size() - 1; i++) {
                AnlassConfig current = configForThisAnlassTyp.get(i);
                AnlassConfig next = configForThisAnlassTyp.get(i + 1);
                if (current.getGueltigkeitsZeitraum().intersects(next.getGueltigkeitsZeitraum())
                    || current.getGueltigkeitsZeitraum().hasGap(next.getGueltigkeitsZeitraum())) {
                    throw new AppValidationException(AppValidationMessage.anlassConfigGapOrOverlapDetected(current, next));
                }
            }
        }
    }

    @NonNull
    public static String createSqlInsertQuery(@NonNull AnlassConfig config) {
        return createSqlInsertQuery(config, null, null);
    }

    @NonNull
    public static String createSqlInsertQuery(@NonNull AnlassConfig config, @Nullable RequirementSetting defaultSetting, @Nullable Boolean defaultBoolean) {
        StringBuilder query = new StringBuilder();
        try {
            List<String> columnNames = new ArrayList<>();
            List<String> values = new ArrayList<>();

            Class<?> clazz = AnlassConfig.class;
            List<Field> allFields = getAllFields(clazz);

            for (Field field : allFields) {
                // Skip fields that shouldn't be in INSERT statement
                if (shouldSkipFieldForInsert(field)) {
                    continue;
                }

                // Make field accessible
                field.setAccessible(true);

                // Get field value
                Object value = field.get(config);

                // Handle embedded objects separately
                if (field.getAnnotation(Embedded.class) != null && value != null) {
                    // Process embedded object fields
                    Map<String, String> embeddedFields = processEmbeddedObject(value);
                    columnNames.addAll(embeddedFields.keySet());
                    values.addAll(embeddedFields.values());
                    continue;
                }

                // Get column annotation
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation == null) {
                    continue;
                }

                // Get column name
                String columnName = getColumnName(field, columnAnnotation);

                // Format the value
                String formattedValue = null;
                if (defaultSetting != null && defaultBoolean != null) {
                    formattedValue = toDefaultValue(field, defaultSetting, defaultBoolean);
                } else {
                    formattedValue = formatValue(value, field);
                }

                // Add to lists
                columnNames.add(columnName);
                values.add(formattedValue);
            }

            // Build INSERT statement
            query.append("-- ").append(config.getAnlassTyp()).append('\n');
            query.append("INSERT INTO anlassconfig (");
            query.append(String.join(", ", columnNames));
            query.append(") VALUES (");
            query.append(String.join(", ", values));
            query.append(");");

        } catch (IllegalAccessException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Failed to access field via reflection"), e);
        }

        // Log to console
        LOG.info("=== INSERT QUERY FOR ANLASS CONFIG ===\n{}", query.toString());
        return query.toString();
    }

    @NonNull
    public static String createSqlUpdateQuery(@NonNull AnlassConfig config) {
        return createSqlUpdateQuery(config, null, null);
    }

    @NonNull
    public static String createSqlUpdateQuery(@NonNull AnlassConfig config, @Nullable RequirementSetting defaultSetting, @Nullable Boolean defaultBoolean) {
        StringBuilder query = new StringBuilder();
        try {
            List<String> updates = new ArrayList<>();
            Class<?> clazz = AnlassConfig.class;
            // Get all declared fields including inherited ones
            List<Field> allFields = getAllFields(clazz);
            for (Field field : allFields) {
                // Skip fields that shouldn't be in UPDATE statement
                if (shouldSkipFieldForUpdate(field)) {
                    continue;
                }
                // Make field accessible
                field.setAccessible(true);
                // Get field value
                Object value = field.get(config);
                // Handle embedded objects separately
                if (field.getAnnotation(Embedded.class) != null && value != null) {
                    // Process embedded object fields
                    Map<String, String> embeddedFields = processEmbeddedObject(value);
                    for (Entry<String, String> entry : embeddedFields.entrySet()) {
                        updates.add(" " + entry.getKey() + " = " + entry.getValue());
                    }
                    continue;
                }
                // Get column annotation
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation == null) {
                    continue;
                }
                // Get column name
                String columnName = getColumnName(field, columnAnnotation);
                // Format the value
                String formattedValue = null;
                if (defaultSetting != null && defaultBoolean != null) {
                    formattedValue = toDefaultValue(field, defaultSetting, defaultBoolean);
                } else {
                    formattedValue = formatValue(value, field);
                }
                // Add to updates list
                updates.add(" " + columnName + " = " + formattedValue);
            }

            query.append("-- ").append(config.getAnlassTyp()).append('\n');
            query.append("UPDATE anlassconfig SET ");
            // Join all updates with commas
            query.append(String.join(",", updates));
            // Add WHERE clause
            query.append(" WHERE id = ")
                .append(START_STRING)
                .append(config.getId().getId())
                .append(START_STRING)
                .append(';')
                .append('\n');
        } catch (IllegalAccessException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Failed to access field via reflection"), e);
        }
        // Log to console
        LOG.info("=== UPDATE QUERY FOR ANLASS CONFIG (REFLECTION) === \n{}", query.toString());
        return query.toString();
    }

    @NonNull
    private static List<Field> getAllFields(@NonNull Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    private static boolean shouldSkipFieldForUpdate(@NonNull Field field) {
        if (shouldSkipFieldForInsert(field)) {
            return true;
        }
        // Skip ID field (usually not updated)
        if (field.getName().equals("id")) {
            return true;
        }
        if (field.getName().equals("anlassTyp")) {
            return true;
        }
        // Skip audit fields
        if (AUDIT_PATTERN.matcher(field.getName()).matches()) {
            return true;
        }
        return false;
    }

    private static boolean shouldSkipFieldForInsert(@NonNull Field field) {
        int modifiers = field.getModifiers();
        // Skip static and transient fields
        if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
            return true;
        }
        // Skip serialVersionUID
        if (field.getName().equals("serialVersionUID")) {
            return true;
        }
        // Skip relationships (@ManyToOne, @OneToMany, etc.)
        if (field.getAnnotation(ManyToOne.class) != null ||
            field.getAnnotation(OneToMany.class) != null ||
            field.getAnnotation(ManyToMany.class) != null ||
            field.getAnnotation(OneToOne.class) != null) {
            return true;
        }
        return false;
    }

    @NonNull
    private static Map<String, String> processEmbeddedObject(@NonNull Object embeddedObj)
        throws IllegalAccessException {
        Map<String, String> result = new LinkedHashMap<>();
        Field[] embeddedFields = embeddedObj.getClass().getDeclaredFields();
        for (Field embeddedField : embeddedFields) {
            if (shouldSkipFieldForInsert(embeddedField)) {
                continue;
            }
            embeddedField.setAccessible(true);
            Object embeddedValue = embeddedField.get(embeddedObj);
            Column columnAnnotation = embeddedField.getAnnotation(Column.class);
            if (columnAnnotation == null) {
                continue;
            }
            String columnName = getColumnName(embeddedField, columnAnnotation);
            String formattedValue = formatValue(embeddedValue, embeddedField);
            result.put(columnName, formattedValue);
        }
        return result;
    }

    @NonNull
    private static String getColumnName(@NonNull Field field, @NonNull Column columnAnnotation) {
        // If column name is explicitly set in annotation, use it
        if (columnAnnotation.name() != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        // Otherwise, convert camelCase to snake_case
        return field.getName().toLowerCase();
    }

    @NonNull
    private static String formatValue(@Nullable Object value, @NonNull Field field) {
        if (value == null) {
            return "NULL";
        }
        Class<?> type = field.getType();
        // Handle primitives and their wrappers
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return value.toString();
        }
        if (type.isPrimitive() ||
            Number.class.isAssignableFrom(type)) {
            return value.toString();
        }
        // Handle enums
        if (type.isEnum() || field.getAnnotation(Enumerated.class) != null) {
            return START_STRING + value.toString() + START_STRING;
        }
        // Handle Lists
        if (List.class.isAssignableFrom(type)) {
            List<?> enumList = (List<?>) value;
            return START_STRING + enumList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(EnumSetConverter.DELIMITER)) + START_STRING;
        }
        // Handle custom converters
        if (field.getAnnotation(Convert.class) != null) {
            return START_STRING + value.toString().replace(START_STRING, "''") + START_STRING;
        }
        // Handle strings and everything else
        String stringValue = value.toString().replace(START_STRING, "''");
        return START_STRING + stringValue + START_STRING;
    }

    @NonNull
    private static String toDefaultValue(@NonNull Field field, @NonNull RequirementSetting defaultSetting, boolean defaultBoolean) {
        Class<?> type = field.getType();
        if (field.getName().equals("tenantId")) {
           return START_STRING + "DEFAULT" + START_STRING;
        }
        if (field.getName().equals("version")) {
            return "0";
        }
        if (field.getName().equals("bezeichnung")) {
            return START_STRING + "Default" + START_STRING;
        }
        // Handle primitives and their wrappers
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return defaultBoolean ? "TRUE" : "FALSE";
        }
        // Handle Lists
        if (List.class.isAssignableFrom(type)) {
            return START_STRING + START_STRING;
        }
        // Alles andere
        return START_STRING + defaultSetting + START_STRING;
    }
}
