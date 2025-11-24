package Transport_Urbain_Microservices.route_service.dataloader;

import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.RouteStop;
import Transport_Urbain_Microservices.route_service.entity.Stop;
import Transport_Urbain_Microservices.route_service.repo.RouteRepo;
import Transport_Urbain_Microservices.route_service.repo.RouteStopRepo;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Order(1)
public class OsmDataLoader implements ApplicationRunner {

    @Value("${app.osm-file:classpath:osm-data.json}")
    private Resource osmFile; // configurable path

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final StopRepo stopRepo;
    private final RouteRepo routeRepo;
    private final RouteStopRepo routeStopRepo;


    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try (InputStream is = osmFile.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode elements = root.get("elements");
            if (elements == null || !elements.isArray()) {
                System.out.println("No elements array found in OSM file");
                return;
            }

            // 1) index all nodes by OSM id
            Map<Long, JsonNode> nodeByOsmId = new HashMap<>();
            for (JsonNode el : elements) {
                if ("node".equals(el.path("type").asText())) {
                    long osmId = el.path("id").asLong();
                    nodeByOsmId.put(osmId, el);
                }
            }

            // 2) create or find stops for each node (persist osmId in DB)
            Map<Long, Stop> osmToStop = new HashMap<>();
            for (Map.Entry<Long, JsonNode> entry : nodeByOsmId.entrySet()) {
                Long osmId = entry.getKey();
                JsonNode node = entry.getValue();

                double lat = node.path("lat").asDouble();
                double lon = node.path("lon").asDouble();
                String name = node.path("tags").path("name").asText(null);
                String address = node.path("tags").path("addr:full").asText(null);

                Optional<Stop> existing = stopRepo.findByOsmId(osmId);
                Stop stop;
                if (existing.isPresent()) {
                    stop = existing.get();
                } else {
                    stop = new Stop();
                    stop.setOsmId(osmId);
                    stop.setName(name != null ? name : "stop-" + osmId);
                    stop.setLat(lat);
                    stop.setLon(lon);
                    stop.setAddress(address);
                    stop = stopRepo.save(stop);
                }
                osmToStop.put(osmId, stop);
            }

            // 3) iterate relations and build routes
            for (JsonNode el : elements) {
                if (!"relation".equals(el.path("type").asText())) continue;

                JsonNode tags = el.path("tags");
                if (tags.isMissingNode()) continue;

                // only bus routes (but you can remove this check to load other route types)
                String routeType = tags.path("route").asText(null);
                if (!"bus".equals(routeType)) continue;

                Long relationOsmId = el.path("id").asLong();
                String routeName = tags.path("name").asText(null);
                String ref = tags.path("ref").asText(null);

                Optional<Route> existingRouteOpt = routeRepo.findByOsmId(relationOsmId);

                if (!existingRouteOpt.isPresent()) {
                    Route route = new Route();
                    route.setOsmId(relationOsmId);
                    route.setName(routeName != null ? routeName : "route-" + el.path("id").asText());
                    route.setNum(ref != null ? ref : (routeName != null ? routeName : "unknown"));
                    route.setDescription(buildDescriptionFromTags(tags));
                    route = routeRepo.save(route);

                    // create route stops in order of members
                    JsonNode members = el.path("members");
                    if (members.isArray()) {
                        int order = 0;
                        for (JsonNode member : members) {
                            if (!"node".equals(member.path("type").asText())) continue;
                            long refOsmId = member.path("ref").asLong();
                            Stop stop = osmToStop.get(refOsmId);
                            if (stop == null) {
                                // node not found among elements; skip or log
                                System.out.println("Warning: member node " + refOsmId + " not found as element; skipping");
                                continue;
                            }
                            order++;
                            RouteStop routeStop = new RouteStop();
                            routeStop.setRoute(route);
                            routeStop.setStop(stop);
                            routeStop.setStopOrder(order);
                            routeStopRepo.save(routeStop);
                        }
                    }
                }
            }
            System.out.println("OSM import finished. Stops: " + osmToStop.size());
        }
    }

    private String buildDescriptionFromTags(JsonNode tags) {
        String from = tags.path("from").asText(null);
        String to = tags.path("to").asText(null);
        if (from != null || to != null) {
            return (from != null ? "From " + from : "") + (from != null && to != null ? " to " : "") + (to != null ? to : "");
        }
        return tags.path("description").asText(null);
    }
}
