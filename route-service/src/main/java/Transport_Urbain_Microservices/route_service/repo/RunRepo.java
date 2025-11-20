package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.Run;
import Transport_Urbain_Microservices.route_service.entity.ScheduleType;
import Transport_Urbain_Microservices.route_service.entity.StopTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

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

    List<Run> findAllByStopTimesIn(Collection<List<StopTime>> stopTimes);
}
