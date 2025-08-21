package ch.dvbern.dvbstarter;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.oss.hemed.esc.api.Util.coalesce;
import static ch.dvbern.oss.hemed.esc.api.Util.coalesceString;
import static ch.dvbern.oss.hemed.esc.api.Util.findEnumBy;
import static ch.dvbern.oss.hemed.esc.api.Util.getEnum;
import static ch.dvbern.oss.hemed.esc.api.Util.ifNull;
import static ch.dvbern.oss.hemed.esc.api.Util.isIn;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({ "EmptyClass", "TestMethodWithoutAssertion", "ConstantValue" })
class UtilTest {

    @Getter
    public enum FooEnum {
        BAR("banana"),
        HELLO("world"),
        DUPLICATE1("duplicate"),
        DUPLICATE2("duplicate");

        private final String text;

        FooEnum(String text) {
            this.text = text;
        }

    }

    @AllArgsConstructor
    @Getter
    public static class FormattingTester {
        private final String text;
    }

    @Nested
    class CoalesceTest {
        @Test
        void should_return_empty_on_empty_input() {
            assertThat(coalesce())
                .isEmpty();
        }

        @ParameterizedTest
        @CsvSource(value = {
            "a,  a,  b,  c",
            "b,  null,  b,  c",
            "a,  a,  null,  c",
            "b,  null,  b,  null",
            "a,  a,  null,  null",
            "c,  null,  null,  c",
            "X,  null,  null,  null",
        }, nullValues = "null")
        void should_return_first_nonnull_option(
            @Nullable String expected,
            @Nullable String a,
            @Nullable String b,
            @Nullable String c
        ) {
            assertThat(coalesce(a, b, c)
                .orElse("X"))
                .isEqualTo(expected);
        }
    }

    @Nested
    class CoalesceStringTest {
        @Test
        void should_return_empty_on_empty_input() {
            assertThat(coalesceString())
                .isEmpty();
        }

        @ParameterizedTest
        @CsvSource(value = {
            "a,  a,  b,  c",
            "b,  null,  b,  c",
            "a,  a,  null,  c",
            "b,  null,  b,  null",
            "a,  a,  null,  null",
            "c,  null,  null,  c",
            "X,  null,  null,  null",
        }, nullValues = "null")
        void should_return_first_nonnull_option(
            @Nullable String expected,
            @Nullable String a,
            @Nullable String b,
            @Nullable String c
        ) {
            assertThat(coalesceString(a, b, c)
                .orElse("X"))
                .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource(value = {
            "a,  a,  b,  c",
            "b,  ,  b,  c",
            "a,  a,  ,  c",
            "b,  ,  b,  ",
            "a,  a,  ,  ",
            "c,  ,  ,  c",
            "X,  ,  ,  ",
        }, nullValues = "null")
        void should_return_first_nonblank_option(
            @Nullable String expected,
            @Nullable String a,
            @Nullable String b,
            @Nullable String c
        ) {
            assertThat(coalesceString(a, b, c)
                .orElse("X"))
                .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource(value = {
            "c,  ,  null,  c",
            "c,  null,  ,  c",
            "X,  ,  null,  null",
            "X,  null,  null,  ",
            "X,  ,  null,  ",
            "X,  null,  ,  ",
        }, nullValues = "null")
        void should_return_first_nonblank_nonblank_option(
            @Nullable String expected,
            @Nullable String a,
            @Nullable String b,
            @Nullable String c
        ) {
            assertThat(coalesceString(a, b, c)
                .orElse("X"))
                .isEqualTo(expected);
        }
    }

    @Nested
    class MapNullTest {
        @Test
        void using_the_supplier_returns_fallback_on_null_value() {
            var actual = Stream.of("asdf", null, "asdf")
                .map(Util.mapNull(() -> "Schnitzel"))
                .collect(Collectors.toList());

            assertThat(actual)
                .containsExactly("asdf", "Schnitzel", "asdf");
        }

        @Test
        void using_an_actual_fallback_value_returns_fallback_on_null_value() {
            var actual = Stream.of("asdf", null, "asdf")
                .map(Util.mapNull("Schnitzel"))
                .collect(Collectors.toList());

            assertThat(actual)
                .containsExactly("asdf", "Schnitzel", "asdf");
        }
    }

    @Nested
    class MapTest {
        @Test
        void returns_the_mapped_value() {
            var actual = Util.map("Hello World", String::length);

            assertThat(actual)
                .contains(11);
        }

        @Test
        void returns_empty_for_null_value() {
            Optional<Object> actual = Util.map(null, Function.identity());

            assertThat(actual)
                .isEmpty();
        }

        @Test
        void returns_empty_for_mapped_null_value() {
            Optional<Object> actual = Util.map("asdf", s -> null);

            assertThat(actual)
                .isEmpty();
        }
    }

    @Nested
    public class IfNullTest {

        @Nested
        class EagerTest {
            @Test
            void should_return_value_if_not_null() {
                Object value = new Object();
                Object fallback = new Object();

                assertThat(ifNull(value, fallback))
                    .isEqualTo(value);
            }

            @Test
            void should_returnfallback_if_null() {
                Object value = null;
                Object fallback = new Object();

                assertThat(ifNull(value, fallback))
                    .isEqualTo(fallback);
            }
        }

        @Nested
        class LazyTest {
            @Test
            void should_return_value_if_not_null() {
                Object value = new Object();
                Object fallback = new Object();

                assertThat(ifNull(value, () -> fallback))
                    .isEqualTo(value);
            }

            @Test
            void should_not_call_fallback_if_not_null() {
                ValueHolder valueHolder = new ValueHolder();
                String value = "value";

                assertThat(ifNull(value, () -> {
                    valueHolder.value = "fail";
                    return "fail";
                }))
                    .isEqualTo(value);

                assertThat(valueHolder.value)
                    .isNull();
            }

            @Test
            void should_returnfallback_if_null() {
                Object value = null;
                Object fallback = new Object();

                assertThat(ifNull(value, () -> fallback))
                    .isEqualTo(fallback);
            }

            static class ValueHolder {
                @SuppressWarnings("PublicField")
                public String value = null;
            }

        }
    }

    @Nested
    class FindEnumByTest {
        @Test
        void should_find_nothing_on_false_predicate() {
            Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> "nothing".equals(e.getText()));
            assertThat(actual)
                .isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "BAR,  banana",
            "HELLO,  world"
        })
        void should_find_the_entry(FooEnum expected, String param) {
            Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> param.equals(e.getText()));
            assertThat(actual)
                .hasValue(expected);
        }

        @Test
        void should_find_the_first_duplicate() {
            Optional<FooEnum> actual = findEnumBy(FooEnum.class, e -> "duplicate".equals(e.getText()));
            assertThat(actual)
                .hasValue(FooEnum.DUPLICATE1);
        }
    }

    @Nested
    class GetEnumTest {
        @Test
        void should_throw_on_false_predicate() {
            assertThatThrownBy(() -> getEnum(FooEnum.class, e -> "nothing".equals(e.getText())))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @CsvSource({
            "BAR,  banana",
            "HELLO,  world"
        })
        void should_find_the_entry(FooEnum expected, String param) {
            FooEnum actual = getEnum(FooEnum.class, e -> param.equals(e.getText()));
            assertThat(actual)
                .isEqualTo(expected);
        }

        @Test
        void should_find_the_first_duplicate() {
            FooEnum actual = getEnum(FooEnum.class, e -> "duplicate".equals(e.getText()));
            assertThat(actual)
                .isEqualTo(FooEnum.DUPLICATE1);
        }
    }

    @Nested
    class MapValueTest {
        @Data
        static class Foo {
            private final String value = "hello";
        }

        @Nested
        class NullableVariantTest {
            @Test
            void should_return_resolver_if_value_present() {
                String actual = Util.mapValue(new Foo(), Foo::getValue);

                assertThat(actual)
                    .isEqualTo("hello");
            }

            @Test
            void should_return_resolver_if_value_missing() {
                @SuppressWarnings("ConstantValue")
                var actual = Util.mapValue(null, Foo::getValue);

                assertThat(actual)
                    .isNull();
            }
        }
    }

    @Nested
    class IsInTest {
        @Test
        void shouldFailOnNullAndEmptyHaystack() {
            var actual = isIn(null);

            assertThat(actual)
                .isFalse();
        }

        @Test
        void shouldAcceptOnNullAndHaystackContainingNull() {
            var actual = isIn(null, (String) null);

            assertThat(actual)
                .isTrue();
        }

        @Test
        void shouldFailOnEmptyHaystack() {
            var actual = isIn("asdf");

            assertThat(actual)
                .isFalse();
        }

        @Test
        void shouldFailOnHaystackNotContainingNeedle_1() {
            var actual = isIn("asdf", "fdas");

            assertThat(actual)
                .isFalse();
        }

        @Test
        void shouldFailOnHaystackNotContainingNeedle_2() {
            var actual = isIn("asdf", "foobar", "fdas", "xyzzy");

            assertThat(actual)
                .isFalse();
        }

        @Test
        void shouldAcceptOnHaystackContainingJustNeedle() {
            var actual = isIn("asdf", "asdf");

            assertThat(actual)
                .isTrue();
        }

        @Test
        void shouldAcceptOnHaystackContainingNeedle() {
            var actual = isIn("asdf", "fdas", "asdf", "xyzzy");

            assertThat(actual)
                .isTrue();
        }
    }

    @Nested
    class ParseURLTest {
        @Test
        void accepts_a_legal_URL() {
            var actual = Util.parseURL("https://localhost:1234/asdf/fdsa?foo=bar");

            assertThat(actual)
                .hasProtocol("https")
                .hasHost("localhost")
                .hasPort(1234)
                .hasPath("/asdf/fdsa")
                .hasParameter("foo", "bar");
        }

        @Test
        void throws_on_invalid_URL() {
            var ex = assertThrows(
                IllegalArgumentException.class,
                () -> Util.parseURL("schnitzel")

            );

            assertThat(ex)
                .hasMessageContaining("Not a valid URL")
                .hasMessageContaining("schnitzel");
        }
    }

    @Nested
    class ParseURITest {
        @Test
        void accepts_a_legal_URI() {
            var actual = Util.parseURI("https://localhost:1234/asdf/fdsa?foo=bar");

            assertThat(actual)
                .hasHost("localhost")
                .hasPort(1234)
                .hasPath("/asdf/fdsa")
                .hasParameter("foo", "bar");
        }

        @Test
        void throws_on_invalid_URI() {
            var ex = assertThrows(
                IllegalArgumentException.class,
                () -> Util.parseURI(" ")
            );

            assertThat(ex)
                .hasMessageContaining("Illegal character");
        }
    }

    @Nested
    class ReadResource {
        @Test
        void reads_a_resources_from_the_classpath() {
            var path = UtilTest.class.getName()
                .replace('.', '/') + ".class";

            var bytes = Util.readResource(path);

            assertThat(bytes)
                .isNotEmpty();
        }

        @Test
        void throws_NPE_on_nonexisting_resource() {
            var ex = assertThrows(
                NullPointerException.class,
                () -> Util.readResource("resource-does-not-exist")
            );

            assertThat(ex)
                .hasMessageContaining("resource-does-not-exist");
        }
    }

    @Nested
    class ResourceURL {
        @Test
        void reads_a_resources_from_the_classpath() {
            var path = UtilTest.class.getName()
                .replace('.', '/') + ".class";

            var url = Util.resourceURL(path);

            assertThat(url)
                .isNotNull();
        }

        @Test
        void throws_NPE_on_nonexisting_resource() {
            var ex = assertThrows(
                NullPointerException.class,
                () -> Util.resourceURL("resource-does-not-exist")
            );

            assertThat(ex)
                .hasMessageContaining("resource-does-not-exist");
        }
    }

    @Nested
    class GetSilent {
        @Test
        void gets_the_value() {
            var value = Util.getSilent(() -> "value");

            assertThat(value)
                .isEqualTo("value");
        }

        @Test
        void returns_a_stub_if_the_getter_throws() {
            var value = Util.getSilent(() -> {
                throw new IllegalStateException("fail");
            });

            assertThat(value)
                .isEqualTo("<unknown>");
        }
    }
}
