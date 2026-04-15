package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Function;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class ValueEnhancedUtil {

    public static final String NICHT_GEPRUEFT = "NICHT_GEPRUEFT";

    @NonNull
    public static <E extends Enum<E>> String translateEnumValueForColumnToFlag(
        @Nullable SingleValueEnhanced<E> valueEnhanced,
        @NonNull Enum<E> enumValueForColumn,
        @NonNull TL tl,
        @NonNull Class<E> clazz
    ) {
        boolean isEnumValueForColumn = Optional.ofNullable(valueEnhanced)
            .map(value -> value.getEnumValue(clazz))
            .map(enumValueForColumn::equals)
            .orElse(false);
        return PrintUtil.trimToPlaceholder(isEnumValueForColumn, tl);
    }

    @NonNull
    public static <E extends Enum<E>> String translateEnhancedEnumValue(
        @Nullable SingleValueEnhanced<E> valueEnhanced,
        @NonNull TL tl,
        @NonNull Class<E> clazz
    ) {
        return Optional.ofNullable(valueEnhanced)
            .map(value -> value.getEnumValue(clazz))
            .map(tl::translateValue)
            .orElse("");
    }

    @NonNull
    public static <E extends Enum<E>, U extends AbstractEntity<U>> SingleValueEnhanced<E> getSingleValueEnhancedForUntersuchungAbschliessen(
        @NonNull AnlassConfig config,
        @NonNull Function<@NonNull AnlassConfig, @NonNull RequirementSetting> extractRequirement,
        @Nullable U untersuchtung,
        @NonNull Function<@NonNull U, @Nullable SingleValueEnhanced<E>> extract,
        @NonNull E defaultWert,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        if (!extractRequirement.apply(config).isVisible()) {
            return SingleValueEnhanced.of();
        }

        if (untersuchtung == null) {
            return SingleValueEnhanced.of(defaultWert, null);
        }

        SingleValueEnhanced<E> value = extract.apply(untersuchtung);

        if (value == null || getEnumValueOrNull(value, defaultWert.getDeclaringClass()) == null) {
            if (nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv && defaultWert.toString().equals(NICHT_GEPRUEFT)) {
                return SingleValueEnhanced.of(defaultWert, NichtGeprueftGrund.ANDERER_GRUND);
            }
            return SingleValueEnhanced.of(defaultWert, null);
        }

        return value;
    }

    @NonNull
    public static <E extends Enum<E>, U extends AbstractEntity<U>> MultiValueEnhanced<E> getMultiValueEnhancedForUntersuchungAbschliessen(
        @NonNull AnlassConfig config,
        @NonNull Function<@NonNull AnlassConfig, @NonNull RequirementSetting> extractRequirement,
        @Nullable U untersuchtung,
        @NonNull Function<@NonNull U, @Nullable MultiValueEnhanced<E>> extract,
        @NonNull E defaultWert,
        boolean nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv
    ) {
        if (!extractRequirement.apply(config).isVisible()) {
            return MultiValueEnhanced.of();
        }

        if (untersuchtung == null) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }

        MultiValueEnhanced<E> value = extract.apply(untersuchtung);

        if (value == null || getEnumSetOrEmpty(value, defaultWert.getDeclaringClass()).isEmpty()) {
            if (nichtGeprueftGrundFeatureIsEnabledForDeploymentEnv && defaultWert.toString().equals(NICHT_GEPRUEFT)) {
                return MultiValueEnhanced.of(EnumSet.of(defaultWert), NichtGeprueftGrund.ANDERER_GRUND);
            }
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }

        return value;
    }

    public static <E extends Enum<E>> boolean isRequiredAndNull(
        @NonNull RequirementSetting requirementSetting,
        @Nullable SingleValueEnhanced<E> singleValueEnhanced,
        @NonNull Class<E> clazz
    ) {
        return requirementSetting.isRequired() && isNull(singleValueEnhanced, clazz);
    }

    public static <E extends Enum<E>> boolean isVisibleAndNull(
        @NonNull RequirementSetting requirementSetting,
        @Nullable SingleValueEnhanced<E> singleValueEnhanced,
        @NonNull Class<E> clazz
    ) {
        return requirementSetting.isVisible() && isNull(singleValueEnhanced, clazz);
    }

    @Nullable
    public static <E extends Enum<E>> E getEnumValueOrNull(
        @Nullable SingleValueEnhanced<E> singleValueEnhanced,
        @NonNull Class<E> clazz
    ) {
        return Optional.ofNullable(singleValueEnhanced).map(value -> value.getEnumValue(clazz)).orElse(null);
    }

    @NonNull
    public static <E extends Enum<E>> EnumSet<E> getEnumSetOrEmpty(
        @Nullable MultiValueEnhanced<E> multiValueEnhanced,
        @NonNull Class<E> clazz
    ) {
        return Optional.ofNullable(multiValueEnhanced)
            .map(value -> value.getEnumValues(clazz))
            .orElse(EnumSet.noneOf(clazz));
    }

    public static <E extends Enum<E>> boolean isNull(
        @Nullable SingleValueEnhanced<E> singleValueEnhanced,
        @NonNull Class<E> clazz
    ) {
        return getEnumValueOrNull(singleValueEnhanced, clazz) == null;
    }
}
