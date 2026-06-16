package tw.teddysoft.aiscrum.pbi.usecase.port.out.projection;

import tw.teddysoft.aiscrum.pbi.usecase.port.TaskDto;
import tw.teddysoft.ezddd.cqrs.usecase.query.Projection;
import tw.teddysoft.ezddd.cqrs.usecase.query.ProjectionInput;

import java.util.List;

public interface TasksDtoProjection extends Projection<TasksDtoProjection.TasksDtoProjectionInput, List<TaskDto>> {
    class TasksDtoProjectionInput implements ProjectionInput {
        public String pbiId;

        public static TasksDtoProjectionInput create(String pbiId) {
            TasksDtoProjectionInput input = new TasksDtoProjectionInput();
            input.pbiId = pbiId;
            return input;
        }
    }
}
