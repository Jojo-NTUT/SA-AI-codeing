package tw.teddysoft.aiscrum.pbi.entity;

import java.util.List;
import java.util.Set;

public class ReadOnlyTask extends Task {
    public ReadOnlyTask(Task task) {
        super(task);
    }

    @Override
    void rename(String newName) {
        throw readOnlyViolation();
    }

    @Override
    void changeState(TaskState newState) {
        throw readOnlyViolation();
    }

    @Override
    void assignTo(Set<String> newAssigneeIds) {
        throw readOnlyViolation();
    }

    @Override
    void estimate(EstimatedHours newEstimatedHours) {
        throw readOnlyViolation();
    }

    @Override
    void setRemainingHours(RemainingHours newRemainingHours) {
        throw readOnlyViolation();
    }

    @Override
    void blockBy(List<String> newBlockedBy) {
        throw readOnlyViolation();
    }

    @Override
    void changeNote(String newNote) {
        throw readOnlyViolation();
    }

    @Override
    void changeExtension(String newExtension) {
        throw readOnlyViolation();
    }

    private UnsupportedOperationException readOnlyViolation() {
        return new UnsupportedOperationException("Read-only Task cannot be modified");
    }
}
