package ch.dvbern.dvbstarter.infrastructure.db;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.QAbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.archunit.ArchConst;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import com.querydsl.core.types.Path;
import com.tngtech.archunit.core.domain.JavaClass;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class QueryDslWarmupTest {

    @Inject
    protected QueryDslWarmup sut;

    @Nested
    class OnStartup {

        @Test
        void initializes_the_query_classes_involved_in_the_known_deadlock() {
            // ACT
            Set<String> actual = sut.getInitializedQueryClasses();

            // ASSERT
            assertThat(actual)
                .contains(
                    QAbstractEntity.class.getName(),
                    QEinladung.class.getName(),
                    QUntersuchung.class.getName(),
                    QUntersuchungImpfungen.class.getName(),
                    QUntersuchungImpfungIntern.class.getName()
                );
        }

        @Test
        void initializes_every_generated_query_class_that_holds_a_static_path_instance() {
            // ARRANGE
            Set<String> generatedPathClasses = ArchConst.APP_CLASSES.stream()
                .filter(javaClass -> javaClass.getSimpleName().startsWith(QueryDslWarmup.QUERY_CLASS_PREFIX))
                .filter(javaClass -> javaClass.isAssignableTo(Path.class))
                .map(JavaClass::getName)
                .collect(Collectors.toSet());

            // ACT
            Set<String> actual = sut.getInitializedQueryClasses();

            // ASSERT
            assertThat(generatedPathClasses)
                .isNotEmpty();
            // test sources contribute additional entities (and thus query classes) to the metamodel
            assertThat(actual)
                .containsAll(generatedPathClasses);
        }
    }

    @Nested
    class QueryClassName {

        @Test
        void prefixes_the_simple_name_with_Q_in_the_same_package() {
            // ACT
            String actual = QueryDslWarmup.queryClassName(Benutzer.class);

            // ASSERT
            assertThat(actual)
                .isEqualTo("ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer");
        }

        @Test
        void joins_nested_classes_with_an_underscore() {
            // ACT
            String actual = QueryDslWarmup.queryClassName(Map.Entry.class);

            // ASSERT
            assertThat(actual)
                .isEqualTo("java.util.QMap_Entry");
        }
    }
}
