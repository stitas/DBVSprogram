package com.dbvs.tables;

import com.dbvs.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.dbvs.Utils.SQL_ERROR_MSG;

public class Nuoma {
    private Connection conn;

    public Nuoma(Connection conn) {
        this.conn = conn;
    }

    public long insertRent() {
        System.out.println("Is egistuojanciu klientu iveskite kuris nuomuosis (ID): ");
        long clientId = Utils.getLongInput();
        System.out.println("Iveskite nuomos kaina: ");
        float price = Utils.getFloatInput();

        String sql = """
            INSERT INTO nuoma (id, klientas_id, kaina, busena)
            VALUES (nextval('nuoma_id_seq'), ?, ?, 'AKTYVI');
            """;

        long rentId = -1;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, clientId);
            stmt.setFloat(2, price);
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next()) {
                rentId = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
            return -1;
        }

        return rentId;
    }
}
