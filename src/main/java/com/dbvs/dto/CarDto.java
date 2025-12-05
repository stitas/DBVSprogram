package com.dbvs.dto;

public record CarDto(
        Long id,
        String color,
        String numberPlate,
        String carMake,
        Float price
) {
    @Override
    public String toString() {
        return String.format("id: %s, spalva: %s, registracijos_nr: %s, marke: %s, kaina: %.2f",
                    id, color, numberPlate, carMake, price
                );
    }
}
