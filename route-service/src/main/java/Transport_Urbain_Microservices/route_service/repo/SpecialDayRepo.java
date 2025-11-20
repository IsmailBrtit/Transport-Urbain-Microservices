package Transport_Urbain_Microservices.route_service.repo;

import Transport_Urbain_Microservices.route_service.entity.SpecialDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SpecialDayRepo extends JpaRepository<SpecialDay, Integer> {
    boolean existsByDate(LocalDate date);
}
