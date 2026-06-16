package tw.teddysoft.aiscrum.pbi.adapter.out.memory;

import tw.teddysoft.aiscrum.pbi.entity.PbiId;
import tw.teddysoft.aiscrum.pbi.entity.ProductBacklogItem;
import tw.teddysoft.aiscrum.pbi.usecase.port.TaskDto;
import tw.teddysoft.aiscrum.pbi.usecase.port.TaskMapper;
import tw.teddysoft.aiscrum.pbi.usecase.port.out.projection.TasksDtoProjection;
import tw.teddysoft.ezddd.usecase.port.out.repository.Repository;

import java.util.List;
import java.util.Objects;

public class InMemoryTasksDtoProjection implements TasksDtoProjection {
    private final Repository<ProductBacklogItem, PbiId> repository;

    public InMemoryTasksDtoProjection(Repository<ProductBacklogItem, PbiId> repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<TaskDto> query(TasksDtoProjectionInput input) {
        return repository.findById(PbiId.valueOf(input.pbiId))
                .map(ProductBacklogItem::getTasks)
                .map(TaskMapper::toDtoList)
                .orElseGet(List::of);
    }
}
