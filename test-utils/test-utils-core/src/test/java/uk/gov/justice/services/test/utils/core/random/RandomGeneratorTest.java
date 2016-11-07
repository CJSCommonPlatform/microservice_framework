package uk.gov.justice.services.test.utils.core.random;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static java.time.LocalDate.now;
import static java.util.EnumSet.allOf;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.Times.times;
import static uk.gov.justice.services.test.utils.core.helper.TypeCheck.typeCheck;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class RandomGeneratorTest {

    private static final int NUMBER_OF_TIMES = 10000;
    private static final String BIG_DECIMAL_PATTERN = "(-)?(0|(?!0)\\d{1,10})\\.\\d{2}";
    private static final String PERCENTAGE_PATTERN = "((0|(?!0)\\d{1,2})\\.\\d{2})|100.00";
    private static final String DOUBLE_WITH_OPTIONAL_FRACTION_PATTERN =
                    "(-)?(0|(?!0)\\d{1,309})(\\.\\d{1,2})?";
    private static final String NI_NUMBER_PATTERN =
                    "(?!BG)(?!GB)(?!NK)(?!KN)(?!TN)(?!NT)(?!ZZ)(?:[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TW-Z])(?:\\s*\\d\\s*){6}([A-D]|\\s)";

    @Test
    public void shouldGenerateRandomBigDecimal() {
        // given
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.BIG_DECIMAL;

        // when & then
        typeCheck(bigDecimalGenerator, s -> s.toPlainString().matches(BIG_DECIMAL_PATTERN))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomBoolean() {
        // given
        final Generator<Boolean> booleanGenerator = RandomGenerator.BOOLEAN;

        // when & then
        typeCheck(booleanGenerator, s -> ImmutableList.of(TRUE, FALSE).contains(s))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomDouble() {
        // given
        final Generator<Double> doubleGenerator = RandomGenerator.DOUBLE;

        // when & then
        typeCheck(doubleGenerator,
                        s -> new DecimalFormat("#.###").format(s)
                                        .matches(DOUBLE_WITH_OPTIONAL_FRACTION_PATTERN))
                                                        .verify(times(NUMBER_OF_TIMES));
    }


    @Test
    public void shouldGenerateRandomString() {
        // given
        final Generator<String> stringGenerator = RandomGenerator.STRING;

        // when
        typeCheck(stringGenerator, s -> !stringGenerator.next().equals(stringGenerator.next()))
                        .verify(times(NUMBER_OF_TIMES));
    }


    @Test
    public void shouldGenerateRandomPercentage() {
        // given
        final Generator<BigDecimal> percentageGenerator = RandomGenerator.PERCENTAGE;

        // when & then
        typeCheck(percentageGenerator, s -> s.toPlainString().matches(PERCENTAGE_PATTERN))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomNiNumber() {
        // given
        final Generator<String> niNumberGenerator = RandomGenerator.NI_NUMBER;

        // when
        typeCheck(niNumberGenerator, s -> s.matches(NI_NUMBER_PATTERN))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomPostCode() {
        // given
        final Generator<String> postCodeGenerator = RandomGenerator.POST_CODE;

        // when
        typeCheck(postCodeGenerator,
                        s -> !postCodeGenerator.next().equals(postCodeGenerator.next()))
                                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomUri() {
        // given
        final Generator<URI> uriGenerator = RandomGenerator.URI;

        // when
        typeCheck(uriGenerator, s -> !uriGenerator.next().equals(uriGenerator.next()))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomUuid() {
        // given
        final Generator<UUID> uuidGenerator = RandomGenerator.UUID;

        // when
        typeCheck(uuidGenerator, s -> !uuidGenerator.next().equals(uuidGenerator.next()))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomForwardDate() {
        // given
        final LocalDateTime startDate = now().atStartOfDay();
        final LocalDateTime endDate = now().plus(Period.ofYears(5)).atStartOfDay();
        final Generator<LocalDate> futureLocalDateGenerator = RandomGenerator.FUTURE_LOCAL_DATE;

        // when & then
        typeCheck(futureLocalDateGenerator,
                        s -> !(s.isBefore(startDate.toLocalDate())
                                        || s.isAfter(endDate.toLocalDate())))
                                                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateRandomBackwardDate() {
        // given
        final LocalDateTime startDate = now().atStartOfDay();
        final LocalDateTime endDate = now().minus(Period.ofYears(5)).atStartOfDay();
        final Generator<LocalDate> pastLocalDateGenerator = RandomGenerator.PAST_LOCAL_DATE;

        // when & then
        typeCheck(pastLocalDateGenerator,
                        s -> !(s.isBefore(endDate.toLocalDate())
                                        || s.isAfter(startDate.toLocalDate())))
                                                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateStringOfLength() {
        final Generator<String> stringOfLength5Generator = RandomGenerator.string(5);
        typeCheck(stringOfLength5Generator, s -> (!(stringOfLength5Generator.next().length() < 5)))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateValuesFromIterable() {
        // given
        final List<Integer> integers = newArrayList(1, 2, 3, 4, 5);
        // and
        final Generator<Integer> valuesGenerator = RandomGenerator.values(integers);

        // when
        typeCheck(valuesGenerator, s -> ((integers.contains(valuesGenerator.next()))))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateIntegerEqualToOrLessThanMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<Integer> integerWithMaxGenerator = RandomGenerator.integer(max);

        // when
        typeCheck(integerWithMaxGenerator, s -> ((integerWithMaxGenerator.next() <= max)))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalLessThanOrEqualToMax() {
        // given
        final Integer max = 100;
        // and
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.bigDecimal(max);

        // when
        typeCheck(bigDecimalGenerator, s -> (s.compareTo(new BigDecimal(max)) != 1))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalLessThanOrEqualToMaxAndDecimalPlaces() {
        // given
        final Integer max = 100;
        // and
        final Integer scale = 2;
        // and
        final Generator<BigDecimal> bigDecimalWithMaxAndDecimalGenerator =
                        RandomGenerator.bigDecimal(max, scale);

        // when
        typeCheck(bigDecimalWithMaxAndDecimalGenerator, s -> ((s
                        .compareTo(new BigDecimal(max).setScale(scale, ROUND_HALF_EVEN)) != 1)))
                                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateBigDecimalGreaterThanOrEqualToMinAndLessThanOrEqualToMax() {
        // given
        final Integer min = -100;
        final Integer max = 100;

        // when
        final Generator<BigDecimal> bigDecimalGenerator = RandomGenerator.bigDecimal(min, max, 0);

        // then
        typeCheck(bigDecimalGenerator,
                        s -> (s.compareTo(new BigDecimal(min)) != -1
                                        && s.compareTo(new BigDecimal(max)) != 1))
                                                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateDoubleGreaterThanOrEqualToMinAndLessThanOrEqualToMaxForBoundsInLong() {
        // given
        final Long min = -100L;
        final Long max = 100L;
        final Integer scale = 2;

        // when
        final Generator<Double> doubleGenerator = RandomGenerator.doubleValue(min, max, scale);

        // when
        typeCheck(doubleGenerator, s -> s >= min && s <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldGenerateDoubleGreaterThanOrEqualToMinAndLessThanOrEqualToMaxForBoundsInDouble() {
        // given
        final Double min = -100.55;
        final Double max = 100.55;
        final Integer scale = 2;

        // when
        final Generator<Double> doubleGenerator = RandomGenerator.doubleValue(min, max, scale);

        // when
        typeCheck(doubleGenerator, s -> s >= min && s <= max).verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldPickAnEnumFromAvailableElements() {
        // given
        final Generator<TimeUnit> enumGenerator = randomEnum(TimeUnit.class);

        // then
        typeCheck(enumGenerator, s -> allOf(TimeUnit.class).contains(enumGenerator.next()))
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldAlwaysPickSameElementFromAnEnumWithSingleElement() {
        // given
        final Generator<SingleEnum> enumGenerator = randomEnum(SingleEnum.class);

        // then
        typeCheck(enumGenerator, s -> enumGenerator.next().compareTo(enumGenerator.next()) == 0)
                        .verify(times(NUMBER_OF_TIMES));
    }

    @Test
    public void shouldReturnAnInstanceEmailAddressGenerator() {
        assertNotNull(RandomGenerator.EMAIL_ADDRESS);
    }

    @Test
    public void shouldReturnAnInstanceIntegerGenerator() {
        assertNotNull(RandomGenerator.INTEGER);
    }

    @Test
    public void shouldReturnAnInstanceIntegerGeneratorWithMaxValue() {
        assertNotNull(RandomGenerator.integer(200));
    }

    @Test
    public void shouldReturnAnInstanceIntegerGeneratorWithMinMaxValue() {
        assertNotNull(RandomGenerator.integer(-100, 100));
    }

    @Test
    public void shouldReturnAnInstanceLongGenerator() {
        assertNotNull(RandomGenerator.LONG);
    }

    @Test
    public void shouldReturnAnInstancePostCodeGenerator() {
        assertNotNull(RandomGenerator.POST_CODE);
    }

    enum SingleEnum {
        SINGLE_VALUE
    }
}
