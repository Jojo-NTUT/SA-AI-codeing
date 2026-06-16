package tw.teddysoft.aiscrum.pbi.entity;

import tw.teddysoft.aiscrum.common.entity.DateProvider;
import tw.teddysoft.ezddd.entity.EsAggregateRoot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static tw.teddysoft.ucontract.Contract.*;

public class ProductBacklogItem extends EsAggregateRoot<PbiId, ProductBacklogItemEvents> {
    public static final String CATEGORY = "ProductBacklogItem";
    private ProductId productId;
    private PbiId id;
    private PbiState state;
    private Map<TaskId, Task> tasks;

    public ProductBacklogItem(List<ProductBacklogItemEvents> domainEvents) {
        super(domainEvents);
    }

    public ProductBacklogItem(ProductId productId, PbiId pbiId, String creatorId) {
        super();
        requireNotNull("Product id", productId);
        requireNotNull("PBI id", pbiId);
        requireNotNull("Creator id", creatorId);

        apply(new ProductBacklogItemEvents.ProductBacklogItemCreated(
                productId,
                pbiId,
                creatorId,
                Map.of(),
                UUID.randomUUID(),
                DateProvider.now()
        ));

        ensure("Product id is set", () -> Objects.equals(this.productId, productId));
        ensure("PBI id is set", () -> Objects.equals(this.id, pbiId));
        ensure("PBI starts in BACKLOGGED state", () -> this.state == PbiState.BACKLOGGED);
    }

    public void createTask(TaskId taskId, String name, EstimatedHours estimatedHours, String creatorId) {
        requireNotNull("Task id", taskId);
        requireNotNull("Task name", name);
        requireNotNull("Creator id", creatorId);
        require("Task id is unique", () -> !tasks.containsKey(taskId));

        RemainingHours remainingHours = RemainingHours.from(estimatedHours);
        PbiState previousState = this.state;

        apply(new ProductBacklogItemEvents.TaskCreated(
                productId,
                id,
                taskId,
                name,
                estimatedHours,
                remainingHours,
                null,
                null,
                creatorId,
                Map.of(),
                UUID.randomUUID(),
                DateProvider.now()
        ));

        if (previousState == PbiState.DONE) {
            apply(new ProductBacklogItemEvents.PbiWorkRegressed(
                    productId,
                    null,
                    id,
                    previousState,
                    PbiState.IN_PROGRESS,
                    creatorId,
                    "New task created on completed PBI",
                    Map.of(),
                    UUID.randomUUID(),
                    DateProvider.now()
            ));
        }

        ensure("Task exists", () -> tasks.containsKey(taskId));
        ensure("Task is exposed as read-only", () -> getTask(taskId) instanceof ReadOnlyTask);
        ensure("Task initial state is TODO", () -> getTask(taskId).getState() == TaskState.TODO);
    }

    public ProductId getProductId() {
        return productId;
    }

    @Override
    public PbiId getId() {
        return id;
    }

    public PbiState getState() {
        return state;
    }

    public boolean hasTask(TaskId taskId) {
        return tasks.containsKey(taskId);
    }

    public Task getTask(TaskId taskId) {
        Task task = tasks.get(taskId);
        return task == null ? null : new ReadOnlyTask(task);
    }

    public List<Task> getTasks() {
        return tasks.values().stream()
                .map(ReadOnlyTask::new)
                .map(Task.class::cast)
                .toList();
    }

    @Override
    protected void when(ProductBacklogItemEvents event) {
        if (event instanceof ProductBacklogItemEvents.ProductBacklogItemCreated productBacklogItemCreated) {
            when(productBacklogItemCreated);
        } else if (event instanceof ProductBacklogItemEvents.TaskCreated taskCreated) {
            when(taskCreated);
        } else if (event instanceof ProductBacklogItemEvents.PbiWorkRegressed pbiWorkRegressed) {
            when(pbiWorkRegressed);
        }
    }

    private void when(ProductBacklogItemEvents.ProductBacklogItemCreated event) {
        this.productId = event.productId();
        this.id = event.pbiId();
        this.state = PbiState.BACKLOGGED;
        this.tasks = new HashMap<>();
    }

    private void when(ProductBacklogItemEvents.TaskCreated event) {
        tasks.put(event.taskId(), new Task(event.taskId(), event.pbiId(), event.name(), event.estimatedHours()));
    }

    private void when(ProductBacklogItemEvents.PbiWorkRegressed event) {
        this.state = event.newState();
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }
}
