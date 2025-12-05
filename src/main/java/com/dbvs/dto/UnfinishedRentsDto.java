package com.dbvs.dto;

import java.time.LocalDate;

public record UnfinishedRentsDto (
        Long rentId,
        String firstname,
        String lastname,
        String carMake,
        String plateNumber,
        LocalDate startDate,
        LocalDate endDate,
        RentStatus rentStatus
) {
    @Override
    public String toString() {
        return String.format("nuoma_id: %d, vardas: %s, pavarde: %s, auto_marke: %s, registracijos_nr: %s, pradzios_data: %s, pabaigos_data: %s, busena: %s",
            rentId, firstname, lastname, carMake, plateNumber, startDate.toString(), endDate.toString(), rentStatus.name()
        );
    }
}
