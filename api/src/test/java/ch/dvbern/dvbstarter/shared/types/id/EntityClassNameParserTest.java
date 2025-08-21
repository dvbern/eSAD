package ch.dvbern.dvbstarter.shared.types.id;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings({ "LimitedScopeInnerClass", "EmptyClass", "unused" })
class EntityClassNameParserTest {

    private static class TypeWithGenericParam<T> {
    }

    private final EntityClassNameParser parser = new EntityClassNameParser(TypeWithGenericParam.class);

    @Test
    void parses_simple_type_parameter() throws NoSuchFieldException {
        class MyTypeParam {
        }
        class SomeEntity {
            private final TypeWithGenericParam<MyTypeParam> field = null;
        }
        var type = SomeEntity.class.getDeclaredField("field").getGenericType();

        var entityClass = parser.entityClassFrom(type);

        assertThat(entityClass).isEqualTo(MyTypeParam.class);
    }

    @Test
    void parses_type_parameter_with_genric_param() throws NoSuchFieldException {
        class MyTypeParam<U> {
        }
        class SomeEntity {
            private final TypeWithGenericParam<MyTypeParam<Object>> field = null;
        }
        var type = SomeEntity.class.getDeclaredField("field").getGenericType();

        var entityClass = parser.entityClassFrom(type);

        assertThat(entityClass).isEqualTo(MyTypeParam.class);
    }

    @Test
    void rejects_other_type_parameter_types_because_not_implemented_yet() throws NoSuchFieldException {
        class SomeEntity {
            private final TypeWithGenericParam<?> field = null;
        }
        var type = SomeEntity.class.getDeclaredField("field").getGenericType();

        var ex = assertThrows(
            IllegalArgumentException.class,
            () -> parser.entityClassFrom(type)
        );

        assertThat(ex)
            .hasMessageContaining("Not yet implemented")
            .hasMessageContaining("WildcardType");
    }

    @Test
    void methods_not_implemented_yet() throws NoSuchMethodException {
        class MyTypeParam {
        }
        class SomeEntity {
            public void method(TypeWithGenericParam<MyTypeParam> param) {
            }
        }
        var type = SomeEntity.class.getMethod("method", TypeWithGenericParam.class)
            .getParameterTypes()[0];

        assertThrows(
            IllegalArgumentException.class,
            () -> parser.entityClassFrom(type)
        );
    }

}
