package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.FixedScaleScaler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static java.math.RoundingMode.HALF_UP;
import static org.assertj.core.api.Assertions.assertThat;

class MathUtilTest {
    static class Fixture extends MathUtil<Fixture> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private static final Configuration<Fixture> CONFIG =
            ConfigurationFactory.create(19, HALF_UP, new FixedScaleScaler(2)).withFactory(Fixture::new);

        private Fixture(BigDecimal value, Configuration<Fixture> configuration) {
            super(
                value, configuration);
        }

        public static Fixture valueOf(double value) {
            return new Fixture(BigDecimal.valueOf(value), CONFIG);
        }
    }

    @Nested
    class IsEven {
        @ParameterizedTest
        @ValueSource(ints = { 0, 2, 4, 6, 8, -2, -4, -6, -8 })
        void for_even_numbers_it_returns_true(int value) {
            assertThat(MathUtil.isEven(value))
                .isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, 3, 5, 7, 9, -1, -3, -5, -7, -9 })
        void for_odd_numbers_it_returns_false(int value) {
            assertThat(MathUtil.isEven(value))
                .isFalse();
        }
    }

    @Nested
    class isPositive {
        @ParameterizedTest
        @ValueSource(doubles = { 0.1, 1.0, 99 })
        void for_positive_numbers_it_returns_true(double value) {
            assertThat(Fixture.valueOf(value).isPositive())
                .isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = { 0.0, -0.1, -1.0, -99 })
        void for_non_positive_numbers_it_returns_false(double value) {
            assertThat(Fixture.valueOf(value).isPositive())
                .isFalse();
        }
    }

    @Nested
    class isNegative {
        @ParameterizedTest
        @ValueSource(doubles = { -0.1, -1.0, -99 })
        void for_negative_numbers_it_returns_true(double value) {
            assertThat(Fixture.valueOf(value).isNegative())
                .isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = { 0.0, 0.1, 1.0, 99 })
        void for_non_negative_numbers_it_returns_false(double value) {
            assertThat(Fixture.valueOf(value).isNegative())
                .isFalse();
        }
    }

    @Nested
    class negate {
        @ParameterizedTest
        @ValueSource(doubles = { -99, -0.1, -1.0, 0, 0.1, 1.0, 99 })
        void it_negates_for_all_values(double value) {
            assertThat(Fixture.valueOf(value).negate())
                .isEqualByComparingTo(Fixture.valueOf(-value));
        }
    }

    @Nested
    class ToString {
        @ParameterizedTest
        @CsvSource({
            "0.1, MathUtilTest.Fixture[0.1]",
            "1.0, MathUtilTest.Fixture[1.0]",
            "99, MathUtilTest.Fixture[99.0]",
            "-0.1, MathUtilTest.Fixture[-0.1]",
            "-1.0, MathUtilTest.Fixture[-1.0]",
            "-99, MathUtilTest.Fixture[-99.0]"
        })
        void it_returns_toPlainString(double value, String expected) {
            assertThat(Fixture.valueOf(value)).hasToString(expected);
        }
    }

    @Nested
    class Sum {
        @Test
        void it_returns_sum_of_values() {
            var input = List.of(
                Fixture.valueOf(1),
                Fixture.valueOf(10),
                Fixture.valueOf(100)
            );

            var actual = MathUtil.sum(input, Function.identity(), Fixture.CONFIG);

            assertThat(actual)
                .isEqualByComparingTo(Fixture.valueOf(111));
        }
    }
}
