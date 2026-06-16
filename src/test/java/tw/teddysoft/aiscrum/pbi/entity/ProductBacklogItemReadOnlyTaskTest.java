package tw.teddysoft.aiscrum.pbi.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductBacklogItemReadOnlyTaskTest {
    @Test
    void taskCanBeCreatedThroughAggregateButExposedTaskReferenceIsReadOnly() {

        //建立 ProductBacklogItem aggregate
        ProductBacklogItem pbi = newProductBacklogItem();
        TaskId taskId = TaskId.valueOf("task-001");

        //透過 pbi.createTask(...) 建立 task，「修改 entity 必須經過 aggregate」
        pbi.createTask(taskId, "Design authentication API", EstimatedHours.valueOf("5"), "user-789");

        //透過 pbi.getTask(taskId) 把 task 拿出來
        Task exposedTask = pbi.getTask(taskId);

        //確認拿到的 Task 實際型別是 ReadOnlyTask
        assertInstanceOf(ReadOnlyTask.class, exposedTask);
        //確認可以讀資料，例如 getName()、getState()
        assertEquals("Design authentication API", exposedTask.getName());
        assertEquals(TaskState.TODO, exposedTask.getState());

        //確認所有會修改 task 狀態的方法都會丟 UnsupportedOperationException
        assertAllMutationsRejected(exposedTask);
    }

    @Test
    void aggregateTaskCollectionExposesOnlyReadOnlyTasksAndCannotBeModifiedDirectly() {

        //透過 aggregate 建立兩個 task
        ProductBacklogItem pbi = newProductBacklogItem();
        pbi.createTask(TaskId.valueOf("task-001"), "Design authentication API", EstimatedHours.valueOf("5"), "user-789");
        pbi.createTask(TaskId.valueOf("task-002"), "Implement login endpoint", EstimatedHours.valueOf("8"), "user-789");

        List<Task> exposedTasks = pbi.getTasks();

        assertEquals(2, exposedTasks.size());
        exposedTasks.forEach(task -> {
            assertInstanceOf(ReadOnlyTask.class, task); //確認 list 裡每個 task 都是 ReadOnlyTask
            assertAllMutationsRejected(task); //確認每個 task 的 mutation method 都被擋
        });
        //確認回傳的 list 本身也不能 add()
        assertThrows(UnsupportedOperationException.class, () ->
                exposedTasks.add(new Task(TaskId.valueOf("task-003"), pbi.getId(), "Bypass aggregate", null)));
    }

    private ProductBacklogItem newProductBacklogItem() {
        return new ProductBacklogItem(
                ProductId.valueOf("product-123"),
                PbiId.valueOf("pbi-456"),
                "user-789"
        );
    }

    private void assertAllMutationsRejected(Task task) {
        //集中驗證 mutation 都不能從 exposed task 上呼叫
        assertThrows(UnsupportedOperationException.class, () -> task.rename("Mutate through exposed reference"));
        assertThrows(UnsupportedOperationException.class, () -> task.changeState(TaskState.DONE));
        assertThrows(UnsupportedOperationException.class, () -> task.assignTo(Set.of("user-001")));
        assertThrows(UnsupportedOperationException.class, () -> task.estimate(EstimatedHours.valueOf("13")));
        assertThrows(UnsupportedOperationException.class, () -> task.setRemainingHours(RemainingHours.valueOf("3")));
        assertThrows(UnsupportedOperationException.class, () -> task.blockBy(List.of("task-000")));
        assertThrows(UnsupportedOperationException.class, () -> task.changeNote("Changed outside aggregate"));
        assertThrows(UnsupportedOperationException.class, () -> task.changeExtension("{\"x\":\"y\"}"));
    }
}
