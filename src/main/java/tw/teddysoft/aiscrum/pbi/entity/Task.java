package tw.teddysoft.aiscrum.pbi.entity;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Task {
    private final TaskId id;
    private final PbiId pbiId;
    private String name;
    private TaskState state;
    private Set<String> assigneeIds;
    private EstimatedHours estimatedHours;
    private RemainingHours remainingHours;
    private List<String> blockedBy;
    private String note;
    private String extension;

    public Task(TaskId id, PbiId pbiId, String name, EstimatedHours estimatedHours) {
        this(id, pbiId, name, TaskState.TODO, Set.of(), estimatedHours,
                RemainingHours.from(estimatedHours), List.of(), null, null);
    }

    protected Task(Task task) {
        this(task.id, task.pbiId, task.name, task.state, task.assigneeIds, task.estimatedHours,
                task.remainingHours, task.blockedBy, task.note, task.extension);
    }

    Task(TaskId id,
         PbiId pbiId,
         String name,
         TaskState state,
         Set<String> assigneeIds,
         EstimatedHours estimatedHours,
         RemainingHours remainingHours,
         List<String> blockedBy,
         String note,
         String extension) {
        this.id = Objects.requireNonNull(id, "TaskId cannot be null");
        this.pbiId = Objects.requireNonNull(pbiId, "PbiId cannot be null");
        this.name = validateName(name);
        this.state = Objects.requireNonNull(state, "TaskState cannot be null");
        this.assigneeIds = Set.copyOf(Objects.requireNonNull(assigneeIds, "assigneeIds cannot be null"));
        this.estimatedHours = estimatedHours;
        this.remainingHours = remainingHours;
        this.blockedBy = List.copyOf(Objects.requireNonNull(blockedBy, "blockedBy cannot be null"));
        this.note = note;
        this.extension = extension;
    }

    public TaskId getId() {
        return id;
    }

    public PbiId getPbiId() {
        return pbiId;
    }

    public String getName() {
        return name;
    }

    public TaskState getState() {
        return state;
    }

    public Set<String> getAssigneeIds() {
        return Set.copyOf(assigneeIds);
    }

    public EstimatedHours getEstimatedHours() {
        return estimatedHours;
    }

    public RemainingHours getRemainingHours() {
        return remainingHours;
    }

    public List<String> getBlockedBy() {
        return List.copyOf(blockedBy);
    }

    public String getNote() {
        return note;
    }

    public String getExtension() {
        return extension;
    }

    void rename(String newName) {
        this.name = validateName(newName);
    }

    private static String validateName(String newName) {
        Objects.requireNonNull(newName, "Task name cannot be null");
        if (newName.isBlank()) {
            throw new IllegalArgumentException("Task name cannot be blank");
        }
        return newName;
    }

    void changeState(TaskState newState) {
        this.state = Objects.requireNonNull(newState, "TaskState cannot be null");
    }

    void assignTo(Set<String> newAssigneeIds) {
        this.assigneeIds = Set.copyOf(Objects.requireNonNull(newAssigneeIds, "assigneeIds cannot be null"));
    }

    void estimate(EstimatedHours newEstimatedHours) {
        this.estimatedHours = newEstimatedHours;
        this.remainingHours = RemainingHours.from(newEstimatedHours);
    }

    void setRemainingHours(RemainingHours newRemainingHours) {
        this.remainingHours = newRemainingHours;
    }

    void blockBy(List<String> newBlockedBy) {
        this.blockedBy = List.copyOf(Objects.requireNonNull(newBlockedBy, "blockedBy cannot be null"));
    }

    void changeNote(String newNote) {
        this.note = newNote;
    }

    void changeExtension(String newExtension) {
        this.extension = newExtension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
