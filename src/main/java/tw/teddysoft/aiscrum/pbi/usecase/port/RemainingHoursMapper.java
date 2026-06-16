package tw.teddysoft.aiscrum.pbi.usecase.port;

import tw.teddysoft.aiscrum.pbi.entity.RemainingHours;

public final class RemainingHoursMapper {
    private RemainingHoursMapper() {
    }

    public static RemainingHoursDto toDto(RemainingHours hours) {
        return hours == null ? null : new RemainingHoursDto(hours.toString());
    }

    public static RemainingHours toDomain(RemainingHoursDto dto) {
        return dto == null ? null : RemainingHours.valueOf(dto.value());
    }
}
