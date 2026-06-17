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

## 實作對象

本作業選擇 `ProductBacklogItem` 作為目標 Aggregate，因為它會管理 child entity `Task`。

`Task` 有自己的 identity，也有多個會變動的欄位，例如：

- `state`
- `estimateHours`
- `remainingHours`
- `assigneeIds`
- `blockedBy`

因此 `Task` 適合用來驗證 Read-only Entity Pattern：外部可以讀取 task 狀態，但不應該直接修改 task，只能透過 `ProductBacklogItem` Aggregate 的行為來改變。

實作上新增了 `ReadOnlyTask`，並讓 `ProductBacklogItem` 的 accessor 回傳 read-only runtime instance：

- `getTask(taskId)` 回傳 `ReadOnlyTask`
- `getTasks()` 回傳不可修改的 `List<Task>`，且 list 中每個元素都是 `ReadOnlyTask`

## Use Case 驗證

筆記中選定兩個 use case 作為驗證目標：

- Command use case：`.dev/specs/pbi/usecase/create-task.json`
- Query use case：`.dev/specs/pbi/usecase/get-tasks-by-pbi.json`

這兩個 use case 可以分別驗證：

- 建立 task 時，狀態改變仍然必須透過 `ProductBacklogItem` Aggregate。
- 查詢 task 時，對外取得的 task 只能讀取，不能透過 entity reference 修改內部狀態。

生成程式碼使用的命令：
- execute-uc --only-inmemory .dev/specs/pbi/usecase/create-task.json
- execute-uc --only-inmemory .dev/specs/pbi/usecase/get-tasks-by-pbi.json

## 測試重點

本作業的測試重點分成三類：

1. Read-only entity mutation 防護
   - 外部取得 `ReadOnlyTask` 後，呼叫 `rename`、`changeState`、`assignTo`、`estimate` 等 mutation method 都必須丟出 `UnsupportedOperationException`。

2. Aggregate accessor 防護
   - `ProductBacklogItem.getTask(taskId)` 不可回傳原本的 mutable `Task`。
   - `ProductBacklogItem.getTasks()` 回傳的每個 task 都必須是 `ReadOnlyTask`。
   - 回傳的 task collection 本身不可被外部修改。

3. Use case 行為符合 pattern
   - `create-task` use case 建立 task 後，對外暴露的 task 必須是 read-only。
   - `get-tasks-by-pbi` use case 查詢 task list 時，不應該讓外部取得可修改的 entity reference。

## 遇到的問題

本次作業遇到的主要問題是：在不同對話中延續工作時，AI 對 pattern 定義的理解可能會不一致。

一開始會先用一個對話確認整體流程，之後為了保持 context 乾淨，每個步驟會換新的對話繼續做。但如果新的對話沒有重新提供 Read-only Entity Pattern 的 PDF，AI 可能會依照自己的理解重新解釋 pattern，導致細節和原始定義不同。

直接重新上傳 PDF 雖然可以解決一致性問題，但每次讀 PDF 都會消耗 token。另一個想法是先整理成濃縮版 markdown，但如果濃縮品質不好，也可能遺漏重要規則。

## 結論

本作業將 Read-only Entity Pattern 補進 `ezddd-java` skill，並以 `ProductBacklogItem` 與 `Task` 作為實作案例。透過 `ReadOnlyTask` 與 Aggregate accessor 規則，可以確保外部只能讀取 child entity，不能繞過 Aggregate 直接修改內部狀態。

這次實作也說明了在 AI coding 流程中，將重要設計規則沉澱成 skill 文件比只依賴單次 prompt 更穩定。當 pattern 定義、生成規則、review checklist 與 deterministic review rule 都指向同一份 authority 文件時，AI agent 產生的程式碼會更容易維持一致性。
