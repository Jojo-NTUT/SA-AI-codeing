# Read-only Entity Pattern

## Intent

Expose an internal child entity outside its aggregate without allowing the caller to mutate aggregate state through the returned reference.

Use this pattern when an aggregate root must return a child entity or a collection of child entities to a client. The aggregate still owns all state changes. External clients may read entity data, but any attempt to call a state-changing method on the returned entity must fail fast.

## Context

DDD aggregate boundaries require outside clients to work through the aggregate root. Child entities belong inside that boundary. Sometimes a use case or query still needs domain-level information from a child entity, for example:

- A `Plan` aggregate exposes a `Task`.
- A `Workflow` aggregate exposes a `Stage`.
- A `Team` aggregate exposes a `Project`.

Returning the mutable child entity directly lets the caller bypass the aggregate root and change state without domain events, preconditions, invariants, or event sourcing replay.

## Problem

How do you safely return child entities from an aggregate?

## Forces

- Encapsulation: clients must not mutate internal entities.
- Ubiquitous Language: returned types should remain domain concepts, not DTO or PO types.
- Fail-fast feedback: misuse should throw immediately instead of silently mutating a detached copy or misleading the caller.
- Aggregate integrity: all state changes must still go through aggregate root command methods and `when()` handlers.

## Solution

Return a read-only entity whenever an aggregate exposes an internal mutable child entity.

From the caller's perspective, the returned object represents the same domain concept and may keep the original entity type in method signatures. However, the runtime instance returned by the aggregate must be `ReadOnly{Entity}`. State-changing methods are overridden to throw `UnsupportedOperationException`. Domain query/accessor methods remain usable, but they must not leak mutable entities or mutable collections.

## Default Implementation: Special Case

Use the Special Case style by subclassing the real entity:

```java
public class ReadOnlyTask extends Task {
    public ReadOnlyTask(Task task) {
        super(task.getId(), task.getPlanId(), task.getName(), task.getState());
    }

    @Override
    void rename(String newName) {
        throw new UnsupportedOperationException("Read-only Task cannot be modified");
    }

    @Override
    void changeState(TaskState newState) {
        throw new UnsupportedOperationException("Read-only Task cannot be modified");
    }
}
```

This is the default for this skill because existing child entity generation uses concrete entity classes.

## Alternative Implementation: Proxy

Use a Proxy style only when the domain already has an entity interface or introducing one is worth the refactor:

```java
public final class ReadOnlyTask implements TaskView {
    private final Task real;

    public ReadOnlyTask(Task real) {
        this.real = Objects.requireNonNull(real);
    }

    @Override
    public TaskId getId() {
        return real.getId();
    }

    @Override
    public void rename(String newName) {
        throw new UnsupportedOperationException("Read-only Task cannot be modified");
    }
}
```

Do not introduce proxy interfaces by default. Prefer Special Case unless the spec or existing codebase already uses interfaces for child entities.

## Method Handling Rules

### Rule 1: Override Command Methods

Every state-changing method must throw `UnsupportedOperationException`.

```java
@Override
void rename(String newName) {
    throw new UnsupportedOperationException("Read-only Task cannot be modified");
}
```

This includes package-private mutation methods. Even if external callers cannot normally access package-private methods, overriding them documents and preserves the invariant inside the package and tests.

### Rule 2: Inherit Safe Domain Query/Accessor Methods

Domain query/accessor methods returning primitives, strings, enums, value objects, immutable timestamps, or IDs may be inherited.

```java
// Safe to inherit
public TaskId getId() { return id; }
public String getName() { return name; }
public TaskState getState() { return state; }
```

### Rule 3: Wrap Domain Query/Accessor Methods Returning Entities

If a domain query/accessor method returns another mutable child entity, override it and return the read-only version.

```java
@Override
public Member leader() {
    return new ReadOnlyMember(super.leader());
}
```

### Rule 4: Wrap Domain Query/Accessor Methods Returning Collections

Never return a mutable collection from a read-only entity.

- Collection of immutable values: return `List.copyOf(...)`, `Set.copyOf(...)`, or `Map.copyOf(...)`.
- Collection of mutable child entities: map each item to its read-only version, then return an unmodifiable collection.

```java
@Override
public List<Member> members() {
    return super.members().stream()
            .map(ReadOnlyMember::new)
            .map(Member.class::cast)
            .toList();
}
```

`Stream.toList()` returns an unmodifiable list on modern Java. `List.copyOf(...)` is also acceptable.

## Aggregate Accessor Rules

Aggregate roots must not expose mutable child entity references.

```java
// CORRECT
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
```

```java
// WRONG
public Task getTask(TaskId taskId) {
    return tasks.get(taskId);
}

public List<Task> getTasks() {
    return new ArrayList<>(tasks.values());
}
```

## Mapper Signature Rules

Do not force mapper method signatures to use `ReadOnly{Entity}` when the Special Case implementation is used. Because `ReadOnly{Entity}` extends `{Entity}`, use case mappers may keep the original entity type from the JSON spec while still receiving a read-only runtime instance from the aggregate.

```java
// CORRECT: mapper keeps the ubiquitous domain type
TaskDto toDto(Task task);
List<TaskDto> toDtoList(List<Task> tasks);

// The aggregate accessor still returns read-only runtime instances
Task task = productBacklogItem.getTask(taskId);
assert task instanceof ReadOnlyTask;
```

Only use `ReadOnly{Entity}` in mapper signatures when the Proxy implementation exposes an entity interface that makes this type part of the intended domain API. The required protection is the aggregate's read-only exposure, not mapper type substitution.

## Naming and Location

| Artifact | Rule |
|----------|------|
| Read-only class name | `ReadOnly{Entity}` |
| File path | `src/main/java/{rootPackage}/{aggregate}/entity/ReadOnly{Entity}.java` |
| Package | Same package as the aggregate and child entity |
| Constructor | `public ReadOnly{Entity}({Entity} entity)` |
| Exception type | `UnsupportedOperationException` |

## Generation Triggers

Generate `ReadOnly{Entity}.java` when all are true:

1. `spec.entities[]` contains a mutable child entity.
2. The aggregate exposes that entity through a domain accessor or uses it to supply a use case mapper/query result.
3. The returned entity is not a value object, DTO, projection, or immutable record.

Do not generate read-only entities for immutable record entities unless they contain mutable entity references or mutable collections.

## Tests

Add focused tests when a read-only entity is generated:

```java
@Test
void readOnlyTaskRejectsMutation() {
    Task task = new Task(TaskId.create(), planId, "Write tests");
    Task readOnly = new ReadOnlyTask(task);

    assertThrows(UnsupportedOperationException.class,
            () -> readOnly.rename("New name"));
}
```

Also test aggregate accessors:

```java
@Test
void aggregateReturnsReadOnlyTask() {
    Task task = plan.getTask(taskId);

    assertInstanceOf(ReadOnlyTask.class, task);
}
```

## Review Checklist

- Aggregate accessors do not return mutable child entities directly.
- `ReadOnly{Entity}` exists for each externally exposed mutable child entity.
- All mutation methods are overridden and throw `UnsupportedOperationException`.
- Domain query/accessor methods returning mutable child entities wrap results in read-only versions.
- Domain query/accessor methods returning collections return unmodifiable collections.
- Mapper signatures are not required to substitute `ReadOnly{Entity}` for `{Entity}` when `ReadOnly{Entity}` extends `{Entity}`.
- Read-only entities stay in the domain `entity` package and do not depend on framework or persistence classes.

## Related Patterns

- Entity: defines child entity identity and lifecycle.
- Aggregate Root: owns all mutations and protects invariants.
- DTO: acceptable at adapter boundaries, but not the default return type from aggregate domain methods.
- Prototype/deep copy: prevents direct mutation but fails silently; read-only entities are preferred because misuse throws immediately.
