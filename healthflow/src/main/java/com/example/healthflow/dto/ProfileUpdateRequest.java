package com.example.healthflow.dto;

import com.example.healthflow.model.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String address;

    // Method to parse dateOfBirth String to LocalDate if needed
    public LocalDate getDateOfBirthAsLocalDate() {
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            return LocalDate.parse(dateOfBirth);
        }
        return null;
    }

    // Method to convert gender String to Gender enum if needed
    public Gender getGenderAsEnum() {
        if (gender != null && !gender.isEmpty()) {
            return Gender.valueOf(gender.toUpperCase());
        }
        return null;
    }
}