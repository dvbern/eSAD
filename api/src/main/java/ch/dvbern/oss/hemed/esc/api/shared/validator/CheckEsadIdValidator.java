package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@RequiredArgsConstructor
@ApplicationScoped
public class CheckEsadIdValidator extends AbstractValidator<CheckEsadId, String> {

    private static final Pattern ESADID_REGEX = Pattern.compile("^[0-9A-Z]\\d{8}$");
    private static final Pattern BLANK_SPACES = Pattern.compile("\\s");

    @Override
    public boolean isEntityValid(@NonNull String s) {
        String esadId = BLANK_SPACES.matcher(s).replaceAll("");
        return ESADID_REGEX.matcher(esadId).matches();
    }
}
