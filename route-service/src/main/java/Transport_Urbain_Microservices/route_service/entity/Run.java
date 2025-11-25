package Transport_Urbain_Microservices.route_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    private String destinationStopName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType scheduleType; //determines whether the run is regular or for a special date

    private Integer dayOfWeek; // 1-7, NULL if special

    private LocalDate specificDate; // NULL if regular

    @Column(nullable = false)
    private Integer runNum;  //this is the number of the run for that day

    @Column(nullable = false)
    private LocalTime startTime;

    @PrePersist
    @PreUpdate
    private void validateScheduleType() {
        if (scheduleType == ScheduleType.REGULAR && dayOfWeek == null) {
            throw new IllegalStateException("dayOfWeek must be set for REGULAR schedule type");
        }
        if (scheduleType == ScheduleType.SPECIAL && specificDate == null) {
            throw new IllegalStateException("specificDate must be set for SPECIAL schedule type");
        }
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 and 7");
        }
    }
}
