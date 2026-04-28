package ch.dvbern.oss.hemed.esc.api.shared.archunit;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResourcePlain;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleProducer;
import ch.dvbern.oss.hemed.esc.api.shared.mapper.BaseMapper;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.Slice;
import jakarta.persistence.Entity;
import jakarta.ws.rs.Path;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst.APP_CLASSES;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst.APP_PACKAGE;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst.STARTER_PACKAGE;
import static ch.dvbern.oss.hemed.esc.api.shared.archunit.predicates.ClassPredicates.interfaceWithSuffix;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameStartingWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.have;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.is;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.codeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class LayeringTest {

    public static final DescribedPredicate<JavaClass> SERVICE =
        is(interfaceWithSuffix("Service")).or(is(metaAnnotatedWith(Service.class)));

    public static final DescribedPredicate<JavaClass> UTIL =
        is(JavaClass.Predicates.simpleNameEndingWith("Util").or(is(interfaceWithSuffix("Util"))));

    public static final DescribedPredicate<JavaClass> NO_TRANSACTION_SERVICE =
        is(metaAnnotatedWith(NoTransactionService.class));
    public static final DescribedPredicate<JavaClass> REPOSITORY =
        is(interfaceWithSuffix("Repo")).or(is(metaAnnotatedWith(Repository.class)));
    public static final DescribedPredicate<JavaClass> RESOURCE = metaAnnotatedWith(JaxResource.class)
        .or(metaAnnotatedWith(JaxResourcePlain.class)).forSubtype();
    public static final DescribedPredicate<JavaClass> RESOURCE_MAPPER = metaAnnotatedWith(JaxMapper.class).forSubtype();
    public static final DescribedPredicate<JavaClass> AUTO_MAPPER =
        is(JavaClass.Predicates.implement(BaseMapper.class));

    public static final DescribedPredicate<JavaClass> JSON_DTO = metaAnnotatedWith(JaxDTO.class).forSubtype();
    public static final DescribedPredicate<JavaClass> ENTITY = annotatedWith(Entity.class).forSubtype();

    private static final DescribedPredicate<JavaClass> QUERYDSL_Q_CLASSES =
        simpleNameStartingWith("Q").and(assignableTo(EntityPathBase.class));

    public static final DescribedPredicate<JavaClass> SHARED = resideInAPackage(APP_PACKAGE + ".shared..");

    private static final DescribedPredicate<JavaClass> DATABASE_LAYER =
        resideInAPackage(STARTER_PACKAGE + ".infrastructure.db..")
            .or(resideInAPackage(APP_PACKAGE + ".shared.abstractentity.."))
            .or(resideInAPackage(APP_PACKAGE + ".shared.validator.."))
            .or(resideInAPackage(APP_PACKAGE + ".shared.util.."))
            .or(LayeringTest.REPOSITORY)
            .or(ENTITY)
            .or(QUERYDSL_Q_CLASSES);


    public static final DescribedPredicate<JavaClass> SHARED_PRINTER =
        resideInAPackage(APP_PACKAGE + ".shared.printer..");

    public static final DescribedPredicate<JavaClass> CSV_COLUMNS =
        resideInAPackage(APP_PACKAGE + ".features.spss.columns..");

    public static final DescribedPredicate<JavaClass> DOC_MERGER =
        resideInAPackage(APP_PACKAGE + ".features.briefvorlagen.briefgenerierung.docmerger..");

    public static final DescribedPredicate<JavaClass> STARTER_SHARED = resideInAPackage(STARTER_PACKAGE + ".shared..");
    public static final DescribedPredicate<JavaClass> STARTER_INFRASTRUCTURE =
        resideInAPackage(STARTER_PACKAGE + ".infrastructure..");


    private static final Architectures.LayeredArchitecture LAYERS = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Resource")
        .definedBy(or(RESOURCE))
        .layer("ResourceMapper")
        .definedBy(RESOURCE_MAPPER)
        .layer("Service")
        .definedBy(SERVICE)
        .layer("Database")
        .definedBy(DATABASE_LAYER)
        .layer("Entity")
        .definedBy(ENTITY)
        .layer("Shared")
        .definedBy(SHARED)
        .layer("Infrastructure")
        .definedBy(STARTER_SHARED.or(STARTER_INFRASTRUCTURE));

    @Test
    void app_package_exists() {
        var packagePrefix = "%s.".formatted(APP_PACKAGE);

        assertThat(Package.getPackages())
            .extracting(Package::getName)
            .anyMatch(name -> name.startsWith(packagePrefix));
    }

    @Test
    void starter_package_exists() {
        var packagePrefix = "%s.".formatted(STARTER_PACKAGE);

        assertThat(Package.getPackages())
            .extracting(Package::getName)
            .anyMatch(name -> name.startsWith(packagePrefix));
    }

    @Test
    void test_layer_boundaries() {
        LAYERS
            .whereLayer("Resource")
            .mayNotBeAccessedByAnyLayer()
            .check(APP_CLASSES);

        LAYERS
            .whereLayer("ResourceMapper")
            .mayOnlyBeAccessedByLayers("Resource")
            .check(APP_CLASSES);
    }

    @Test
    void test_layer_boundaries_entities_codeUnits() {
        final ArchRule rule = codeUnits().that()
            .areDeclaredInClassesThat(are(DATABASE_LAYER))
            .should()
            .onlyBeCalled()
            .byClassesThat(are(DATABASE_LAYER).or(SERVICE)
                .or(RESOURCE_MAPPER)
                .or(AUTO_MAPPER)
                .or(STARTER_INFRASTRUCTURE)
                .or(STARTER_SHARED)
                .or(SHARED_PRINTER)
                .or(UTIL)
                .or(CSV_COLUMNS)
                .or(DOC_MERGER)
                .or(have(simpleNameStartingWith(ArchivierungRuleProducer.class.getSimpleName())))
            )
            .because("Entities should not be used in Resource Layer");
        rule.allowEmptyShould(true).check(APP_CLASSES);
    }

    @Test
    void test_layer_boundaries_entities_constructors() {
        final ArchRule rule = constructors().that()
            .areDeclaredInClassesThat(are(DATABASE_LAYER))
            .should()
            .onlyBeCalled()
            .byClassesThat(are(DATABASE_LAYER).or(SERVICE)
                .or(RESOURCE_MAPPER)
                .or(AUTO_MAPPER)
                .or(STARTER_INFRASTRUCTURE)
                .or(STARTER_SHARED)
                .or(DOC_MERGER))
            .because("Entities should not be used in Resource Layer");
        rule.allowEmptyShould(true).check(APP_CLASSES);
    }

    @Test
    void test_layer_boundaries_entities_methods() {
        final ArchRule rule = methods().that()
            .areDeclaredInClassesThat(are(DATABASE_LAYER))
            .should()
            .onlyBeCalled()
            .byClassesThat(are(DATABASE_LAYER).or(SERVICE).or(UTIL)
                .or(RESOURCE_MAPPER)
                .or(AUTO_MAPPER)
                .or(STARTER_INFRASTRUCTURE)
                .or(STARTER_SHARED)
                .or(SHARED_PRINTER)
                .or(CSV_COLUMNS)
                .or(DOC_MERGER)
                .or(have(simpleNameStartingWith(ArchivierungRuleProducer.class.getSimpleName())))
            )
            .because("Entities should not be used in Resource Layer");
        rule.allowEmptyShould(true).check(APP_CLASSES);
    }

    @Test
    void no_cycles_between_features() {
        var rule = slices()
            .matching(APP_PACKAGE + ".features.(*)..")
            .should()
            .beFreeOfCycles()
            .because("Cycles between feature decrease maintainability. Introduce a new shared feature");
        rule.allowEmptyShould(true).check(ArchConst.APP_CLASSES);
    }

    @Test
    void no_cross_feature_repo_access() {
        // FIXME: sinnlos: repos duerfen nicht Repos von anderen Features brauchen... aber alles Andere schon?
        //  Ausserdem: das ein Repo ein Anderes einbaut ist schon seeeeehr selten.
        //  Umbauen auf: Features duerfen nur Service-Interfaces von anderen Features brauchen.
        var rule = slices()
            .matching(ArchConst.APP_PACKAGE + ".features.(*).repo")
            .that(areNotInTheCommonPackage())
            .should()
            .notDependOnEachOther()
            .because("Use a service to access repos outside of the feature package");
        rule.allowEmptyShould(true).check(ArchConst.APP_CLASSES);
    }

    @Test
    void jax_resource_plain_must_not_have_path_annotation() {
        var rule = classes().that()
            .areAnnotatedWith(JaxResourcePlain.class)
            .should()
            .notBeAnnotatedWith(Path.class)
            .because("The JaxResourcePlain is intended for filters etc that belong to the Resource layer but do not want transactional");

        rule.allowEmptyShould(true).check(ArchConst.APP_CLASSES);
    }

    private static @NonNull DescribedPredicate<Slice> areNotInTheCommonPackage() {
        return new DescribedPredicate<>("features") {
            @Override
            public boolean test(Slice javaClasses) {
                return !javaClasses.getNamePart(1).equals("common");
            }
        };
    }



}
