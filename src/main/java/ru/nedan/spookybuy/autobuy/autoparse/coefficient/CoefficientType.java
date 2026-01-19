package ru.nedan.spookybuy.autobuy.autoparse.coefficient;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BiPredicate;

@Getter
@AllArgsConstructor
public enum CoefficientType {
    EQUALS(BigDecimal::equals),
    OVER_OR_EQUALS((itemPrice, coefficientPrice) -> itemPrice.intValue() >= coefficientPrice.intValue()),
    OVER((itemPrice, coefficientPrice) -> itemPrice.intValue() > coefficientPrice.intValue()),
    LOWER_OR_EQUALS((itemPrice, coefficientPrice) -> itemPrice.intValue() <= coefficientPrice.intValue()),
    LOWER((itemPrice, coefficientPrice) -> itemPrice.intValue() < coefficientPrice.intValue());

    private final BiPredicate<BigDecimal, BigDecimal> comparator;

    public boolean test(BigDecimal itemPrice, BigDecimal coefficientPrice) {
        return comparator.test(itemPrice, coefficientPrice);
    }

    public static CoefficientType next(CoefficientType current) {
        int nextOrdinal = current.ordinal() + 1;
        CoefficientType[] values = values();
        return nextOrdinal < values.length ? values[nextOrdinal] : null;
    }

    public static CoefficientType previous(CoefficientType current) {
        int prevOrdinal = current.ordinal() - 1;
        return prevOrdinal >= 0 ? values()[prevOrdinal] : null;
    }

    public static CoefficientType fromString(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static String toShortString(CoefficientType type) {
        switch (type) {
            case EQUALS: return "==";
            case OVER_OR_EQUALS: return ">=";
            case OVER: return ">";
            case LOWER_OR_EQUALS: return "<=";
            case LOWER: return "<";
            default: return "";
        }
    }
}
