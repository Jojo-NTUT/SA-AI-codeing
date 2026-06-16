package tw.teddysoft.aiscrum.pbi.io.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.teddysoft.aiscrum.pbi.adapter.out.memory.InMemoryTasksDtoProjection;
import tw.teddysoft.aiscrum.pbi.entity.PbiId;
import tw.teddysoft.aiscrum.pbi.entity.ProductBacklogItem;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.CreateTaskUseCase;
import tw.teddysoft.aiscrum.pbi.usecase.port.in.GetTasksByProductBacklogItemUseCase;
import tw.teddysoft.aiscrum.pbi.usecase.port.out.projection.TasksDtoProjection;
import tw.teddysoft.aiscrum.pbi.usecase.service.CreateTaskService;
import tw.teddysoft.aiscrum.pbi.usecase.service.GetTasksByProductBacklogItemService;
import tw.teddysoft.ezddd.usecase.port.out.repository.Repository;

@Configuration
public class ProductBacklogItemUseCaseConfig {
    @Bean
    public CreateTaskUseCase createTaskUseCase(Repository<ProductBacklogItem, PbiId> productBacklogItemRepository) {
        return new CreateTaskService(productBacklogItemRepository);
    }

    @Bean
    public TasksDtoProjection tasksDtoProjection(Repository<ProductBacklogItem, PbiId> productBacklogItemRepository) {
        return new InMemoryTasksDtoProjection(productBacklogItemRepository);
    }

    @Bean
    public GetTasksByProductBacklogItemUseCase getTasksByProductBacklogItemUseCase(TasksDtoProjection tasksDtoProjection) {
        return new GetTasksByProductBacklogItemService(tasksDtoProjection);
    }
}
