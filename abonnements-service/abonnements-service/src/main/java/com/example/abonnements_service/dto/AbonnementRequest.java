package com.example.abonnements_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AbonnementRequest {

    private String nomClient;
    private String emailClient;
    private Long forfaitId;

}
