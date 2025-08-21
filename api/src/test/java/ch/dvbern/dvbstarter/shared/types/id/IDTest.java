package ch.dvbern.dvbstarter.shared.types.id;

import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings({ "serial", "LimitedScopeInnerClass", "NewClassNamingConvention" })
class IDTest {

    @Getter
    @Setter
    private static class FooEntity extends AbstractEntity<FooEntity> {
        private ID<FooEntity> id;
    }

    @Test
    void Parsing() {
        ID<FooEntity> actual = ID.parse("3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

        assertThat(actual.getUUID())
            .isEqualTo(UUID.fromString("3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3"));
        assertThat(actual.getEntityClazz())
            .isEqualTo(FooEntity.class);
    }

    @Test
    void toString_formats_as_expected() {
        ID<FooEntity> actual = ID.parse("3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

        assertThat(actual)
            .hasToString("ID[3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3,FooEntity]");
    }

    @Test
    void random_creation() {
        ID<FooEntity> actual = ID.random(FooEntity.class);

        assertThat(actual.getUUID())
            .isInstanceOf(UUID.class);
        assertThat(actual.getEntityClazz())
            .isEqualTo(FooEntity.class);
    }

    @Nested
    class Equality {
        @Test
        void equal_ids_are_equal() {
            ID<FooEntity> a = ID.parse("3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);
            ID<FooEntity> b = ID.parse("3c2d0ee3-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

            assertThat(a)
                .isEqualTo(b)
                .hasSameHashCodeAs(b);
        }

        @Test
        void different_UUIDs_are_different() {
            ID<FooEntity> a = ID.parse("aaaaaaaa-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);
            ID<FooEntity> b = ID.parse("bbbbbbbb-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

            assertThat(a)
                .isNotEqualTo(b)
                .doesNotHaveSameHashCodeAs(b);
        }

        @Test
        void different_Classes_are_different() {
            @Getter
            @Setter
            class BarEntity extends AbstractEntity<BarEntity> {
                private ID<BarEntity> id;
            }

            ID<FooEntity> a = ID.parse("aaaaaaaa-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);
            ID<BarEntity> b = ID.parse("aaaaaaaa-c07e-4e1e-96ef-00c8fee6a9a3", BarEntity.class);

            assertThat(a)
                .isNotEqualTo(b)
                .doesNotHaveSameHashCodeAs(b);
        }

        @Test
        void same_instances_are_equal() {
            ID<FooEntity> a = ID.parse("aaaaaaaa-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

            //noinspection EqualsWithItself
            assertThat(a)
                .isEqualTo(a)
                .hasSameHashCodeAs(a);
        }

        @Test
        void differing_objects_are_different() {
            ID<FooEntity> a = ID.parse("aaaaaaaa-c07e-4e1e-96ef-00c8fee6a9a3", FooEntity.class);

            assertThat(a)
                .isNotEqualTo(new Object());
        }

    }

}
