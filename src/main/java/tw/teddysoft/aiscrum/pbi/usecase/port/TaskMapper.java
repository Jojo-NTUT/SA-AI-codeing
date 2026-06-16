package tw.teddysoft.aiscrum.pbi.usecase.port;

import tw.teddysoft.aiscrum.pbi.entity.Task;

import java.util.List;

public final class TaskMapper {
    private TaskMapper() {
    }

    public static TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskDto(
                task.getId().value(),
                task.getPbiId().value(),
                task.getName(),
                task.getState().name(),
                task.getAssigneeIds(),
                EstimatedHoursMapper.toDto(task.getEstimatedHours()),
                RemainingHoursMapper.toDto(task.getRemainingHours()),
                task.getBlockedBy(),
                task.getNote(),
                task.getExtension()
        );
    }

    public static List<TaskDto> toDtoList(List<Task> tasks) {
        return tasks.stream()
                .map(TaskMapper::toDto)
                .toList();
    }
}
