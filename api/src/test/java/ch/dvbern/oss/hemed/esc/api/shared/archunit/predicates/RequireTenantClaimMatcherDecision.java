package ch.dvbern.oss.hemed.esc.api.shared.archunit.predicates;

import ch.dvbern.oss.hemed.esc.api.shared.auth.PermitAllEndpointConstants;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequireTenantClaimMatcherDecision {

    public static DescribedPredicate<JavaMethod> handledByTenantClaimMatcher() {
        return DescribedPredicate.describe(
            "handled by TenantClaimMatcher",
            method -> {
                var resourceClass = method.getOwner().reflect();
                return PermitAllEndpointConstants.ALL_PUBLIC_RESOURCES.containsValue(resourceClass);
            }
        );
    }
}
