package org.dsoft.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroqRequestDTO {
    private String model;
    private List<GroqMessageDTO> messages;
    private double temperature;
    @JsonProperty("max_tokens") private int maxTokens;
}