# Repo 來源

https://gitlab.com/TeddyChen/ai-coding-exercise-skills-uc

# AI Coding 作業報告

## 作業目標

將 **Read-only Entity Pattern** 加入 `ezddd-java` skill，讓 AI agent 在依照 use case spec 產生程式碼時，能正確保護 Aggregate 內部的 mutable child entity。

在 DDD 的 Aggregate 邊界中，外部物件不應該直接修改 Aggregate 內部的 Entity。若 Aggregate 必須對外回傳 child entity，回傳值應該只能讀取，不能讓外部繞過 Aggregate root 呼叫 mutation method。

因此此次作業希望透過 skill 規則補強，讓 agent 生成程式碼時能自動套用 Read-only Entity 的設計。

## 實作方向

先使用 prompt 向 AI 交代背景與需求，再請 AI 協助確認應修改的 skill 範圍。

核心需求如下：
- 新增 Read-only Entity Pattern 的定義與生成規則。
- 當 Aggregate 對外暴露 mutable child entity 時，必須回傳 `ReadOnly{Entity}`。
- Read-only entity 只允許 query/accessor method 正常執行。
- 所有會改變狀態的 command method 都必須覆寫並丟出 `UnsupportedOperationException`。
- 若 query method 回傳另一個 mutable entity，也必須包成對應的 read-only entity。
- 若 query method 回傳 entity collection，集合本身必須不可修改，集合內的 entity 也都必須轉成 read-only entity。

## Read-only Entity Pattern 設計

專案採用 **Special Case** 實作策略，也就是讓 `ReadOnly{Entity}` 繼承原本的 entity 類別。

例如原本的 child entity 是 `Task`，read-only 版本就命名為 `ReadOnlyTask`。

主要規則如下：

- 命名規則：`ReadOnly{Entity}.java`，例如 `ReadOnlyTask.java`。
- Aggregate 不直接回傳 mutable child entity。
- 單一 entity accessor 回傳 `new ReadOnly{Entity}(entity)`。
- Entity collection accessor 將每個 entity 包成 read-only 後，回傳不可修改集合。
- Mutation method 在 read-only entity 中一律丟出 `UnsupportedOperationException`。
- Primitive、enum、value object、ID 等只讀資料可以直接回傳。

## Skill 修改範圍

為了讓 agent 在後續產生 use case 程式碼時能穩定套用此 pattern，筆記中整理出的 skill 修改範圍包含：

- 新增 `.claude/skills/ezddd-java/references/patterns/domain/read-only-entity.md`
- 在 `.claude/skills/ezddd-java/references/patterns/domain/entity.md` 補充 child entity 若會被 Aggregate 對外回傳，必須搭配 Read-only Entity Pattern。
- 在 `.claude/skills/ezddd-java/references/patterns/domain/aggregate.md` 補充 Aggregate accessor 規則，避免直接回傳 mutable child entity。
- 在 `.claude/skills/ezddd-java/references/rules/domain-patterns.md` 補充 domain pattern rule。
- 在 `.claude/skills/ezddd-java/references/code-reviewer/checklist.md` 補充 code review 檢查項目。
- 在 `.claude/skills/ezddd-java/references/AUTHORITY-REGISTRY.yaml` 加入 `read_only_entity_exposure` 權威來源。
- 在 `.claude/skills/ezddd-java/SKILL.md` 的 Domain Layer pattern 表格加入 Read-only Entity Pattern。
- 在 UC Executor 相關文件中補充 Read-only Entity Pattern 的讀取與套用時機。
- 在 Gate 2.5 deterministic review rules 中加入 Aggregate accessor 是否直接暴露 mutable child entity 的檢查。

## 專案程式碼中的 Read-only Entity Pattern

`ProductBacklogItem` 可以管理 `Task`，外部也可以讀 `Task`，但外部不能拿到一個可以直接修改的 `Task`。

### Context

目標 Aggregate root 是 `ProductBacklogItem`，它內部有一個 `tasks` map，用來保存 child entity `Task`。

```java
public class ProductBacklogItem extends EsAggregateRoot<PbiId, ProductBacklogItemEvents> {
    private ProductId productId;
    private PbiId id;
    private PbiState state;
    private Map<TaskId, Task> tasks;
}
```

`Task` 本身不是只有一個資料物件而已，它有自己的 identity，也有許多會變動的欄位。

```java
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
}
```

### Problem

問題出在 `Task` 是 mutable child entity。如果 `ProductBacklogItem` 直接把內部的 `Task` 回傳出去，外部就可能繞過 Aggregate root，直接呼叫 `Task` 的 mutation method 修改狀態。

目前 `Task` 裡真的存在的 mutation method 如下：

```java
void rename(String newName) {
    this.name = validateName(newName);
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
```

除了上面這幾個，專案中還有 `setRemainingHours`、`blockBy`、`changeNote`、`changeExtension`。這些都會改變 `Task` 的內部狀態，所以外部不應該透過 Aggregate accessor 拿到它們後直接呼叫。

### Solution

這裡採用的是老師給的論文中提到的 Special Case 實作方式：建立一個 `ReadOnlyTask`，讓它繼承 `Task`。這樣外部拿到的型別仍然可以是 domain model 裡的 `Task`，但實際上是 `ReadOnlyTask`，而且修改行為可以因此被擋下來。

`ReadOnlyTask` 的 constructor 直接接收一個既有的 `Task`：

```java
public class ReadOnlyTask extends Task {
    public ReadOnlyTask(Task task) {
        super(task);
    }
}
```

而 `Task` 裡有對應的 copy constructor，會把既有 task 的欄位複製到新的 instance：

```java
protected Task(Task task) {
    this(task.id, task.pbiId, task.name, task.state, task.assigneeIds, task.estimatedHours,
            task.remainingHours, task.blockedBy, task.note, task.extension);
}
```

接著，`ReadOnlyTask` 把會改變狀態的 method 都覆寫掉。在 aggregate root 以外的物件呼叫的時候會直接丟出 `UnsupportedOperationException`，讓呼叫端馬上知道這個 task 不能這樣改。

```java
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

private UnsupportedOperationException readOnlyViolation() {
    return new UnsupportedOperationException("Read-only Task cannot be modified");
}
```

在完整的 `ReadOnlyTask` 中，`setRemainingHours`、`blockBy`、`changeNote`、`changeExtension` 也都用了同樣做法。

### Aggregate 怎麼對外回傳 Task

`ProductBacklogItem` 內部仍然保存真正可以修改的 `Task`。差別在於：只要要對外回傳，就先包成 `ReadOnlyTask`。

單一 task 的 accessor 是這樣：

```java
public Task getTask(TaskId taskId) {
    Task task = tasks.get(taskId);
    return task == null ? null : new ReadOnlyTask(task);
}
```

多筆 task 的 accessor 則是把每一個 task 都轉成 `ReadOnlyTask`：

```java
public List<Task> getTasks() {
    return tasks.values().stream()
            .map(ReadOnlyTask::new)
            .map(Task.class::cast)
            .toList();
}
```

這裡回傳型別仍然是 `Task` 或 `List<Task>`，但實際物件是 `ReadOnlyTask`。也就是說，外部仍然用 domain language 裡的 `Task` 互動，但不能繞過 `ProductBacklogItem` 修改 task。

### Use Case 中怎麼用

Command use case 的 `CreateTaskService` 並沒有直接 new `Task` 給外部修改，而是透過 Aggregate root 建立 task，然後儲存整個 `ProductBacklogItem`。

```java
TaskId taskId = TaskId.valueOf(input.taskId);
pbi.createTask(taskId, input.name, EstimatedHours.valueOf(input.estimatedHours), input.creatorId);
repository.save(pbi);
```

Query use case 的 `GetTasksByProductBacklogItemService` 對外回傳的是 `TaskDto`，不是 entity reference。

```java
List<TaskDto> tasks = tasksDtoProjection.query(TasksDtoProjection.TasksDtoProjectionInput.create(input.pbiId));
return GetTasksByProductBacklogItemOutput.create()
        .setTasks(tasks)
        .setId(input.pbiId)
        .setExitCode(ExitCode.SUCCESS);
```

### Tests

目前測試主要確認三件事：

第一，從 Aggregate 取出的單一 task 是 `ReadOnlyTask`，而且 mutation method 會被擋下來：

```java
Task exposedTask = pbi.getTask(taskId);

assertInstanceOf(ReadOnlyTask.class, exposedTask);
assertEquals("Design authentication API", exposedTask.getName());
assertEquals(TaskState.TODO, exposedTask.getState());

assertAllMutationsRejected(exposedTask);
```

第二，從 `getTasks()` 拿到的 list 裡，每一個 task 也都是 `ReadOnlyTask`，而且 list 本身不能直接 add 新 task。

```java
List<Task> exposedTasks = pbi.getTasks();

assertEquals(2, exposedTasks.size());
exposedTasks.forEach(task -> {
    assertInstanceOf(ReadOnlyTask.class, task);
    assertAllMutationsRejected(task);
});

assertThrows(UnsupportedOperationException.class, () ->
        exposedTasks.add(new Task(TaskId.valueOf("task-003"), pbi.getId(), "Bypass aggregate", null)));
```

第三，use case 執行後也符合這個規則。以 create-task use case 的測試來看，service 建立 task 並存回 repository 後，再從 Aggregate 取出的 task 仍然是 `ReadOnlyTask`。

```java
CqrsOutput<?> output = service.execute(input);

ProductBacklogItem savedPbi = repository.findById(PbiId.valueOf("pbi-456")).orElseThrow();
Task exposedTask = savedPbi.getTask(TaskId.valueOf("task-001"));

assertEquals(ExitCode.SUCCESS, output.getExitCode());
assertInstanceOf(ReadOnlyTask.class, exposedTask);
assertRenameRejectedEvenIfCallerForcesAccess(exposedTask);
```

## 遇到的問題

本次作業遇到的主要問題是：在不同對話中延續工作時，AI 對 pattern 定義的理解可能會不一致。

一開始會先用一個對話確認整體流程，之後為了保持 context 乾淨，每個步驟會換新的對話繼續做。但如果新的對話沒有重新提供 Read-only Entity Pattern 的 PDF，AI 可能會依照自己的理解重新解釋 pattern，導致細節和原始定義不同。

直接重新上傳 PDF 雖然可以解決一致性問題，但每次讀 PDF 都會消耗 token。另一個想法是先整理成濃縮版 markdown，但如果濃縮品質不好，也可能遺漏重要規則。