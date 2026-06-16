package tw.teddysoft.aiscrum.pbi.usecase.port.in;

import tw.teddysoft.aiscrum.pbi.usecase.port.TaskDto;
import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;
import tw.teddysoft.ezddd.cqrs.usecase.query.Query;
import tw.teddysoft.ezddd.usecase.port.in.interactor.Input;

import java.util.List;

public interface GetTasksByProductBacklogItemUseCase extends Query<GetTasksByProductBacklogItemUseCase.GetTasksByProductBacklogItemInput, GetTasksByProductBacklogItemUseCase.GetTasksByProductBacklogItemOutput> {
    class GetTasksByProductBacklogItemInput implements Input {
        public String pbiId;

        public static GetTasksByProductBacklogItemInput create() {
            return new GetTasksByProductBacklogItemInput();
        }
    }

    class GetTasksByProductBacklogItemOutput extends CqrsOutput<GetTasksByProductBacklogItemOutput> {
        private List<TaskDto> tasks;

        public static GetTasksByProductBacklogItemOutput create() {
            return new GetTasksByProductBacklogItemOutput();
        }

        public List<TaskDto> getTasks() {
            return tasks;
        }

        public GetTasksByProductBacklogItemOutput setTasks(List<TaskDto> tasks) {
            this.tasks = tasks;
            return this;
        }
    }
}
