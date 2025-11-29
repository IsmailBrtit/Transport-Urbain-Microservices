package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.CreateRunDTO;
import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.exception.ResourceNotFoundException;
import Transport_Urbain_Microservices.route_service.mapper.RunMapper;
import Transport_Urbain_Microservices.route_service.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RunService{

    private final RunMapper runMapper;

    private final RunRepo runRepo;
    private final RouteRepo routeRepo;
    private final StopRepo stopRepo;
    private final RouteStopRepo routeStopRepo;
    private final SpecialDayRepo specialDayRepo;

    public RunDetailsDto getRunById(Long runId){
        Run run = runRepo.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Run not found"));
        return runMapper.toDetailsDto(run);
    }

    public List<RunDetailsDto> getAllRuns(){
        List<Run> runs = runRepo.findAll();
        return runs.stream().map(runMapper::toDetailsDto).toList();
    }

    public List<RunDetailsDto> getAllRunsForRoute(Long routeId) {
        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        if (route.getStatus() != RouteStatus.ACTIVE) {
            return List.of(); // Empty list if suspended or under maintenance
        }
        return runRepo.findByRoute(route).stream().map(runMapper::toDetailsDto).toList();
    }

    public List<RunDetailsDto> getAllRunsForRouteForGivenDay(Long routeId, LocalDate givenDay) {
        Route route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        if (route.getStatus() != RouteStatus.ACTIVE) {
            return List.of(); // Empty list if suspended or under maintenance
        }
        boolean isSpecial = specialDayRepo.existsById(givenDay);
        if (isSpecial) {
            return runRepo.findByRouteAndScheduleTypeAndSpecificDate(route, ScheduleType.SPECIAL, givenDay)
                    .stream()
                    .map(runMapper::toDetailsDto)
                    .toList();
        } else {
            int dayOfWeek = givenDay.getDayOfWeek().getValue(); // 1=MONDAY to 7=SUNDAY
            return runRepo.findByRouteAndScheduleTypeAndDayOfWeek(route, ScheduleType.REGULAR, dayOfWeek)
                    .stream()
                    .map(runMapper::toDetailsDto)
                    .toList();
        }
    }

    public List<RunDetailsDto> getAllRunsForStop(Long stopId) {
        Stop stop = stopRepo.findById(stopId)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found"));
        List<Route> routes = routeStopRepo.findByStop(stop).stream()
                .map(RouteStop::getRoute)
                .filter(route -> route.getStatus() == RouteStatus.ACTIVE)
                .distinct()
                .toList();
        List<Run> runs = new ArrayList<>();
        for (Route route : routes) {
            runs.addAll(runRepo.findByRoute(route));
        }
        return runs.stream().map(runMapper::toDetailsDto).toList();
    }

    public List<RunDetailsDto> getAllRunsForStopForGivenDay(Long stopId, LocalDate givenDay) {
        Stop stop = stopRepo.findById(stopId)
                .orElseThrow(() -> new IllegalArgumentException("Stop not found"));
        List<Route> routes = routeStopRepo.findByStop(stop).stream()
                .map(RouteStop::getRoute)
                .filter(route -> route.getStatus() == RouteStatus.ACTIVE) // Filter active routes only
                .distinct()
                .toList();
        List<Run> runs = new ArrayList<>();
        boolean isSpecial = specialDayRepo.existsByDate(givenDay);
        for (Route route : routes) {
            if (isSpecial) {
                runs.addAll(runRepo.findByRouteAndScheduleTypeAndSpecificDate(route, ScheduleType.SPECIAL, givenDay));
            } else {
                int dayOfWeek = givenDay.getDayOfWeek().getValue();
                runs.addAll(runRepo.findByRouteAndScheduleTypeAndDayOfWeek(route, ScheduleType.REGULAR, dayOfWeek));
            }
        }
        return runs.stream().map(runMapper::toDetailsDto).toList();
    }

    @Transactional
    public void deleteRunById(Long runId) {
        Run run = runRepo.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run not found"));
        if (run.getScheduleType() == ScheduleType.SPECIAL) {
            LocalDate date = run.getSpecificDate();
            runRepo.delete(run);
            long remainingSpecialRuns = runRepo.countByScheduleTypeAndSpecificDate(ScheduleType.SPECIAL, date);
            if (remainingSpecialRuns == 0) {
                specialDayRepo.deleteById(date);
            }
        } else {
            runRepo.delete(run);
        }
    }

    @Transactional
    public void deleteAllSpecialRunsForGivenDay(LocalDate date) {
        List<Run> specialRuns = runRepo.findByScheduleTypeAndSpecificDate(ScheduleType.SPECIAL, date);
        runRepo.deleteAll(specialRuns);
        specialDayRepo.deleteById(date);
    }

    @Transactional
    public RunDetailsDto createRun(CreateRunDTO createRunDTO) {
        Run run = new Run();
        Route route = routeRepo.findById(createRunDTO.getRouteId()).orElseThrow(
                () -> new ResourceNotFoundException("Route not found")
        );
        List<RouteStop> orderedStops = routeStopRepo.findByRouteOrderByStopOrderAsc(route);
        Stop lastStop = orderedStops.getLast().getStop();
        run.setRoute(route);
        run.setDestinationStopName(lastStop.getName());
        run.setScheduleType(createRunDTO.getScheduleType());
        if(createRunDTO.getScheduleType() == ScheduleType.SPECIAL) {
            run.setSpecificDate(createRunDTO.getSpecificDate());
            run.setDayOfWeek(null);
            run.setRunNum((int)runRepo.countByRouteAndScheduleTypeAndSpecificDate(route, ScheduleType.SPECIAL, createRunDTO.getSpecificDate()) + 1);
            if (!specialDayRepo.existsById(createRunDTO.getSpecificDate())) {
                specialDayRepo.save(new SpecialDay(createRunDTO.getSpecificDate()));
            }
        } else {
            run.setDayOfWeek(createRunDTO.getDayOfWeek());
            run.setSpecificDate(null);
            run.setRunNum((int)runRepo.countByRouteAndScheduleTypeAndDayOfWeek(route, ScheduleType.REGULAR, createRunDTO.getDayOfWeek()) + 1);
        }
        run.setStartTime(createRunDTO.getStartTime());
        Run savedRun = runRepo.save(run);
        return runMapper.toDetailsDto(savedRun);
    }
}
