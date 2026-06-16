package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record ProductId(String value) implements ValueObject {
    public ProductId {
        Objects.requireNonNull(value, "ProductId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be blank");
        }
    }

    public static ProductId create() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId valueOf(String value) {
        return new ProductId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
