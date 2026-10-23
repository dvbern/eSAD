package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import io.quarkus.arc.Arc;
import io.quarkus.test.junit.callback.QuarkusTestBeforeTestExecutionCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@NoArgsConstructor
public class TestSecuritySetupCallback implements QuarkusTestBeforeTestExecutionCallback {

    @Override
    public void beforeTestExecution(@NonNull QuarkusTestMethodContext context) {
        Method testMethod = context.getTestMethod();
        Class<?> testClass = context.getTestInstance().getClass();

        Annotation testAsAnnotation = findTestAsAnnotation(testMethod, testClass);
        if (testAsAnnotation == null) {
            return;
        }

        Class<? extends Annotation> annotationType = testAsAnnotation.annotationType();
        String tenant = extractTenantFromAnnotation(annotationType);
        String benutzerId = extractBenutzerIdFromAnnotation(annotationType);

        if (tenant == null || benutzerId == null) {
            return;
        }
        if (tenant.equals(TenantEnum.WIN.name())) {
            LOG.warn("Skipping test setup for WIN tenant, as the database for WIN is not active in tests");
            return;
        }
        try {
            TenantContext tenantContext = Arc.container().instance(TenantContext.class).get();
            tenantContext.setCurrentTenant(TenantEnum.valueOf(tenant));
        } catch (Exception e) {
            LOG.debug("Could not set tenant for benutzer {} in tenant {}: {}",
                benutzerId, tenant, e.getMessage());
        }
    }

    @Nullable
    private Annotation findTestAsAnnotation(@NonNull Method testMethod, @NonNull Class<?> testClass) {
        for (Annotation annotation : testMethod.getAnnotations()) {
            if (isTestAsAnnotation(annotation)) {
                return annotation;
            }
        }
        for (Annotation annotation : testClass.getAnnotations()) {
            if (isTestAsAnnotation(annotation)) {
                return annotation;
            }
        }
        return null;
    }

    private boolean isTestAsAnnotation(@NonNull Annotation annotation) {
        Class<? extends Annotation> type = annotation.annotationType();
        return type.isAnnotationPresent(TestAsMarker.class)
            && type.isAnnotationPresent(OidcSecurity.class);
    }

    @Nullable
    private String extractTenantFromAnnotation(@NonNull Class<? extends Annotation> annotationType) {
        OidcSecurity oidcSecurity = annotationType.getAnnotation(OidcSecurity.class);
        if (oidcSecurity == null) {
            return null;
        }
        for (Claim claim : oidcSecurity.claims()) {
            if ("tenant".equals(claim.key())) {
                return claim.value();
            }
        }
        return null;
    }

    @Nullable
    private String extractBenutzerIdFromAnnotation(@NonNull Class<? extends Annotation> annotationType) {
        try {
            Field field = annotationType.getField("BENUTZER_ID");
            return (String) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
