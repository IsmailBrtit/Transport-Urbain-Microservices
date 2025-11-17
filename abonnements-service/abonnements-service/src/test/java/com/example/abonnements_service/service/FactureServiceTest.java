package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.FactureResponse;
import com.example.abonnements_service.exception.ResourceNotFoundException;
import com.example.abonnements_service.model.Abonnement;
import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.Facture;
import com.example.abonnements_service.model.StatutFacture;
import com.example.abonnements_service.repository.FactureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FactureService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FactureService Unit Tests")
class FactureServiceTest {

    @Mock
    private FactureRepository factureRepository;

    @Mock
    private com.example.abonnements_service.repository.AbonnementRepository abonnementRepository;

    @InjectMocks
    private FactureService factureService;

    private UUID testFactureId;
    private UUID testAbonnementId;
    private Facture testFacture;
    private Abonnement testAbonnement;

    @BeforeEach
    void setUp() {
        testFactureId = UUID.randomUUID();
        testAbonnementId = UUID.randomUUID();

        testAbonnement = Abonnement.builder()
                .id(testAbonnementId)
                .utilisateurId(UUID.randomUUID())
                .forfaitId(UUID.randomUUID())
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .build();

        testFacture = Facture.builder()
                .id(testFactureId)
                .abonnementId(testAbonnementId)
                .montant(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .numeroFacture("FAC-2025-00123")
                .statut(StatutFacture.EN_ATTENTE)
                .emissLe(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should generate facture successfully")
    void shouldGenerateFactureSuccessfully() {
        // Given
        when(factureRepository.findByAbonnementId(testAbonnementId)).thenReturn(Optional.empty());
        when(factureRepository.save(any(Facture.class))).thenReturn(testFacture);

        // When
        FactureResponse response = factureService.genererFacture(testAbonnement);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAbonnementId()).isEqualTo(testAbonnementId);
        assertThat(response.getMontant()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(response.getDevise()).isEqualTo(Devise.MAD);
        assertThat(response.getStatut()).isEqualTo(StatutFacture.EN_ATTENTE);
        assertThat(response.getNumeroFacture()).isEqualTo("FAC-2025-00123");

        verify(factureRepository, times(1)).findByAbonnementId(testAbonnementId);
        verify(factureRepository, times(1)).save(any(Facture.class));
    }

    @Test
    @DisplayName("Should return existing facture if already exists")
    void shouldReturnExistingFactureIfAlreadyExists() {
        // Given
        when(factureRepository.findByAbonnementId(testAbonnementId)).thenReturn(Optional.of(testFacture));

        // When
        FactureResponse response = factureService.genererFacture(testAbonnement);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testFactureId);

        verify(factureRepository, times(1)).findByAbonnementId(testAbonnementId);
        verify(factureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find facture by id")
    void shouldFindFactureById() {
        // Given
        when(factureRepository.findById(testFactureId)).thenReturn(Optional.of(testFacture));

        // When
        Optional<FactureResponse> response = factureService.findById(testFactureId);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(testFactureId);
        assertThat(response.get().getNumeroFacture()).isEqualTo("FAC-2025-00123");

        verify(factureRepository, times(1)).findById(testFactureId);
    }

    @Test
    @DisplayName("Should return empty when facture not found")
    void shouldReturnEmptyWhenFactureNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(factureRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<FactureResponse> response = factureService.findById(nonExistentId);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Should find all factures")
    void shouldFindAllFactures() {
        // Given
        List<Facture> factures = Arrays.asList(testFacture, testFacture);
        when(factureRepository.findAll()).thenReturn(factures);

        // When
        List<FactureResponse> responses = factureService.findAll();

        // Then
        assertThat(responses).hasSize(2);
        verify(factureRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find facture by abonnement id")
    void shouldFindFactureByAbonnementId() {
        // Given
        when(factureRepository.findByAbonnementId(testAbonnementId)).thenReturn(Optional.of(testFacture));

        // When
        Optional<FactureResponse> response = factureService.findByAbonnementId(testAbonnementId);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getAbonnementId()).isEqualTo(testAbonnementId);

        verify(factureRepository, times(1)).findByAbonnementId(testAbonnementId);
    }

    @Test
    @DisplayName("Should find factures by multiple abonnement ids")
    void shouldFindFacturesByMultipleAbonnementIds() {
        // Given
        UUID abonnementId1 = UUID.randomUUID();
        UUID abonnementId2 = UUID.randomUUID();
        List<UUID> abonnementIds = Arrays.asList(abonnementId1, abonnementId2);

        List<Facture> factures = Arrays.asList(testFacture, testFacture);
        when(factureRepository.findByAbonnementIdIn(abonnementIds)).thenReturn(factures);

        // When
        List<FactureResponse> responses = factureService.findByAbonnementIds(abonnementIds);

        // Then
        assertThat(responses).hasSize(2);
        verify(factureRepository, times(1)).findByAbonnementIdIn(abonnementIds);
    }

    @Test
    @DisplayName("Should update facture status successfully")
    void shouldUpdateFactureStatusSuccessfully() {
        // Given
        when(factureRepository.findById(testFactureId)).thenReturn(Optional.of(testFacture));

        Facture updatedFacture = Facture.builder()
                .id(testFactureId)
                .abonnementId(testAbonnementId)
                .montant(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .numeroFacture("FAC-2025-00123")
                .statut(StatutFacture.PAYEE)
                .emissLe(LocalDateTime.now())
                .build();

        when(factureRepository.save(any(Facture.class))).thenReturn(updatedFacture);

        // When
        FactureResponse response = factureService.updateStatut(testFactureId, StatutFacture.PAYEE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatut()).isEqualTo(StatutFacture.PAYEE);

        verify(factureRepository, times(1)).findById(testFactureId);
        verify(factureRepository, times(1)).save(any(Facture.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent facture")
    void shouldThrowExceptionWhenUpdatingNonExistentFacture() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(factureRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> factureService.updateStatut(nonExistentId, StatutFacture.PAYEE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Facture not found");

        verify(factureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle different statut transitions")
    void shouldHandleDifferentStatutTransitions() {
        // Given
        when(factureRepository.findById(testFactureId)).thenReturn(Optional.of(testFacture));
        when(factureRepository.save(any(Facture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Test EN_ATTENTE -> PAYEE
        FactureResponse response1 = factureService.updateStatut(testFactureId, StatutFacture.PAYEE);
        assertThat(response1.getStatut()).isEqualTo(StatutFacture.PAYEE);

        // Test PAYEE -> ANNULEE
        FactureResponse response2 = factureService.updateStatut(testFactureId, StatutFacture.ANNULEE);
        assertThat(response2.getStatut()).isEqualTo(StatutFacture.ANNULEE);

        verify(factureRepository, times(2)).save(any(Facture.class));
    }
}
