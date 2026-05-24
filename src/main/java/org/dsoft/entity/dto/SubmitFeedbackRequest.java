package org.dsoft.entity.dto;

public class SubmitFeedbackRequest {

    public int rating;              // Required: 1 to 5

    public String likedNotes;       // Optional: what the user liked

    public String improvementNotes; // Optional: what to improve next time
}
