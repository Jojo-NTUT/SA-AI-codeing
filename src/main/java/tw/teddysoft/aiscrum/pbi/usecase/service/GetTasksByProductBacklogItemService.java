package tw.teddysoft.aiscrum.pbi.usecase.service;

import tw.teddysoft.aiscrum.pbi.usecase.port.TaskDto;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.GetTasksByProductBacklogItemUseCase;
import tw.teddysoft.aiscrum.pbi.usecase.port.out.projection.TasksDtoProjection;
import tw.teddysoft.ezddd.usecase.port.in.interactor.ExitCode;

import java.util.List;
import java.util.Objects;

import static tw.teddysoft.ucontract.Contract.requireNotNull;

public class GetTasksByProductBacklogItemService implements GetTasksByProductBacklogItemUseCase {
    private final TasksDtoProjection tasksDtoProjection;

    public GetTasksByProductBacklogItemService(TasksDtoProjection tasksDtoProjection) {
        this.tasksDtoProjection = Objects.requireNonNull(tasksDtoProjection);
    }

    @Override
    public GetTasksByProductBacklogItemOutput execute(GetTasksByProductBacklogItemInput input) {
        requireNotNull("Input", input);
        requireNotNull("PBI id", input.pbiId);

        List<TaskDto> tasks = tasksDtoProjection.query(TasksDtoProjection.TasksDtoProjectionInput.create(input.pbiId));
        return GetTasksByProductBacklogItemOutput.create()
                .setTasks(tasks)
                .setId(input.pbiId)
                .setExitCode(ExitCode.SUCCESS);
    }
}
