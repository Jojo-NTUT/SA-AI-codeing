package tw.teddysoft.aiscrum.pbi.usecase.port.in;

import tw.teddysoft.ezddd.cqrs.usecase.CqrsOutput;
import tw.teddysoft.ezddd.cqrs.usecase.command.Command;
import tw.teddysoft.ezddd.usecase.port.in.interactor.Input;

public interface CreateTaskUseCase extends Command<CreateTaskUseCase.CreateTaskInput, CqrsOutput<?>> {
    class CreateTaskInput implements Input {
        public String productId;
        public String pbiId;
        public String taskId;
        public String name;
        public String estimatedHours;
        public String creatorId;

        public static CreateTaskInput create() {
            return new CreateTaskInput();
        }
    }
}
