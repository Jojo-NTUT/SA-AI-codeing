package tw.teddysoft.aiscrum.pbi.usecase.port;

import java.util.List;
import java.util.Set;

public record TaskDto(
        String id,
        String pbiId,
        String name,
        String state,
        Set<String> assigneeIds,
        EstimatedHoursDto estimatedHours,
        RemainingHoursDto remainingHours,
        List<String> blockedBy,
        String note,
        String extension
) {
}
