package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.ezddd.entity.DomainEventTypeMapper;
import tw.teddysoft.ezddd.entity.InternalDomainEvent;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public sealed interface ProductBacklogItemEvents extends InternalDomainEvent {
    String MAPPING_TYPE_PREFIX = "ProductBacklogItemEvents$";

    PbiId pbiId();

    @Override
    default String source() {
        return pbiId().value();
    }

    record ProductBacklogItemCreated(
            ProductId productId,
            PbiId pbiId,
            String creatorId,
            Map<String, String> metadata,
            UUID id,
            Instant occurredOn
    ) implements ProductBacklogItemEvents, InternalDomainEvent.ConstructionEvent {
        public ProductBacklogItemCreated {
            Objects.requireNonNull(productId, "ProductId cannot be null");
            Objects.requireNonNull(pbiId, "PbiId cannot be null");
            Objects.requireNonNull(creatorId, "creatorId cannot be null");
            metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata cannot be null"));
            Objects.requireNonNull(id, "event id cannot be null");
            Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
        }
    }

    record TaskCreated(
            ProductId productId,
            PbiId pbiId,
            TaskId taskId,
            String name,
            EstimatedHours estimatedHours,
            RemainingHours remainingHours,
            String note,
            String extension,
            String creatorId,
            Map<String, String> metadata,
            UUID id,
            Instant occurredOn
    ) implements ProductBacklogItemEvents {
        public TaskCreated {
            Objects.requireNonNull(productId, "ProductId cannot be null");
            Objects.requireNonNull(pbiId, "PbiId cannot be null");
            Objects.requireNonNull(taskId, "TaskId cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(creatorId, "creatorId cannot be null");
            metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata cannot be null"));
            Objects.requireNonNull(id, "event id cannot be null");
            Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
        }
    }

    record PbiWorkRegressed(
            ProductId productId,
            SprintId sprintId,
            PbiId pbiId,
            PbiState previousState,
            PbiState newState,
            String regressedBy,
            String reason,
            Map<String, String> extension,
            UUID id,
            Instant occurredOn
    ) implements ProductBacklogItemEvents {
        public PbiWorkRegressed {
            Objects.requireNonNull(productId, "ProductId cannot be null");
            Objects.requireNonNull(pbiId, "PbiId cannot be null");
            Objects.requireNonNull(previousState, "previousState cannot be null");
            Objects.requireNonNull(newState, "newState cannot be null");
            Objects.requireNonNull(regressedBy, "regressedBy cannot be null");
            Objects.requireNonNull(reason, "reason cannot be null");
            extension = extension == null ? Map.of() : Map.copyOf(extension);
            Objects.requireNonNull(id, "event id cannot be null");
            Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
        }

        @Override
        public Map<String, String> metadata() {
            return extension;
        }
    }

    static DomainEventTypeMapper mapper() {
        DomainEventTypeMapper mapper = DomainEventTypeMapper.create();
        mapper.put(MAPPING_TYPE_PREFIX + "ProductBacklogItemCreated", ProductBacklogItemCreated.class);
        mapper.put(MAPPING_TYPE_PREFIX + "TaskCreated", TaskCreated.class);
        mapper.put(MAPPING_TYPE_PREFIX + "PbiWorkRegressed", PbiWorkRegressed.class);
        return mapper;
    }
}
