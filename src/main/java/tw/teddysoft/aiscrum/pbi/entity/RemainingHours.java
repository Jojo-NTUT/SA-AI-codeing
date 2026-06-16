package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

public record RemainingHours(BigDecimal value) implements ValueObject {
    public RemainingHours {
        Objects.requireNonNull(value, "RemainingHours cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("RemainingHours cannot be negative");
        }
    }

    public static RemainingHours valueOf(String value) {
        return value == null ? null : new RemainingHours(new BigDecimal(value));
    }

    public static RemainingHours from(EstimatedHours estimatedHours) {
        return estimatedHours == null ? null : new RemainingHours(estimatedHours.value());
    }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString();
    }
}
