package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record PbiId(String value) implements ValueObject {
    public PbiId {
        Objects.requireNonNull(value, "PbiId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PbiId cannot be blank");
        }
    }

    public static PbiId create() {
        return new PbiId(UUID.randomUUID().toString());
    }

    public static PbiId valueOf(String value) {
        return new PbiId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
