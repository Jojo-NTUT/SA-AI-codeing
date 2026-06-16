package tw.teddysoft.aiscrum.pbi.usecase.port;

import tw.teddysoft.aiscrum.pbi.entity.EstimatedHours;

public final class EstimatedHoursMapper {
    private EstimatedHoursMapper() {
    }

    public static EstimatedHoursDto toDto(EstimatedHours hours) {
        return hours == null ? null : new EstimatedHoursDto(hours.toString());
    }

    public static EstimatedHours toDomain(EstimatedHoursDto dto) {
        return dto == null ? null : EstimatedHours.valueOf(dto.value());
    }
}
