package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public record EstimatedHours(BigDecimal value) implements ValueObject {
    public EstimatedHours {
        Objects.requireNonNull(value, "EstimatedHours cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("EstimatedHours cannot be negative");
        }
    }

    public static EstimatedHours valueOf(String value) {
        return value == null ? null : new EstimatedHours(new BigDecimal(value));
    }

    public static EstimatedHours valueOf(BigDecimal value) {
        return value == null ? null : new EstimatedHours(value);
    }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString();
    }
}
