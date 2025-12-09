package com.dbvs.tables;

import com.dbvs.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static com.dbvs.Utils.SQL_ERROR_MSG;

public class NuomaAutomobilis {
    private final Connection conn;

    public NuomaAutomobilis(Connection conn) {
        this.conn = conn;
    }

    public boolean insertRentCar(long rentId, LocalDate dateFrom, LocalDate dateTo) {
        System.out.println("Iveskite pasirinkto automobilio id: ");
        int carId = Utils.getIntInput();

        String sql = """
              INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
              (nextval('nuoma_automobilis_id_seq'), ?, ?, ?, null, ?);
              """;

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, rentId);
            stmt.setObject(2, dateFrom);
            stmt.setObject(3, dateTo);
            stmt.setLong(4, carId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
            return false;
        }

        return true;
    }
}
