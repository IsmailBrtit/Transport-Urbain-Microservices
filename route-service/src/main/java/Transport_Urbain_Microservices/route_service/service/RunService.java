package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.CreateRunDTO;
import Transport_Urbain_Microservices.route_service.dto.RunDetailsDto;
import Transport_Urbain_Microservices.route_service.dto.UpcomingRunForStopDto;
import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.exception.InvalidRunDataException;
import Transport_Urbain_Microservices.route_service.exception.ResourceNotFoundException;
import Transport_Urbain_Microservices.route_service.mapper.RunMapper;
import Transport_Urbain_Microservices.route_service.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RunService {

    private final RunRepo runRepo;
    private final RouteRepo routeRepo;
    private final StopRepo stopRepo;
    private final StopTimeRepo stopTimeRepo;
    private final SpecialDayRepo specialDayRepo;

    @Transactional
    public Run createRun(CreateRunDTO dto) {
        // Validate all data
        ValidationContext context = validateRunData(dto);

        // Calculate run number for the day
        int runNum = calculateRunNumber(context.route, dto.getScheduleType(),
                dto.getDayOfWeek(), dto.getSpecificDate());

        // Determine destination stop name (last stop in the route based on direction)
        String destinationStopName = determineDestinationStopName(context);

        // Create Run entity
        Run run = new Run();
        run.setRoute(context.route);
        run.setDirection(dto.getDirection());
        run.setDestinationStopName(destinationStopName);
        run.setScheduleType(dto.getScheduleType());
        run.setDayOfWeek(dto.getDayOfWeek());
        run.setSpecificDate(dto.getSpecificDate());
        run.setRunNum(runNum);
        run.setStartTime(dto.getStartTime());

        if (dto.getScheduleType() == ScheduleType.SPECIAL) {
            LocalDate date = dto.getSpecificDate();
            if (!specialDayRepo.existsByDate(date)) {
                specialDayRepo.save(new SpecialDay(date));
            }
        }

        // Create StopTime entities
        List<StopTime> stopTimes = new ArrayList<>();
        for (CreateRunDTO.StopTimeDTO stopTimeDTO : dto.getStopTimes()) {
            StopTime stopTime = new StopTime();
            stopTime.setRun(run);
            stopTime.setStop(context.stopMap.get(stopTimeDTO.getStopId()));
            stopTime.setArrivalMinuteFromStart(stopTimeDTO.getArrivalMinuteFromStart());
            stopTimes.add(stopTime);
        }

        run.setStopTimes(stopTimes);

        return runRepo.save(run);
    }

    private ValidationContext validateRunData(CreateRunDTO dto) {
        // 1. Validate schedule type constraints
        validateScheduleTypeConstraints(dto);

        // 2. Fetch and validate route exists
        Route route = routeRepo.findById(dto.getRouteId())
                .orElseThrow(() -> new InvalidRunDataException("Route not found"));

        // 3. Ensure route has stops
        if (route.getRouteStops() == null || route.getRouteStops().isEmpty()) {
            throw new InvalidRunDataException("Route has no configured stops");
        }

        // 4. Get ordered route stops based on direction
        List<Long> orderedRouteStopIds = getOrderedRouteStopIds(route, dto.getDirection());

        // 5. Validate all stops exist in database
        Set<Long> dtoStopIds = dto.getStopTimes().stream()
                .map(CreateRunDTO.StopTimeDTO::getStopId)
                .collect(Collectors.toSet());

        List<Stop> stops = stopRepo.findAllById(dtoStopIds);
        if (stops.size() != dtoStopIds.size()) {
            throw new InvalidRunDataException("Some stops not found");
        }

        Map<Long, Stop> stopMap = stops.stream()
                .collect(Collectors.toMap(Stop::getId, stop -> stop));

        // 6. Validate stop IDs match route stops
        Set<Long> routeStopIdSet = new HashSet<>(orderedRouteStopIds);
        if (!routeStopIdSet.equals(dtoStopIds)) {
            throw new InvalidRunDataException("Incoherent stop data");
        }

        // 7. Validate stop times are ordered correctly with increasing times
        validateStopTimesOrdering(dto.getStopTimes(), orderedRouteStopIds);

        return new ValidationContext(route, orderedRouteStopIds, stopMap);
    }

    private void validateScheduleTypeConstraints(CreateRunDTO dto) {
        if (dto.getScheduleType() == ScheduleType.REGULAR) {
            if (dto.getDayOfWeek() == null) {
                throw new InvalidRunDataException("Day of week required for regular schedule");
            }
            if (dto.getSpecificDate() != null) {
                throw new InvalidRunDataException("Specific date must be null for regular schedule");
            }
        } else if (dto.getScheduleType() == ScheduleType.SPECIAL) {
            if (dto.getSpecificDate() == null) {
                throw new InvalidRunDataException("Specific date required for special schedule");
            }
            if (dto.getDayOfWeek() != null) {
                throw new InvalidRunDataException("Day of week must be null for special schedule");
            }
        }
    }

    private List<Long> getOrderedRouteStopIds(Route route, Direction direction) {
        List<Long> stopIds = route.getRouteStops().stream()
                .sorted(Comparator.comparing(RouteStop::getOrder))
                .map(rs -> rs.getStop().getId())
                .collect(Collectors.toList());

        if (direction == Direction.RETOUR) {
            Collections.reverse(stopIds);
        }

        return stopIds;
    }

    private void validateStopTimesOrdering(List<CreateRunDTO.StopTimeDTO> stopTimes,
                                           List<Long> orderedRouteStopIds) {
        // Create a map of stopId -> position in route
        Map<Long, Integer> stopPositions = new HashMap<>();
        for (int i = 0; i < orderedRouteStopIds.size(); i++) {
            stopPositions.put(orderedRouteStopIds.get(i), i);
        }

        // Sort stop times by their position in the route
        List<CreateRunDTO.StopTimeDTO> sortedByRoute = new ArrayList<>(stopTimes);
        sortedByRoute.sort(Comparator.comparing(st -> stopPositions.get(st.getStopId())));

        // Validate that arrival times are strictly increasing when ordered by route
        for (int i = 1; i < sortedByRoute.size(); i++) {
            int prevTime = sortedByRoute.get(i - 1).getArrivalMinuteFromStart();
            int currTime = sortedByRoute.get(i).getArrivalMinuteFromStart();

            if (currTime <= prevTime) {
                throw new InvalidRunDataException("Incoherent stop time data");
            }
        }
    }

    private int calculateRunNumber(Route route, ScheduleType scheduleType,
                                   Integer dayOfWeek, LocalDate specificDate) {
        if (scheduleType == ScheduleType.REGULAR) {
            // Count runs for this route on this day of week
            return (int) runRepo.countByRouteAndScheduleTypeAndDayOfWeek(
                    route, ScheduleType.REGULAR, dayOfWeek) + 1;
        } else {
            // Count runs for this route on this specific date
            return (int) runRepo.countByRouteAndScheduleTypeAndSpecificDate(
                    route, ScheduleType.SPECIAL, specificDate) + 1;
        }
    }

    private String determineDestinationStopName(ValidationContext context) {
        // Last stop in the ordered list is the destination
        Long lastStopId = context.orderedRouteStopIds.get(context.orderedRouteStopIds.size() - 1);
        return context.stopMap.get(lastStopId).getName();
    }

    // Helper class to hold validation results
    private record ValidationContext(
            Route route,
            List<Long> orderedRouteStopIds,
            Map<Long, Stop> stopMap
    ){}

    public RunDetailsDto getRunById(Long id) {
        Run run = runRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Run not found"));
        return RunMapper.mapToRunDetailsDto(run);
    }

    @Transactional
    public void deleteRunById(Long id) {
        if (!runRepo.existsById(id)) {
            throw new ResourceNotFoundException("Run not found");
        }
        runRepo.deleteById(id);
    }

    /**
     * Returns all runs that start in the next 24h for a specific route
     * To return runs for all routes, leave routeId null
     */
    public List<RunDetailsDto> getRunsForRouteInNext24Hours(Long routeId) {

        Route route = null;
        if (routeId != null) {
            route = routeRepo.findById(routeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        }
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalTime now = LocalTime.now();

        List<Run> runs = new ArrayList<>();

        int todayDayOfWeek = today.getDayOfWeek().getValue();
        int tomorrowDayOfWeek = tomorrow.getDayOfWeek().getValue();

        List<Run> regularRuns;
        if (routeId == null){
            regularRuns = runRepo.findByScheduleTypeAndDayOfWeekIn(
                    ScheduleType.REGULAR, Arrays.asList(todayDayOfWeek, tomorrowDayOfWeek));
        } else {
            regularRuns = runRepo.findByRouteAndScheduleTypeAndDayOfWeekIn(
                    route, ScheduleType.REGULAR, Arrays.asList(todayDayOfWeek, tomorrowDayOfWeek));
        }
        for (Run run : regularRuns) {
            if (isRunInNext24Hours(run, today, tomorrow, now)) {
                runs.add(run);
            }
        }

        List<Run> specialRuns;
        if (routeId == null){
            specialRuns = runRepo.findByScheduleTypeAndSpecificDateBetween(
                    ScheduleType.SPECIAL, today, tomorrow);
        } else {
            specialRuns = runRepo.findByRouteAndScheduleTypeAndSpecificDateBetween(
                    route, ScheduleType.SPECIAL, today, tomorrow);
        }
        for (Run run : specialRuns) {
            if (isRunInNext24Hours(run, today, tomorrow, now)) {
                runs.add(run);
            }
        }

        return runs.stream()
                .map(RunMapper::mapToRunDetailsDto)
                .sorted(Comparator.comparing(RunDetailsDto::getStartTime))
                //this sorting mechanism is flawed, it doesn't take into account date
                .collect(Collectors.toList());
    }

    /**
     * returns all the runs that arrive at a given stop in the next 24
     */
    public List<UpcomingRunForStopDto> getRunsForStopInNext24Hours(Long stopId){
        Stop currentStop = stopRepo.findById(stopId).orElseThrow(() -> new ResourceNotFoundException("Stop not found"));
        List<StopTime> stopTimes = stopTimeRepo.findByStop(currentStop);
        if (stopTimes.isEmpty()) return List.of();

        LocalDate todayDate = LocalDate.now();
        LocalDate yesterdayDate = todayDate.minusDays(1);
        int todayDayOfWeek = todayDate.getDayOfWeek().getValue();
        int yesterdayDayOfWeek = yesterdayDate.getDayOfWeek().getValue();

        Map<Long, LocalDateTime> stopArrivalTimeMap = new HashMap<>(); // the key being the stop time id
        for (StopTime stopTime : stopTimes) {
            Run r = stopTime.getRun();
            // filtering out duplicate runs in case of special days
            if(specialDayRepo.existsByDate(yesterdayDate)){
                if (r.getScheduleType().equals(ScheduleType.REGULAR) && r.getDayOfWeek() == yesterdayDayOfWeek ) continue;
            }
            if(specialDayRepo.existsByDate(todayDate)){
                if (r.getScheduleType().equals(ScheduleType.REGULAR) && r.getDayOfWeek() == todayDayOfWeek ) continue;
            }

            //calculating start LocalDateTime for each run
            LocalDateTime startLocalDateTime = null;

            if (r.getScheduleType().equals(ScheduleType.SPECIAL)){
                startLocalDateTime = r.getSpecificDate().atTime(r.getStartTime());
            } else {
                if(r.getDayOfWeek()==todayDayOfWeek){
                    startLocalDateTime = todayDate.atTime(r.getStartTime());
                } else if (r.getDayOfWeek()==yesterdayDayOfWeek){
                    startLocalDateTime = yesterdayDate.atTime(r.getStartTime());
                } else {
                    continue;
                }
            }

            if (startLocalDateTime.isBefore(yesterdayDate.atStartOfDay())
                    || startLocalDateTime.isAfter(LocalDateTime.now())
            ) continue;

            LocalDateTime stopArrivalTime = startLocalDateTime.plusMinutes(stopTime.getArrivalMinuteFromStart());

            if ( stopArrivalTime.isBefore(LocalDateTime.now().minusMinutes(5))
                    || stopArrivalTime.isAfter(LocalDateTime.now().plusDays(1))
            ) continue;

            stopArrivalTimeMap.put(stopTime.getId(), stopArrivalTime);
        }
        List<UpcomingRunForStopDto> l = new ArrayList<>();
        stopArrivalTimeMap.entrySet().stream().forEach(
                entry -> {
                    UpcomingRunForStopDto u = new UpcomingRunForStopDto();
                    StopTime st = stopTimes.stream()
                            .filter(s->s.getId().equals(entry.getKey()))
                            .findFirst().orElseThrow();
                    u.setRunId(st.getRun().getId());
                    u.setArrivalTime(entry.getValue());
                    u.setRouteNum(st.getRun().getRoute().getNum());
                    u.setRouteName(st.getRun().getRoute().getName());
                    u.setDestination(st.getRun().getDestinationStopName());
                    l.add(u);
                }
        );
        return l.stream().sorted(Comparator.comparing(UpcomingRunForStopDto::getArrivalTime)).collect(Collectors.toList());
    }

    public List<RunDetailsDto> getAllRuns(){
        List<Run> runs = runRepo.findAll();
        return runs.stream().map(RunMapper::mapToRunDetailsDto).collect(Collectors.toList());
    }

    public List<RunDetailsDto> getAllRunsByRoute(Long routeId){
        Route route = routeRepo.findById(routeId).orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        List<Run> runs = runRepo.findAllByRoute(route);
        return runs.stream().map(RunMapper::mapToRunDetailsDto).collect(Collectors.toList());
    }

    public List<RunDetailsDto> getAllRunsByStop(Long stopId){
        Stop stop = stopRepo.findById(stopId).orElseThrow(() -> new ResourceNotFoundException("Stop not found"));
        List<StopTime> stopTimes =  stopTimeRepo.findByStop(stop);
        Set<Run> runs = new HashSet<>();
        for(StopTime stopTime : stopTimes){
            runs.add(stopTime.getRun());
        }
        return runs.stream().map(RunMapper::mapToRunDetailsDto).collect(Collectors.toList());
    }

    private boolean isRunInNext24Hours(Run run, LocalDate today, LocalDate tomorrow, LocalTime now) {
        if (run.getScheduleType() == ScheduleType.REGULAR) {
            int runDayOfWeek = run.getDayOfWeek();
            int todayDayOfWeek = today.getDayOfWeek().getValue();
            int tomorrowDayOfWeek = tomorrow.getDayOfWeek().getValue();

            // Run is today
            if (runDayOfWeek == todayDayOfWeek) {
                return run.getStartTime().isAfter(now) || run.getStartTime().equals(now);
            }
            // Run is tomorrow
            else if (runDayOfWeek == tomorrowDayOfWeek) {
                // Check if it's within 24 hours from now
                return run.getStartTime().isBefore(now) || run.getStartTime().equals(now);
            }
            return false;
        } else {
            // SPECIAL schedule
            LocalDate runDate = run.getSpecificDate();

            if (runDate.equals(today)) {
                return run.getStartTime().isAfter(now) || run.getStartTime().equals(now);
            } else if (runDate.equals(tomorrow)) {
                return run.getStartTime().isBefore(now) || run.getStartTime().equals(now);
            }
            return false;
        }
    }
}
