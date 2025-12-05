package com.dbvs.dto;

import java.time.LocalDate;

public record ClientDto(
        Long id,
        String firstname,
        String lastname,
        String email,
        LocalDate birthDate
) {
    @Override
    public String toString() {
        return String.format("id: %s, vardas: %s, pavarde: %s, el_pastas: %s, gimimo_data: %s",
                id, firstname, lastname, email, birthDate.toString()
        );
    }
}
