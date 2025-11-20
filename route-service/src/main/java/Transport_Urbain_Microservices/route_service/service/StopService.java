package Transport_Urbain_Microservices.route_service.service;

import Transport_Urbain_Microservices.route_service.dto.StopDto;
import Transport_Urbain_Microservices.route_service.entity.Route;
import Transport_Urbain_Microservices.route_service.entity.Stop;
import Transport_Urbain_Microservices.route_service.mapper.StopMapper;
import Transport_Urbain_Microservices.route_service.repo.StopRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StopService {

    private final StopRepo stopRepo;

    @Transactional
    public StopDto createStop(StopDto stopDto) {
        Stop newStop = new Stop();
        newStop.setName(stopDto.getName());
        newStop.setAddress(stopDto.getAddress());
        newStop.setLat(stopDto.getLat());
        newStop.setLon(stopDto.getLon());
        return StopMapper.toDto(stopRepo.save(newStop));
    }

    @Transactional
    public StopDto updateStop(StopDto stopDto) {
        Stop existingStop = stopRepo.findById(stopDto.getId()).orElseThrow(
                ()->{return new RuntimeException("Stop with id "+stopDto.getId()+" not found");}
        );
        existingStop.setName(stopDto.getName());
        existingStop.setAddress(stopDto.getAddress());
        existingStop.setLat(stopDto.getLat());
        existingStop.setLon(stopDto.getLon());
        return StopMapper.toDto(stopRepo.save(existingStop));
    }

    public StopDto getStopById(Long id) {
        Stop existingStop = stopRepo.findById(id).orElseThrow(
                ()->{return new RuntimeException("Stop with id "+id+" not found");}
        );
        return StopMapper.toDto(existingStop);
    }

    public List<StopDto> getAllStops() {
        List<Stop> stops = stopRepo.findAll();
        return stops.stream().map(StopMapper::toDto).toList();
    }

    public void deleteStopById(Long id) {
        try {
            stopRepo.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Cannot delete stop because it is used by one or more routes.", ex);
        }
    }



}
