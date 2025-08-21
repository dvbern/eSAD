package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import org.assertj.core.api.AbstractAssert;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class AppValidationErrorJsonSpecAssert extends AbstractAssert<AppValidationErrorJsonSpecAssert, AppValidationErrorJsonSpec> {

    private AppValidationErrorJsonSpecAssert(AppValidationErrorJsonSpec actual) {
        super(actual, AppValidationErrorJsonSpecAssert.class);
    }

    public static AppValidationErrorJsonSpecAssert assertThatAppValidationErrorJsonSpec(AppValidationErrorJsonSpec actual) {
        return new AppValidationErrorJsonSpecAssert(actual);
    }

    public ViolationJsonSpecAssert extractingExactlyOneViolation(){
        isNotNull();
        if(actual.getViolations().size() != 1){
            failWithMessage("Expected AppValidationErrorJsonSpec to have exactly one violation but had %d", actual.getViolations().size());
        }
        return ViolationJsonSpecAssert.assertThatViolationJsonSpec(actual.getViolations().getFirst());

    }

}
