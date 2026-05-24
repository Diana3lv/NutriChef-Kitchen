package org.dsoft.entity.dto;

import java.time.LocalDateTime;

public class RecipeFeedbackDTO {

    public int rating;
    public String likedNotes;
    public String improvementNotes;
    public LocalDateTime updatedAt;

    public RecipeFeedbackDTO() {}

    public RecipeFeedbackDTO(int rating, String likedNotes, String improvementNotes, LocalDateTime updatedAt) {
        this.rating = rating;
        this.likedNotes = likedNotes;
        this.improvementNotes = improvementNotes;
        this.updatedAt = updatedAt;
    }
}
