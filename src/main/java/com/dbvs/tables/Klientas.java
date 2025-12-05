package com.dbvs.tables;

import com.dbvs.Utils;
import com.dbvs.dto.ClientDto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.dbvs.Utils.SQL_ERROR_MSG;

public class Klientas {
    private final Connection conn;

    public Klientas(Connection conn) {
        this.conn = conn;
    }

    public void findAllClients() {
        List<ClientDto> dataList = new ArrayList<>();
        String sql = "SELECT * FROM klientas ORDER BY id;";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ClientDto data = new ClientDto(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getObject(5, LocalDate.class)
                );

                dataList.add(data);
            }
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }

        Utils.printList(dataList);
    }
}
