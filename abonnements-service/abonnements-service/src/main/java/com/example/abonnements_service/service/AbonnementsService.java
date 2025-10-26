package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.repository.AbonnementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbonnementsService {

    private final AbonnementRepository abonnementRepository;

    public static List<Abonnement> findAll() {
    }

    public static java.lang.ScopedValue<Object> findById(Long id) {
    }


    public void createAbonnement(AbonnementRequest abonnementRequest) {
        Abonnement abonnement = Abonnement.();

    }

    public void updateAbonnement(Long id, AbonnementRequest abonnementRequest) {
    }

    public void deleteAbonnement(Long id) {
    }

    public List<Abonnement> getActiveAbonnementsByUserId(Long userId) {
    }

    public List<AbonnementResponse> getActiveAbonnementsByUserId(Long userId) {
    }
}
