package com.example.abonnements_service.service;

import com.example.abonnements_service.dto.ForfaitRequest;
import com.example.abonnements_service.dto.ForfaitResponse;
import com.example.abonnements_service.exception.ResourceNotFoundException;
import com.example.abonnements_service.model.Devise;
import com.example.abonnements_service.model.Forfait;
import com.example.abonnements_service.repository.ForfaitRepository;
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
 * Unit tests for ForfaitService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ForfaitService Unit Tests")
class ForfaitServiceTest {

    @Mock
    private ForfaitRepository forfaitRepository;

    @InjectMocks
    private ForfaitService forfaitService;

    private UUID testForfaitId;
    private Forfait testForfait;
    private ForfaitRequest testRequest;

    @BeforeEach
    void setUp() {
        testForfaitId = UUID.randomUUID();

        testForfait = Forfait.builder()
                .id(testForfaitId)
                .nom("Mensuel Standard")
                .duree("30 jours")
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .description("Abonnement mensuel")
                .actif(true)
                .createLe(LocalDateTime.now())
                .build();

        testRequest = ForfaitRequest.builder()
                .nom("Mensuel Standard")
                .duree("30 jours")
                .prix(BigDecimal.valueOf(200.00))
                .devise(Devise.MAD)
                .description("Abonnement mensuel")
                .actif(true)
                .build();
    }

    @Test
    @DisplayName("Should create forfait successfully")
    void shouldCreateForfaitSuccessfully() {
        // Given
        when(forfaitRepository.existsByNom("Mensuel Standard")).thenReturn(false);
        when(forfaitRepository.save(any(Forfait.class))).thenReturn(testForfait);

        // When
        ForfaitResponse response = forfaitService.createForfait(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("Mensuel Standard");
        assertThat(response.getPrix()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(response.getDevise()).isEqualTo(Devise.MAD);
        assertThat(response.getActif()).isTrue();

        verify(forfaitRepository, times(1)).existsByNom("Mensuel Standard");
        verify(forfaitRepository, times(1)).save(any(Forfait.class));
    }

    @Test
    @DisplayName("Should throw exception when creating forfait with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateForfait() {
        // Given
        when(forfaitRepository.existsByNom("Mensuel Standard")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> forfaitService.createForfait(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forfait with name 'Mensuel Standard' already exists");

        verify(forfaitRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find forfait by id")
    void shouldFindForfaitById() {
        // Given
        when(forfaitRepository.findById(testForfaitId)).thenReturn(Optional.of(testForfait));

        // When
        Optional<ForfaitResponse> response = forfaitService.findById(testForfaitId);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(testForfaitId);
        assertThat(response.get().getNom()).isEqualTo("Mensuel Standard");

        verify(forfaitRepository, times(1)).findById(testForfaitId);
    }

    @Test
    @DisplayName("Should return empty when forfait not found")
    void shouldReturnEmptyWhenForfaitNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(forfaitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<ForfaitResponse> response = forfaitService.findById(nonExistentId);

        // Then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Should get forfait entity by id")
    void shouldGetForfaitEntityById() {
        // Given
        when(forfaitRepository.findById(testForfaitId)).thenReturn(Optional.of(testForfait));

        // When
        Forfait forfait = forfaitService.getForfaitEntityById(testForfaitId);

        // Then
        assertThat(forfait).isNotNull();
        assertThat(forfait.getId()).isEqualTo(testForfaitId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent forfait entity")
    void shouldThrowExceptionWhenGettingNonExistentEntity() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(forfaitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> forfaitService.getForfaitEntityById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Forfait not found");
    }

    @Test
    @DisplayName("Should find all forfaits")
    void shouldFindAllForfaits() {
        // Given
        List<Forfait> forfaits = Arrays.asList(testForfait, testForfait);
        when(forfaitRepository.findAll()).thenReturn(forfaits);

        // When
        List<ForfaitResponse> responses = forfaitService.findAll();

        // Then
        assertThat(responses).hasSize(2);
        verify(forfaitRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find all active forfaits")
    void shouldFindAllActiveForfaits() {
        // Given
        List<Forfait> activeForfaits = Arrays.asList(testForfait);
        when(forfaitRepository.findByActifTrue()).thenReturn(activeForfaits);

        // When
        List<ForfaitResponse> responses = forfaitService.findAllActive();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getActif()).isTrue();
        verify(forfaitRepository, times(1)).findByActifTrue();
    }

    @Test
    @DisplayName("Should update forfait successfully")
    void shouldUpdateForfaitSuccessfully() {
        // Given
        when(forfaitRepository.findById(testForfaitId)).thenReturn(Optional.of(testForfait));
        when(forfaitRepository.save(any(Forfait.class))).thenReturn(testForfait);

        ForfaitRequest updateRequest = ForfaitRequest.builder()
                .prix(BigDecimal.valueOf(250.00))
                .build();

        // When
        ForfaitResponse response = forfaitService.updateForfait(testForfaitId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(forfaitRepository, times(1)).findById(testForfaitId);
        verify(forfaitRepository, times(1)).save(any(Forfait.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent forfait")
    void shouldThrowExceptionWhenUpdatingNonExistentForfait() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(forfaitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> forfaitService.updateForfait(nonExistentId, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Forfait not found");

        verify(forfaitRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating to duplicate name")
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        // Given
        when(forfaitRepository.findById(testForfaitId)).thenReturn(Optional.of(testForfait));
        when(forfaitRepository.existsByNom("New Name")).thenReturn(true);

        ForfaitRequest updateRequest = ForfaitRequest.builder()
                .nom("New Name")
                .build();

        // When & Then
        assertThatThrownBy(() -> forfaitService.updateForfait(testForfaitId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forfait with name 'New Name' already exists");

        verify(forfaitRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft delete forfait")
    void shouldSoftDeleteForfait() {
        // Given
        when(forfaitRepository.findById(testForfaitId)).thenReturn(Optional.of(testForfait));

        // When
        forfaitService.deleteForfait(testForfaitId);

        // Then
        verify(forfaitRepository, times(1)).save(argThat(forfait ->
                !forfait.getActif()
        ));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent forfait")
    void shouldThrowExceptionWhenDeletingNonExistentForfait() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(forfaitRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> forfaitService.deleteForfait(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Forfait not found");

        verify(forfaitRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should use default values when creating forfait")
    void shouldUseDefaultValuesWhenCreating() {
        // Given
        ForfaitRequest minimalRequest = ForfaitRequest.builder()
                .nom("Minimal Forfait")
                .duree("30 jours")
                .prix(BigDecimal.valueOf(100.00))
                .build();

        Forfait savedForfait = Forfait.builder()
                .id(UUID.randomUUID())
                .nom("Minimal Forfait")
                .duree("30 jours")
                .prix(BigDecimal.valueOf(100.00))
                .devise(Devise.MAD)  // Default
                .actif(true)  // Default
                .build();

        when(forfaitRepository.existsByNom("Minimal Forfait")).thenReturn(false);
        when(forfaitRepository.save(any(Forfait.class))).thenReturn(savedForfait);

        // When
        ForfaitResponse response = forfaitService.createForfait(minimalRequest);

        // Then
        assertThat(response.getDevise()).isEqualTo(Devise.MAD);
        assertThat(response.getActif()).isTrue();
    }
}
