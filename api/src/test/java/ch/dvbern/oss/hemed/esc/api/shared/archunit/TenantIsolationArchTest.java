package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.RolesAugmentor;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantExtractorFromUrlRequestFilter;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3RequestBuilder;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaModifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import static ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst.APP_CLASSES;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.have;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * One tenant = one request context (see {@link PerTenantRunnerService}). These rules prevent tenant state from
 * being captured in a bean and silently reused for another tenant.
 * <p>
 * Review rule (not checked here): caches in {@code @ApplicationScoped} beans must contain the tenant in their key
 * (e.g. {@code TLProducer.bundleMap} keyed by {@code LanguageTenantKey}).
 */
@DisplayNameGeneration(ReplaceUnderscores.class)
class TenantIsolationArchTest {

    private static final DescribedPredicate<JavaClass> CDI_BEAN =
        metaAnnotatedWith(NormalScope.class)
            .or(annotatedWith(Dependent.class))
            .or(annotatedWith(Singleton.class))
            .or(metaAnnotatedWith(Stereotype.class))
            .forSubtype();

    private static final DescribedPredicate<JavaClass> LONG_LIVED_BEAN =
        annotatedWith(ApplicationScoped.class)
            .or(annotatedWith(Singleton.class))
            .forSubtype();

    private static final DescribedPredicate<JavaConstructor> TENANT_ENUM_PARAMETER =
        DescribedPredicate.describe(
            "a TenantEnum parameter",
            constructor -> constructor.getRawParameterTypes()
                .stream()
                .anyMatch(type -> type.isEquivalentTo(TenantEnum.class))
        );

    @Test
    void TenantEnum_is_not_produced_as_a_bean() {
        methods()
            .that()
            .areAnnotatedWith(Produces.class)
            .should()
            .notHaveRawReturnType(TenantEnum.class)
            .because("a produced tenant can be captured in a field, use TenantContext.get()")
            .check(APP_CLASSES);
    }

    @Test
    void TenantEnum_is_not_injected_into_constructors_of_beans() {
        constructors()
            .that()
            .areDeclaredInClassesThat(CDI_BEAN)
            .should(not(have(TENANT_ENUM_PARAMETER)))
            .because("the tenant must be read through TenantContext.get() when it is needed")
            .check(APP_CLASSES);
    }

    @Test
    void TenantEnum_is_not_injected_into_fields() {
        noFields()
            .that()
            .areAnnotatedWith(Inject.class)
            .should()
            .haveRawType(TenantEnum.class)
            .because("the tenant must be read through TenantContext.get() when it is needed")
            .check(APP_CLASSES);
    }

    @Test
    void long_lived_beans_do_not_hold_tenant_derived_objects() {
        fields()
            .that()
            .areDeclaredInClassesThat(LONG_LIVED_BEAN)
            .should()
            .notHaveRawType(S3AsyncClient.class)
            .andShould()
            .notHaveRawType(S3RequestBuilder.class)
            .andShould()
            .notHaveRawType(ArchivierungRuleService.class)
            .because("these objects are created for one tenant and must not outlive its request context")
            .check(APP_CLASSES);
    }

    @Test
    void only_documented_classes_set_the_current_tenant() {
        noClasses()
            .that()
            .doNotBelongToAnyOf(
                TenantContext.class,
                PerTenantRunnerService.class,
                TenantExtractorFromUrlRequestFilter.class,
                // publishes the tenant of the current HTTP request
                RolesAugmentor.class
            )
            .should()
            .callMethod(TenantContext.class, "setCurrentTenant", TenantEnum.class)
            .because("switching the tenant inside a request context reuses the beans of the previous tenant,"
                + " use PerTenantRunnerService")
            .check(APP_CLASSES);
    }

    @Test
    void only_PerTenantRunnerService_iterates_all_tenants() {
        noClasses()
            .that()
            .doNotBelongToAnyOf(
                TenantEnum.class,
                PerTenantRunnerService.class,
                // maps a URL to its tenant, does not run anything per tenant
                TenantExtractorFromUrlRequestFilter.class
            )
            // switch statements over enums compile to synthetic classes calling values()
            .and()
            .doNotHaveModifier(JavaModifier.SYNTHETIC)
            .should()
            .callMethod(TenantEnum.class, "values")
            .because("use PerTenantRunnerService.getActiveTenants() / runForActiveTenants()")
            .check(APP_CLASSES);
    }
}
