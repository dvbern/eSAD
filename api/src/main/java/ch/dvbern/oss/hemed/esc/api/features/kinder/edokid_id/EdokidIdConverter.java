package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import ch.dvbern.dvbstarter.shared.types.GenericStringConverter;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Converter(autoApply = true)
public class EdokidIdConverter extends GenericStringConverter<EdokidId> {

    public EdokidIdConverter() {
        super(EdokidIdConverter::toInternalForm, EdokidIdConverter::fromString);
    }

    @Nullable
    private static String toExternalForm(@IgnoreForIdClassTest @Nullable EdokidId edokidId) {
        return edokidId == null ? null : edokidId.toFormattedString();
    }

    @Nullable
    private static String toInternalForm(@IgnoreForIdClassTest @Nullable EdokidId edokidId) {
        return edokidId == null ? null : edokidId.toString();
    }

    @Nullable
    private static EdokidId fromString(@IgnoreForIdClassTest @Nullable String edokidId) {
        if (edokidId == null) {
            return null;
        }

        return EdokidId.parse(edokidId);
    }

    public static void registerJackson(@NonNull SimpleModule module) {
        GenericStringConverter.registerJackson(
            module,
            EdokidId.class,
            EdokidIdConverter::toExternalForm,
            EdokidIdConverter::fromString
        );
    }



}
