package org.dsoft.client;

import org.dsoft.entity.dto.GroqRequestDTO;
import org.dsoft.entity.dto.GroqResponseDTO;
import org.dsoft.entity.dto.GroqMessageDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Client for Groq's hosted LLaMA 3 model.
 * Free tier: 50K tokens/day
 */
@ApplicationScoped
public class GroqClient {
    
    private static final Logger logger = LoggerFactory.getLogger(GroqClient.class);
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final double TEMPERATURE = 0.3;
    private static final int MAX_TOKENS = 512;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @ConfigProperty(name = "groq.api.key", defaultValue = "")
    String groqApiKey;
    
    public String generateResponse(String userMessage) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            logger.warn("Groq API key not configured");
            return "";
        }
        
        try {
            GroqMessageDTO message = new GroqMessageDTO("user", userMessage);
            GroqRequestDTO request = new GroqRequestDTO(MODEL, List.of(message), TEMPERATURE, MAX_TOKENS);
            
            // Serialize DTO to JSON string
            String requestJson = mapper.writeValueAsString(request);
            
            Response response = ClientBuilder.newClient()
                .target(GROQ_API_URL)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + groqApiKey)
                .post(Entity.entity(requestJson, MediaType.APPLICATION_JSON), Response.class);
            
            if (response.getStatus() == 200) {
                // Read response as string and deserialize with ObjectMapper
                String responseBody = response.readEntity(String.class);
                GroqResponseDTO groqResponse = mapper.readValue(responseBody, GroqResponseDTO.class);
                
                if (groqResponse != null && 
                    groqResponse.getChoices() != null && 
                    !groqResponse.getChoices().isEmpty()) {
                    
                    String content = groqResponse.getChoices().get(0).getMessage().getContent();
                    return content != null ? content.trim() : "";
                } else {
                    logger.warn("Groq API returned empty choices");
                    return "";
                }
            } else {
                String errorBody = response.readEntity(String.class);
                logger.error("Groq API error status: {} - Body: {}", response.getStatus(), errorBody);
                return "";
            }
        } catch (Exception e) {
            logger.error("Error calling Groq API: {}", e.getMessage(), e);
            return "";
        }
    }
    
    public boolean isAvailable() {
        try {
            if (groqApiKey == null || groqApiKey.isBlank()) {
                return false;
            }
            
            String testResponse = generateResponse("test");
            return !testResponse.isEmpty();
        } catch (Exception e) {
            logger.error("Groq API unavailable: {}", e.getMessage());
            return false;
        }
    }
}