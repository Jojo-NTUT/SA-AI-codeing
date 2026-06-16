package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.ValueObject;

import java.util.Objects;
import java.util.UUID;

public record TaskId(String value) implements ValueObject {
    public TaskId {
        Objects.requireNonNull(value, "TaskId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TaskId cannot be blank");
        }
    }

    public static TaskId create() {
        return new TaskId(UUID.randomUUID().toString());
    }

    public static TaskId valueOf(String value) {
        return new TaskId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
