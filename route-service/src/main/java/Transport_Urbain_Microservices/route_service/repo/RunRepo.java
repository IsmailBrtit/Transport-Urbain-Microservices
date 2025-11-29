package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.Run;
import Transport_Urbain_Microservices.route_service.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RunRepo extends JpaRepository<Run, Long> {
    long countByRouteAndScheduleTypeAndDayOfWeek(
            Route route,
            ScheduleType scheduleType,
            Integer dayOfWeek
    );

    long countByRouteAndScheduleTypeAndSpecificDate(
            Route route,
            ScheduleType scheduleType,
            LocalDate specificDate
    );

    List<Run> findByRouteAndScheduleTypeAndSpecificDateBetween(Route route, ScheduleType scheduleType, LocalDate specificDateAfter, LocalDate specificDateBefore);

    List<Run> findByRouteAndScheduleTypeAndDayOfWeekIn(Route route, ScheduleType scheduleType, Collection<Integer> dayOfWeeks);

    List<Run> findByScheduleTypeAndDayOfWeekIn(ScheduleType scheduleType, Collection<Integer> dayOfWeeks);

    List<Run> findByScheduleTypeAndSpecificDateBetween(ScheduleType scheduleType, LocalDate specificDateAfter, LocalDate specificDateBefore);

    List<Run> findAllByRoute(Route route);

    boolean existsByRouteAndScheduleType(Route route, ScheduleType scheduleType);

    boolean existsByRouteAndScheduleTypeAndDayOfWeekAndStartTime(Route route, ScheduleType scheduleType, Integer dayOfWeek, LocalTime startTime);

    List<Run> findByRoute(Route route);

    List<Run> findByRouteAndScheduleTypeAndSpecificDate(Route route, ScheduleType scheduleType, LocalDate specificDate);

    List<Run> findByRouteAndScheduleTypeAndDayOfWeek(Route route, ScheduleType scheduleType, Integer dayOfWeek);

    long countByScheduleTypeAndSpecificDate(ScheduleType scheduleType, LocalDate specificDate);

    List<Run> findByScheduleTypeAndSpecificDate(ScheduleType scheduleType, LocalDate specificDate);
}
