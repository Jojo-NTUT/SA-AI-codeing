package tw.teddysoft.aiscrum.pbi.usecase.service;

import org.junit.jupiter.api.Test;
import tw.teddysoft.aiscrum.pbi.entity.EstimatedHours;
import tw.teddysoft.aiscrum.pbi.entity.PbiId;
import tw.teddysoft.aiscrum.pbi.entity.ProductBacklogItem;
import tw.teddysoft.aiscrum.pbi.entity.ProductId;
import tw.teddysoft.aiscrum.pbi.entity.ReadOnlyTask;
import tw.teddysoft.aiscrum.pbi.entity.Task;
import tw.teddysoft.aiscrum.pbi.entity.TaskId;
import tw.teddysoft.aiscrum.pbi.usecase.port.TaskMapper;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.GetTasksByProductBacklogItemUseCase.GetTasksByProductBacklogItemInput;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.GetTasksByProductBacklogItemUseCase.GetTasksByProductBacklogItemOutput;
import tw.teddysoft.aiscrum.pbi.usecase.port.out.projection.TasksDtoProjection;
import tw.teddysoft.ezddd.usecase.port.in.interactor.ExitCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class GetTasksByProductBacklogItemServiceTest {
    //這個測試對應 .dev/specs/pbi/usecase/get-tasks-by-pbi.json
    //重點：query use case 可以讀 task 並轉 DTO，但讀取來源仍然經過 aggregate accessor，所以拿到的是 read-only task，不是 mutable task。
    @Test
    void queryUseCaseReadsTasksThroughAggregateAccessorAndMapsReadOnlyTasksToDto() {
        ProductBacklogItem pbi = new ProductBacklogItem(
                ProductId.valueOf("product-123"),
                PbiId.valueOf("pbi-456"),
                "user-789"
        );
        pbi.createTask(TaskId.valueOf("task-001"), "Design authentication API", EstimatedHours.valueOf("5"), "user-789");

        TasksDtoProjection projection = input -> {
            List<Task> tasks = pbi.getTasks();
            tasks.forEach(task -> assertInstanceOf(ReadOnlyTask.class, task));
            return TaskMapper.toDtoList(tasks);
        };
        GetTasksByProductBacklogItemService service = new GetTasksByProductBacklogItemService(projection);

        GetTasksByProductBacklogItemInput input = GetTasksByProductBacklogItemInput.create();
        input.pbiId = "pbi-456";
        GetTasksByProductBacklogItemOutput output = service.execute(input);

        assertEquals(ExitCode.SUCCESS, output.getExitCode());//從 use case 回傳的 output 裡取得執行狀態
        assertEquals(1, output.getTasks().size());
        assertEquals("task-001", output.getTasks().get(0).id());
        assertEquals("Design authentication API", output.getTasks().get(0).name());
        assertEquals("TODO", output.getTasks().get(0).state());
    }
}
