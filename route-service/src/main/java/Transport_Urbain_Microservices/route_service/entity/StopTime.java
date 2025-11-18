package Transport_Urbain_Microservices.route_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(nullable = false)
    private Integer arrivalMinuteFromStart;

    /**
     * Helper method to calculate actual arrival time
     */
    public String getActualArrivalTime() {
        if (run == null || run.getStartTime() == null) {
            return null;
        }
        return run.getStartTime()
                .plusMinutes(arrivalMinuteFromStart)
                .toString();
    }
}
