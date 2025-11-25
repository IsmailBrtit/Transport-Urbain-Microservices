package Transport_Urbain_Microservices.route_service.dataloader;

import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.repo.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@Order(2)
public class ScheduleDataLoader implements ApplicationRunner {
    @Value("${app.schedule-file:classpath:schedules.json}")
    private Resource scheduleFile;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RouteRepo routeRepo;
    private final StopRepo stopRepo;
    private final RunRepo runRepo;
    private final RouteStopOffsetRepo routeStopOffsetRepo;

    public ScheduleDataLoader(RouteRepo routeRepo,
                              StopRepo stopRepo,
                              RunRepo runRepo,
                              RouteStopOffsetRepo routeStopOffsetRepo) {
        this.routeRepo = routeRepo;
        this.stopRepo = stopRepo;
        this.runRepo = runRepo;
        this.routeStopOffsetRepo = routeStopOffsetRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try (InputStream is = scheduleFile.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode routesNode = root.path("routes");
            if (routesNode.isMissingNode() || !routesNode.isObject()) {
                System.out.println("No routes object found in schedule file");
                return;
            }

            Iterator<Map.Entry<String, JsonNode>> routesIter = routesNode.fields();
            while (routesIter.hasNext()) {
                Map.Entry<String, JsonNode> entry = routesIter.next();
                String relationIdStr = entry.getKey();
                JsonNode routeJson = entry.getValue();

                Long relationOsmId;
                try {
                    relationOsmId = Long.parseLong(relationIdStr);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping route with non-numeric key: " + relationIdStr);
                    continue;
                }

                // Find route by osmId
                Optional<Route> routeOpt = routeRepo.findByOsmId(relationOsmId);
                if (!routeOpt.isPresent()) {
                    System.out.println("Schedule: route with osmId " + relationOsmId + " not found in DB. Skipping.");
                    continue;
                }
                Route route = routeOpt.get();

                JsonNode stopsNode = routeJson.path("stops");
                if (stopsNode.isArray()) {
                    for (JsonNode stopJson : stopsNode) {
                        Long stopOsmId = stopJson.path("id").asLong();
                        Integer minutes = stopJson.path("arrival_time_from_start_minutes").isNumber()
                                ? stopJson.path("arrival_time_from_start_minutes").asInt()
                                : null;

                        if (minutes == null) {
                            System.out.println("Schedule: missing arrival_time_from_start_minutes for stop " + stopOsmId + " on route " + relationOsmId + ". Skipping offset.");
                            continue;
                        }

                        Optional<Stop> stopOpt = stopRepo.findByOsmId(stopOsmId);
                        if (!stopOpt.isPresent()) {
                            System.out.println("Schedule: stop osmId " + stopOsmId + " not found in DB for route " + relationOsmId + ". Skipping offset creation.");
                            continue;
                        }
                        Stop stop = stopOpt.get();

                        // Upsert RouteStopOffset
                        Optional<RouteStopOffset> maybeOffset = routeStopOffsetRepo.findByRouteAndStop(route, stop);
                        if (maybeOffset.isPresent()) {
                            RouteStopOffset offset = maybeOffset.get();
                            if (!Integer.valueOf(minutes).equals(offset.getCumulativeMinutesFromStart())) {
                                offset.setCumulativeMinutesFromStart(minutes);
                                routeStopOffsetRepo.save(offset);
                            }
                        } else {
                            RouteStopOffset newOffset = new RouteStopOffset();
                            newOffset.setRoute(route);
                            newOffset.setStop(stop);
                            newOffset.setCumulativeMinutesFromStart(minutes);
                            routeStopOffsetRepo.save(newOffset);
                        }
                    }
                } else {
                    System.out.println("Schedule: no stops array for route " + relationOsmId);
                }

                String operatingHours = routeJson.path("operating_hours").asText(null);
                Integer frequencyMinutes = routeJson.path("frequency_minutes").isNumber()
                        ? routeJson.path("frequency_minutes").asInt()
                        : null;

                if (operatingHours == null || frequencyMinutes == null) {
                    System.out.println("Schedule: missing operating_hours or frequency_minutes for route " + relationOsmId + ". Skipping runs creation.");
                    continue;
                }

                LocalTime startTime;
                LocalTime endTime;
                try {
                    String[] parts = operatingHours.split("-");
                    DateTimeFormatter tf = DateTimeFormatter.ofPattern("H:mm"); // accept e.g. 06:00 or 6:00
                    startTime = LocalTime.parse(parts[0].trim(), tf);
                    endTime = LocalTime.parse(parts[1].trim(), tf);
                } catch (Exception ex) {
                    System.out.println("Schedule: could not parse operating_hours '" + operatingHours + "' for route " + relationOsmId + ". Skipping runs.");
                    continue;
                }

                boolean hasAnyRegular = runRepo.existsByRouteAndScheduleType(route, ScheduleType.REGULAR);
                if (hasAnyRegular) {
                    System.out.println("Schedule: route " + relationOsmId + " already has regular runs. Use app.osm.force-refresh-schedules=true to replace. Skipping run creation.");
                    continue;
                }

                Map<Integer, Integer> dayRunNumCounter = new HashMap<>(); // day -> next runNum

                for (int day = 1; day <= 7; day++) {
                    LocalTime t = startTime;
                    int runNum = 0;
                    while (!t.isAfter(endTime)) {
                        runNum++;
                        // Dedup check: exist by route, scheduleType REGULAR, dayOfWeek day, startTime t
                        boolean exists = runRepo.existsByRouteAndScheduleTypeAndDayOfWeekAndStartTime(route, ScheduleType.REGULAR, day, t);
                        if (!exists) {
                            Run run = new Run();
                            run.setRoute(route);
                            run.setDestinationStopName(routeJson.path("to").asText(null)); // optional
                            run.setScheduleType(ScheduleType.REGULAR);
                            run.setDayOfWeek(day);
                            run.setSpecificDate(null);
                            run.setRunNum(runNum);
                            run.setStartTime(t);
                            runRepo.save(run);
                        }
                        t = t.plusMinutes(frequencyMinutes);
                    }
                }

                System.out.println("Schedule: processed route " + relationOsmId + " (" + route.getName() + ")");
            }
        }
    }
}
