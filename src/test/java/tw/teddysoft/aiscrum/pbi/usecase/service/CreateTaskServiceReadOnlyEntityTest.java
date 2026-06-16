package tw.teddysoft.aiscrum.pbi.usecase.service;

import org.junit.jupiter.api.Test;
import tw.teddysoft.aiscrum.pbi.entity.PbiId;
import tw.teddysoft.aiscrum.pbi.entity.ProductBacklogItem;
import tw.teddysoft.aiscrum.pbi.entity.ReadOnlyTask;
import tw.teddysoft.aiscrum.pbi.entity.Task;
import tw.teddysoft.aiscrum.pbi.entity.TaskId;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.CreateTaskUseCase.CreateTaskInput;
import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;
import tw.teddysoft.ezddd.usecase.port.in.interactor.ExitCode;
import tw.teddysoft.ezddd.usecase.port.out.repository.Repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateTaskServiceReadOnlyEntityTest {
    //這個測試對應 .dev/specs/pbi/usecase/create-task.json
    //重點：command use case 可以透過 aggregate 建立 task，但 use case 執行完後，aggregate 對外暴露的 task 仍是 read-only
    @Test
    void commandUseCaseCreatesTaskThroughAggregateAndSavedAggregateExposesReadOnlyTask() throws Exception {
        InMemoryProductBacklogItemRepository repository = new InMemoryProductBacklogItemRepository();
        CreateTaskService service = new CreateTaskService(repository);

        CreateTaskInput input = CreateTaskInput.create();
        input.productId = "product-123";
        input.pbiId = "pbi-456";
        input.taskId = "task-001";
        input.name = "Design authentication API";
        input.estimatedHours = "5";
        input.creatorId = "user-789";

        CqrsOutput<?> output = service.execute(input);

        ProductBacklogItem savedPbi = repository.findById(PbiId.valueOf("pbi-456")).orElseThrow();
        Task exposedTask = savedPbi.getTask(TaskId.valueOf("task-001"));
        assertEquals(ExitCode.SUCCESS, output.getExitCode());//從 use case 回傳的 output 裡取得執行狀態
        assertInstanceOf(ReadOnlyTask.class, exposedTask);
        assertRenameRejectedEvenIfCallerForcesAccess(exposedTask);
    }

    private void assertRenameRejectedEvenIfCallerForcesAccess(Task exposedTask) throws Exception {
        Method rename = Task.class.getDeclaredMethod("rename", String.class);
        rename.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> rename.invoke(exposedTask, "Bypass aggregate"));
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    //建立一個假的 in-memory repository
    private static class InMemoryProductBacklogItemRepository implements Repository<ProductBacklogItem, PbiId> {
        private final Map<PbiId, ProductBacklogItem> data = new HashMap<>();

        @Override
        public Optional<ProductBacklogItem> findById(PbiId id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public void save(ProductBacklogItem aggregate) {
            data.put(aggregate.getId(), aggregate);
        }

        @Override
        public void delete(ProductBacklogItem aggregate) {
            data.remove(aggregate.getId());
        }
    }
}
