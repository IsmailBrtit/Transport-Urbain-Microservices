package Transport_Urbain_Microservices.route_service.dataloader;

import Transport_Urbain_Microservices.route_service.entity.*;
import Transport_Urbain_Microservices.route_service.repo.RouteRepo;
import Transport_Urbain_Microservices.route_service.repo.RunRepo;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import Transport_Urbain_Microservices.route_service.repo.StopTimeRepo;
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
    private final StopTimeRepo stopTimeRepo;

    // Default: create regular runs for all days (1..7). Change if you want fewer days.
    private static final int[] DEFAULT_DAYS = {1,2,3,4,5,6,7};

    public ScheduleDataLoader(RouteRepo routeRepo,
                              StopRepo stopRepo,
                              RunRepo runRepo,
                              StopTimeRepo stopTimeRepo) {
        this.routeRepo = routeRepo;
        this.stopRepo = stopRepo;
        this.runRepo = runRepo;
        this.stopTimeRepo = stopTimeRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try (InputStream is = scheduleFile.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode routesNode = root.path("routes");
            if (routesNode.isMissingNode()) {
                System.out.println("No 'routes' node found in schedule file.");
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
                } catch (NumberFormatException ex) {
                    System.out.println("Skipping route key that is not a number: " + relationIdStr);
                    continue;
                }

                Optional<Route> routeOpt = routeRepo.findByOsmId(relationOsmId);
                if (!routeOpt.isPresent()) {
                    System.out.println("Route with osmId=" + relationOsmId + " not found in DB — skipping schedule for it.");
                    continue;
                }
                Route route = routeOpt.get();

                // parse operating_hours and frequency_minutes
                String opHours = routeJson.path("operating_hours").asText(null); // "06:00-22:00"
                int frequencyMinutes = routeJson.path("frequency_minutes").asInt(-1);
                if (opHours == null || frequencyMinutes <= 0) {
                    System.out.println("Route " + relationOsmId + " missing operating_hours or frequency_minutes — skipping.");
                    continue;
                }

                String[] parts = opHours.split("-");
                if (parts.length != 2) {
                    System.out.println("Invalid operating_hours format for route " + relationOsmId + ": " + opHours);
                    continue;
                }
                LocalTime firstDeparture;
                LocalTime lastDeparture;
                try {
                    firstDeparture = LocalTime.parse(parts[0].trim());
                    lastDeparture = LocalTime.parse(parts[1].trim());
                } catch (Exception e) {
                    System.out.println("Failed to parse times for route " + relationOsmId + ": " + e.getMessage());
                    continue;
                }

                JsonNode stopsArray = routeJson.path("stops");
                if (!stopsArray.isArray() || stopsArray.size() == 0) {
                    System.out.println("No stops listed for route " + relationOsmId + " — skipping.");
                    continue;
                }

                // We'll generate runs for each configured day (default 1..7)
                for (int day : DEFAULT_DAYS) {
                    LocalTime departure = firstDeparture;
                    int runIndex = 0;
                    while (!departure.isAfter(lastDeparture)) {
                        runIndex++;
                        // Check if run already exists (avoid duplication)
                        Optional<Run> existingRun = runRepo.findByRouteAndScheduleTypeAndDayOfWeekAndStartTime(
                                route, ScheduleType.REGULAR, day, departure);

                        if (existingRun.isPresent()) {
                            // skip: do not touch existing run or its stopTimes
                            System.out.println(String.format("Skipping existing run for route=%d day=%d time=%s",
                                    route.getId(), day, departure));
                        } else {
                            // create new Run
                            Run run = new Run();
                            run.setRoute(route);
                            run.setDestinationStopName(routeJson.path("to").asText(null));
                            run.setScheduleType(ScheduleType.REGULAR);
                            run.setDayOfWeek(day);
                            run.setSpecificDate(null);
                            run.setRunNum(runIndex);
                            run.setStartTime(departure);
                            run = runRepo.save(run);

                            // create StopTimes for this run
                            int createdStops = createStopTimesForRun(run, stopsArray);
                            System.out.println(String.format("Created run id=%d for route=%d day=%d time=%s (%d stopTimes)",
                                    run.getId(), route.getId(), day, departure, createdStops));
                        }

                        departure = departure.plusMinutes(frequencyMinutes);
                    }
                }
            }

            System.out.println("Schedule import finished.");
        }
    }

    private int createStopTimesForRun(Run run, JsonNode stopsArray) {
        int count = 0;
        for (JsonNode stopJson : stopsArray) {
            long stopOsmId = stopJson.path("id").asLong(-1);
            if (stopOsmId == -1) continue;

            Optional<Stop> stopOpt = stopRepo.findByOsmId(stopOsmId);
            if (!stopOpt.isPresent()) {
                // stop isn't in DB — log and skip. Optionally auto-create here.
                System.out.println("Stop with osmId=" + stopOsmId + " not found in DB; skipping StopTime.");
                continue;
            }
            Stop stop = stopOpt.get();

            int arrivalMinute = stopJson.path("arrival_time_from_start_minutes").asInt(0);

            StopTime st = new StopTime();
            st.setRun(run);
            st.setStop(stop);
            st.setArrivalMinuteFromStart(arrivalMinute);
            stopTimeRepo.save(st);
            count++;
        }
        return count;
    }
}
