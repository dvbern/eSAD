package ch.dvbern.dvbstarter.shared.types.id;

import ch.dvbern.dvbstarter.shared.types.GenericStringConverter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.persistence.Converter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Converter(autoApply = true)
public class HasUntersuchungIDWrapperConverter extends GenericStringConverter<HasUntersuchungIDWrapper> {

    protected HasUntersuchungIDWrapperConverter() {
        super(HasUntersuchungIDWrapperConverter::toExternalForm, HasUntersuchungIDWrapperConverter::fromString);
    }

    public static void registerJackson(
        SimpleModule module
    ) {
        GenericStringConverter.registerJackson(
            module,
            HasUntersuchungIDWrapper.class,
            HasUntersuchungIDWrapperConverter::toExternalForm,
            HasUntersuchungIDWrapperConverter::fromString
        );
    }

    private static @Nullable String toExternalForm(@Nullable HasUntersuchungIDWrapper hasUntersuchungIDWrapper) {
        if(hasUntersuchungIDWrapper == null){
            return null;
        }
        return hasUntersuchungIDWrapper.toExternalForm();
    }

    private static @Nullable HasUntersuchungIDWrapper fromString(@Nullable String string) {
        if( string == null){
            return null;
        }
        return HasUntersuchungIDWrapper.fromString(string);
    }
}
