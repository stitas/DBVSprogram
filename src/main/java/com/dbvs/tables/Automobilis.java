package com.dbvs.tables;

import com.dbvs.Utils;
import com.dbvs.dto.CarDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.dbvs.Utils.SQL_ERROR_MSG;

public class Automobilis {
    private final Connection conn;

    public Automobilis(Connection conn) {
        this.conn = conn;
    }

    public boolean findAvailableUnavailableCars(LocalDate from, LocalDate to) {
        List<Integer> availableCarIdList = new ArrayList<>();
        String sql = """
                SELECT a.id AS automobilis_id
                FROM automobilis a
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM nuoma_automobilis na
                    WHERE na.automobilis_id = a.id
                      AND na.pradzios_data <= ?
                      AND na.pabaigos_data >= ?
                );
                """;

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, to);
            stmt.setObject(2, from);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Integer carId = rs.getInt(1);
                availableCarIdList.add(carId);
            }
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }

        String paramStr = String.join(",", availableCarIdList.stream().map(Object::toString).toList());
        System.out.println(paramStr);

        sql = String.format("SELECT * FROM automobilis a WHERE a.id IN (%s)", paramStr);
        List<CarDto> availableCarData = findCars(sql);

        sql = String.format("SELECT * FROM automobilis a WHERE a.id NOT IN (%s)", paramStr);
        List<CarDto> unavailableCarData = findCars(sql);

        System.out.println("PRIEINAMI AUTOMOBILIAI:");
        Utils.printList(availableCarData);

        System.out.println("TOMIS DATOMIS UZIMTI AUTOMOBILIAI:");
        Utils.printList(unavailableCarData);

        return availableCarData.isEmpty();
    }

    private List<CarDto> findCars(String sql) {
        List<CarDto> carData = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                CarDto dto = new CarDto(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getFloat(5)
                );
                carData.add(dto);
            }
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }

        return carData;
    }

    public void updateCarPrice() {
        String sql = """
                SELECT *
                FROM automobilis;
                """;
        List<CarDto> cars = findCars(sql);
        Utils.printList(cars);

        System.out.println("Iveskite pasirinkto automobilio id: ");
        int carId = Utils.getIntInput();

        System.out.println("Iveskite nauja kaina: ");
        float dayPrice = Utils.getFloatInput();

        sql = """
             UPDATE automobilis
             SET kaina_parai = ?
             WHERE id = ?;
             """;

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, dayPrice);
            stmt.setObject(2, carId);
            stmt.executeUpdate();
            System.out.println("Automobilis sekmingai redaguotas");
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }
    }

    public void deleteCar() {
        String sql = """
                SELECT *
                FROM automobilis;
                """;
        List<CarDto> cars = findCars(sql);
        Utils.printList(cars);

        System.out.println("Iveskite pasirinkto automobilio id: ");
        int carId = Utils.getIntInput();

        sql = """
             DELETE FROM automobilis
             WHERE id = ?;
             """;

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, carId);
            stmt.executeUpdate();
            System.out.println("Automobilis sekmingai pasalintas");
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }
    }
}
