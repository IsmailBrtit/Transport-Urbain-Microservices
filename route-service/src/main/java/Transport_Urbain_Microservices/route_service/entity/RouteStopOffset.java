package Transport_Urbain_Microservices.route_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RouteStopId.class)
public class RouteStopOffset {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_route", nullable = false)
    private Route route;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_stop", nullable = false)
    private Stop stop;

    // Fixed cumulative arrival offset from the route's start (in minutes).
    // 0 for the first stop, accumulated based on travel times to subsequent stops.
    @Column(nullable = false)
    private Integer cumulativeMinutesFromStart;
}
