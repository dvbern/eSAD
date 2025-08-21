package ch.dvbern.dvbstarter.shared.types.cleanfilename;

import ch.dvbern.dvbstarter.shared.types.GenericStringConverter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Converter(autoApply = true)
public class CleanFileNameConverter extends GenericStringConverter<CleanFileName> {

    public CleanFileNameConverter() {
        super(CleanFileNameConverter::toExternalForm, CleanFileNameConverter::fromString);
    }

    private static @Nullable String toExternalForm(@Nullable CleanFileName fileName) {
        return fileName == null
            ? null
            : fileName.getFileName();
    }

    private static @Nullable CleanFileName fromString(@Nullable String s) {
        return s == null
            ? null
            : CleanFileName.parse(s);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        GenericStringConverter.registerJackson(
            module,
            CleanFileName.class,
            CleanFileNameConverter::toExternalForm,
            CleanFileNameConverter::fromString
        );
    }
}
