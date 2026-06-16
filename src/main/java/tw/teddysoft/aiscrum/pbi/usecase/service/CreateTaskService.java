package tw.teddysoft.aiscrum.pbi.usecase.service;

import tw.teddysoft.aiscrum.pbi.entity.EstimatedHours;
import tw.teddysoft.aiscrum.pbi.entity.PbiId;
import tw.teddysoft.aiscrum.pbi.entity.ProductBacklogItem;
import tw.teddysoft.aiscrum.pbi.entity.ProductId;
import tw.teddysoft.aiscrum.pbi.entity.TaskId;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.CreateTaskUseCase;
import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;
import tw.teddysoft.ezddd.usecase.port.in.interactor.ExitCode;
import tw.teddysoft.ezddd.usecase.port.in.interactor.UseCaseFailureException;
import tw.teddysoft.ezddd.usecase.port.out.repository.Repository;

import java.util.Objects;

import static tw.teddysoft.ucontract.Contract.requireNotNull;

public class CreateTaskService implements CreateTaskUseCase {
    private final Repository<ProductBacklogItem, PbiId> repository;

    public CreateTaskService(Repository<ProductBacklogItem, PbiId> repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public CqrsOutput<?> execute(CreateTaskInput input) {
        requireNotNull("Input", input);
        requireNotNull("Product id", input.productId);
        requireNotNull("PBI id", input.pbiId);
        requireNotNull("Task id", input.taskId);
        requireNotNull("Task name", input.name);
        requireNotNull("Creator id", input.creatorId);

        try {
            PbiId pbiId = PbiId.valueOf(input.pbiId);
            ProductBacklogItem pbi = repository.findById(pbiId).orElse(null);
            if (pbi == null) {
                pbi = new ProductBacklogItem(ProductId.valueOf(input.productId), pbiId, input.creatorId);
            }

            TaskId taskId = TaskId.valueOf(input.taskId);
            pbi.createTask(taskId, input.name, EstimatedHours.valueOf(input.estimatedHours), input.creatorId);
            repository.save(pbi);

            return CqrsOutput.create()
                    .setId(taskId.value())
                    .setExitCode(ExitCode.SUCCESS);
        } catch (Exception e) {
            throw new UseCaseFailureException(e);
        }
    }
}
