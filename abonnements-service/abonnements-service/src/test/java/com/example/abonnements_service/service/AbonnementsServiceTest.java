package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.AbonnementRequest;
import com.example.abonnements_service.dto.AbonnementResponse;
import com.example.abonnements_service.dto.FactureResponse;
import com.example.abonnements_service.exception.ResourceNotFoundException;
import com.example.abonnements_service.model.*;
import com.example.abonnements_service.repository.AbonnementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AbonnementsService
 * Tests business logic in isolation using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AbonnementsService Unit Tests")
class AbonnementsServiceTest {

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private ForfaitService forfaitService;

    @Mock
    private FactureService factureService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AbonnementsService abonnementsService;

    private UUID testUserId;
    private UUID testForfaitId;
    private UUID testAbonnementId;
    private Forfait testForfait;
    private Abonnement testAbonnement;
    private AbonnementRequest testRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testForfaitId = UUID.randomUUID();
        testAbonnementId = UUID.randomUUID();

        // Create test forfait
        testForfait = Forfait.builder()
                .id(testForfaitId)
                .nom("Mensuel Standard")
                .duree("30 jours")
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .description("Test forfait")
                .actif(true)
                .build();

        // Create test abonnement
        testAbonnement = Abonnement.builder()
                .id(testAbonnementId)
                .utilisateurId(testUserId)
                .forfaitId(testForfaitId)
                .dateDebut(LocalDate.of(2025, 1, 1))
                .dateFin(LocalDate.of(2025, 12, 31))
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .statut(StatutAbonnement.ACTIVE)
                .build();

        // Create test request
        testRequest = AbonnementRequest.builder()
                .utilisateurId(testUserId)
                .forfaitId(testForfaitId)
                .dateDebut(LocalDate.of(2025, 1, 1))
                .dateFin(LocalDate.of(2025, 12, 31))
                .statut(StatutAbonnement.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should create abonnement successfully")
    void shouldCreateAbonnementSuccessfully() {
        // Given
        when(forfaitService.getForfaitEntityById(testForfaitId)).thenReturn(testForfait);
        when(abonnementRepository.save(any(Abonnement.class))).thenReturn(testAbonnement);
        when(factureService.genererFacture(any(Abonnement.class)))
                .thenReturn(FactureResponse.builder().numeroFacture("FAC-2025-00001").build());

        // When
        AbonnementResponse response = abonnementsService.createAbonnement(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testAbonnementId);
        assertThat(response.getUtilisateurId()).isEqualTo(testUserId);
        assertThat(response.getForfaitId()).isEqualTo(testForfaitId);
        assertThat(response.getPrix()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(response.getDevise()).isEqualTo(Devise.MAD);
        assertThat(response.getStatut()).isEqualTo(StatutAbonnement.ACTIVE);

        // Verify interactions
        verify(forfaitService, times(1)).getForfaitEntityById(testForfaitId);
        verify(abonnementRepository, times(1)).save(any(Abonnement.class));
        verify(factureService, times(1)).genererFacture(any(Abonnement.class));
        verify(kafkaProducerService, times(1)).publishAbonnementEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when forfait is inactive")
    void shouldThrowExceptionWhenForfaitIsInactive() {
        // Given
        testForfait.setActif(false);
        when(forfaitService.getForfaitEntityById(testForfaitId)).thenReturn(testForfait);

        // When & Then
        assertThatThrownBy(() -> abonnementsService.createAbonnement(testRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forfait is not active");

        verify(abonnementRepository, never()).save(any());
        verify(factureService, never()).genererFacture(any());
    }

    @Test
    @DisplayName("Should throw exception when dates are invalid")
    void shouldThrowExceptionWhenDatesAreInvalid() {
        // Given - end date before start date
        testRequest.setDateDebut(LocalDate.of(2025, 12, 31));
        testRequest.setDateFin(LocalDate.of(2025, 1, 1));

        // When & Then
        assertThatThrownBy(() -> abonnementsService.createAbonnement(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must be after start date");

        verify(abonnementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find abonnement by id")
    void shouldFindAbonnementById() {
        // Given
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));

        // When
        Optional<AbonnementResponse> response = abonnementsService.findById(testAbonnementId);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(testAbonnementId);
        assertThat(response.get().getUtilisateurId()).isEqualTo(testUserId);

        verify(abonnementRepository, times(1)).findById(testAbonnementId);
    }

    @Test
    @DisplayName("Should return empty when abonnement not found")
    void shouldReturnEmptyWhenAbonnementNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(abonnementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<AbonnementResponse> response = abonnementsService.findById(nonExistentId);

        // Then
        assertThat(response).isEmpty();
        verify(abonnementRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should find all abonnements")
    void shouldFindAllAbonnements() {
        // Given
        List<Abonnement> abonnements = Arrays.asList(testAbonnement, testAbonnement);
        when(abonnementRepository.findAll()).thenReturn(abonnements);

        // When
        List<AbonnementResponse> responses = abonnementsService.findAll();

        // Then
        assertThat(responses).hasSize(2);
        verify(abonnementRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update abonnement successfully")
    void shouldUpdateAbonnementSuccessfully() {
        // Given
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));
        when(abonnementRepository.save(any(Abonnement.class))).thenReturn(testAbonnement);

        AbonnementRequest updateRequest = AbonnementRequest.builder()
                .dateFin(LocalDate.of(2026, 1, 1))
                .build();

        // When
        AbonnementResponse response = abonnementsService.updateAbonnement(testAbonnementId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(abonnementRepository, times(1)).findById(testAbonnementId);
        verify(abonnementRepository, times(1)).save(any(Abonnement.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent abonnement")
    void shouldThrowExceptionWhenUpdatingNonExistentAbonnement() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(abonnementRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> abonnementsService.updateAbonnement(nonExistentId, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Abonnement not found");

        verify(abonnementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating expired abonnement")
    void shouldThrowExceptionWhenUpdatingExpiredAbonnement() {
        // Given
        testAbonnement.setStatut(StatutAbonnement.EXPIRED);
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));

        // When & Then
        assertThatThrownBy(() -> abonnementsService.updateAbonnement(testAbonnementId, testRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update an expired subscription");

        verify(abonnementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel abonnement successfully")
    void shouldCancelAbonnementSuccessfully() {
        // Given
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));
        when(forfaitService.getForfaitEntityById(testForfaitId)).thenReturn(testForfait);

        // When
        abonnementsService.deleteAbonnement(testAbonnementId);

        // Then
        verify(abonnementRepository, times(1)).save(argThat(abonnement ->
                abonnement.getStatut() == StatutAbonnement.CANCELED
        ));
        verify(kafkaProducerService, times(1)).publishAbonnementEvent(any());
    }

    @Test
    @DisplayName("Should get active abonnements by user")
    void shouldGetActiveAbonnementsByUser() {
        // Given
        List<Abonnement> activeAbonnements = Arrays.asList(testAbonnement);
        when(abonnementRepository.findByUtilisateurIdAndStatut(testUserId, StatutAbonnement.ACTIVE))
                .thenReturn(activeAbonnements);

        // When
        List<AbonnementResponse> responses = abonnementsService.getActiveAbonnementsByUserId(testUserId);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatut()).isEqualTo(StatutAbonnement.ACTIVE);
        verify(abonnementRepository, times(1)).findByUtilisateurIdAndStatut(testUserId, StatutAbonnement.ACTIVE);
    }

    @Test
    @DisplayName("Should renew abonnement successfully")
    void shouldRenewAbonnementSuccessfully() {
        // Given
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));
        when(forfaitService.getForfaitEntityById(testForfaitId)).thenReturn(testForfait);

        Abonnement renewedAbonnement = Abonnement.builder()
                .id(UUID.randomUUID())
                .utilisateurId(testUserId)
                .forfaitId(testForfaitId)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .dateFin(LocalDate.of(2026, 12, 31))
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .statut(StatutAbonnement.ACTIVE)
                .build();

        when(abonnementRepository.save(any(Abonnement.class))).thenReturn(renewedAbonnement);
        when(factureService.genererFacture(any(Abonnement.class)))
                .thenReturn(FactureResponse.builder().numeroFacture("FAC-2025-00002").build());

        // When
        AbonnementResponse response = abonnementsService.renouvelerAbonnement(testAbonnementId);

        // Then
        assertThat(response).isNotNull();
        verify(abonnementRepository, times(1)).save(any(Abonnement.class));
        verify(factureService, times(1)).genererFacture(any(Abonnement.class));
        verify(kafkaProducerService, times(1)).publishAbonnementEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when renewing with inactive forfait")
    void shouldThrowExceptionWhenRenewingWithInactiveForfait() {
        // Given
        when(abonnementRepository.findById(testAbonnementId)).thenReturn(Optional.of(testAbonnement));
        testForfait.setActif(false);
        when(forfaitService.getForfaitEntityById(testForfaitId)).thenReturn(testForfait);

        // When & Then
        assertThatThrownBy(() -> abonnementsService.renouvelerAbonnement(testAbonnementId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forfait is no longer active");

        verify(abonnementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate dates correctly")
    void shouldValidateDatesCorrectly() {
        // Given - dates are equal
        testRequest.setDateDebut(LocalDate.of(2025, 1, 1));
        testRequest.setDateFin(LocalDate.of(2025, 1, 1));

        // When & Then
        assertThatThrownBy(() -> abonnementsService.createAbonnement(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must be different from start date");
    }

    @Test
    @DisplayName("Should throw exception when dates are null")
    void shouldThrowExceptionWhenDatesAreNull() {
        // Given
        testRequest.setDateDebut(null);
        testRequest.setDateFin(null);

        // When & Then
        assertThatThrownBy(() -> abonnementsService.createAbonnement(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date and end date are required");
    }
}
