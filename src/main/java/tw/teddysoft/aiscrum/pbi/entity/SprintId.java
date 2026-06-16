package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record SprintId(String value) implements ValueObject {
    public SprintId {
        Objects.requireNonNull(value, "SprintId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SprintId cannot be blank");
        }
    }

    public static SprintId create() {
        return new SprintId(UUID.randomUUID().toString());
    }

    public static SprintId valueOf(String value) {
        return new SprintId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
