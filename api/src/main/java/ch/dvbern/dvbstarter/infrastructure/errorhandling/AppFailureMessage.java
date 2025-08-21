package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerTyp;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AppFailureMessage {

    public static final String MESSAGE = "message";
    public static final String ENTITY_NAME = "entityName";

    @NonNull
    public static AppFailureMessage internalError(@NonNull String message) {
        return build(I18nMessage.of(
            "AppFailure.INTERNAL_ERROR",
            MESSAGE, message
        ));
    }

    @NonNull
    public static AppFailureMessage jsonParsing(@NonNull String message, @NonNull String location) {
        return build(I18nMessage.of(
            "AppFailure.JSON_PARSING",
            MESSAGE, message,
            "location", location
        ));
    }

    @NonNull
    public static AppFailureMessage jsonMapping() {
        return build(I18nMessage.of(
            "AppFailure.JSON_MAPPING"
        ));
    }

    @NonNull
    public static AppFailureMessage changedByOtherUser() {
        return build(I18nMessage.of(
            "AppFailure.CHANGED_BY_OTHER_USER"
        ));
    }

    @NonNull
    public static AppFailureMessage couldNotCreateEntity(@NonNull String entityName) {
        return build(I18nMessage.of(
            "AppFailure.COULD_NOT_CREATE_ENTITY",
            ENTITY_NAME, entityName
        ));
    }

    @NonNull
    public static AppFailureMessage edokidIdLastLaufnummerReached() {
        return build(I18nMessage.of("AppFailure.EDOKID_ID_LAST_LAUFNUMMER_REACHED"));
    }

    @NonNull
    public static AppFailureMessage anlassIsNotAbgeschlossen() {
        return build(I18nMessage.of("AppFailure.AppFailure.ANLASS_NOT_ABGESCHLOSSEN"));
    }

    @NonNull
    public static AppFailureMessage entityNotFound(@NonNull ID<?> id) {
        return build(I18nMessage.of(
            "AppFailure.ENTITY_NOT_FOUND",
            ENTITY_NAME, id.getEntityClazz().getSimpleName(),
            "id", id.getUUID().toString()
        ));
    }

    @NonNull
    public static AppFailureMessage entityNotFound(@NonNull ID<?> id, @NonNull String message) {
        return build(I18nMessage.of(
            "AppFailure.ENTITY_NOT_FOUND_WITH_MESSAGE",
            ENTITY_NAME, id.getEntityClazz().getSimpleName(),
            "id", id.getUUID().toString(),
            MESSAGE, message
        ));
    }

    @NonNull
    public static <T> AppFailureMessage entityNotFoundUsingExternalId(
        @NonNull Class<T> clazz,
        @IgnoreForIdClassTest @NonNull String id) {
        return build(I18nMessage.of(
            "AppFailure.ENTITY_NOT_FOUND",
            ENTITY_NAME, clazz.getSimpleName(),
            "id", id
        ));
    }

    @NonNull
    public static AppFailureMessage invalidTenantClaim(@NonNull String tenantClaim) {
        return build(I18nMessage.of(
            "AppFailure.INVALID_TENANT_CLAIM",
            "claim", tenantClaim
        ));
    }

    @NonNull
    public static AppFailureMessage unrecognizedProperty(
        @NonNull Class<?> owningDto,
        @NonNull String propertyName,
        @NonNull String path,
        @NonNull String allowedProperties
    ) {
        return build(I18nMessage.of(
            "AppFailure.UNRECOGNIZED_PROPERTY",
            "dtoName", owningDto.getSimpleName(),
            "propertyName", propertyName,
            "path", path,
            "allowedProperties", allowedProperties
        ));
    }

    @NonNull
    public static AppFailureMessage schedulerConfigNotFound(@NonNull SchedulerTyp typ) {
        return build(I18nMessage.of(
            "AppFailure.SCHEDULER_CONFIG_NOT_FOUND",
            "schedulerTyp", typ
        ));
    }

    @NonNull
    public static AppFailureMessage anlassConfigNotFound(@NonNull AnlassTyp typ) {
        return build(I18nMessage.of(
            "AppFailure.ANLASS_CONFIG_NOT_FOUND",
            "anlassTyp", typ
        ));
    }

    @NonNull
    public static AppFailureMessage einladungCannotBeRemoved(@NonNull Einladung einladung) {
        return build(I18nMessage.of(
            "AppFailure.EINLADUNG_CANNOT_BE_REMOVED",
            "kindName", einladung.getDossier().getKind().getNameVorname()
        ));
    }

    @NonNull
    public static AppFailureMessage impfungInternCannotBeRemoved() {
        return build(I18nMessage.of(
            "AppFailure.IMPFUNG_INTERN_CANNOT_BE_REMOVED"
        ));
    }

    @NonNull
    public static AppFailureMessage missingBerechtigung(
        Class<?> entity,
        ID<? extends AbstractEntity<?>> id,
        String username
    ) {
        return build(I18nMessage.of(
            "AppFailure.MISSING_BERECHTIGUNG",
            "entity", entity.getSimpleName(),
            "id", id,
            "username", username));
    }

    private final I18nMessage i18nMessage;

    @IgnoreForIdClassTest
    private final ExceptionId id;

    @NonNull
    private static AppFailureMessage build(@NonNull I18nMessage message) {
        var id = ExceptionId.random();
        return new AppFailureMessage(message, id);
    }
}
