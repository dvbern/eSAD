package ch.dvbern.oss.hemed.esc.api.testutil.hamcrest;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

@UtilityClass
public class MyMatchers {

    public static Matcher<String> matchesUUID() {
        return Matchers.matchesPattern("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
    }

    public static Matcher<String> matchesExceptionId() {
        return Matchers.matchesPattern("^[a-zA-Z0-9+/]+$");
    }
}
