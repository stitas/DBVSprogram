package com.dbvs;

import com.dbvs.dto.RentStatus;
import com.dbvs.dto.UnfinishedRentsDto;
import com.dbvs.tables.Automobilis;
import com.dbvs.tables.Klientas;
import com.dbvs.tables.Nuoma;
import com.dbvs.tables.NuomaAutomobilis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.dbvs.Utils.SQL_ERROR_MSG;

public class DbExecutor {
    private final Connection conn;
    private final Automobilis automobilis;
    private final Klientas klientas;
    private final Nuoma nuoma;
    private final NuomaAutomobilis nuomaAutomobilis;

    public DbExecutor(Connection conn) {
        this.conn = conn;
        this.automobilis = new Automobilis(conn);
        this.klientas = new Klientas(conn);
        this.nuoma = new Nuoma(conn);
        this.nuomaAutomobilis = new NuomaAutomobilis(conn);
    }

    public void execute(int input) {
        switch (input) {
            case 1 -> findUnfinishedRents();
            case 2 -> insertNewRent();
            case 3 -> automobilis.updateCarPrice();
            case 4 -> automobilis.deleteCar();
            default -> System.out.println("Operacija nerasta");
        }
    }

    private void findUnfinishedRents() {
        List<UnfinishedRentsDto> dataList = new ArrayList<>();
        String sql = "SELECT * FROM vw_neuzbaigtos_nuomos ORDER BY pradzios_data;";

        try {
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String rentStatusStr = rs.getString(8);
                RentStatus rentStatus = RentStatus.valueOf(rentStatusStr);

                UnfinishedRentsDto data = new UnfinishedRentsDto(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getObject(6, LocalDate.class),
                        rs.getObject(7, LocalDate.class),
                        rentStatus
                );

                dataList.add(data);
            }
        } catch (SQLException e) {
            System.out.println(String.format(SQL_ERROR_MSG, e.getMessage()));
        }

        Utils.printList(dataList);
    }

    private void insertNewRent() {
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("Nepavyko isjungti auto commit funkcijos bandykite dar karta");
            return;
        }

        klientas.findAllClients();
        long rentId = nuoma.insertRent();

        if (rentId == -1) {
            rollback();
            return;
        }

        boolean insertCar = true;
        int count = 0;
        while (insertCar) {
            System.out.println("Iveskite nuomos data nuo: ");
            LocalDate dateFrom = Utils.getLocalDateInput();
            System.out.println("Iveskite nuomos data iki: ");
            LocalDate dateTo = Utils.getLocalDateInput();
            boolean noCarsAvailable = automobilis.findAvailableUnavailableCars(dateFrom, dateTo);

            if (noCarsAvailable) {
                System.out.println("Nera galimu pasirinkti automobiliu");

                if (count == 0) {
                    rollback();
                }

                return;
            }

            if (!nuomaAutomobilis.insertRentCar(rentId, dateFrom, dateTo)) {
                rollback();
                return;
            }

            System.out.println("Ar norite ivesti dar viena automobili nuomai ? (taip/ne): ");
            insertCar = Utils.getStringInput().equals("taip");
            count++;
        }

        try {
            conn.commit();
            System.out.println("Nuoma su automobiliais sekmingai sukurta");
        } catch (SQLException e) {
            rollback();
            System.out.println("Nepavyko uzbaigti transakcijos");
        }
    }

    private void rollback() {
        try {
            System.out.println("Vykdomas ROLLBACK");
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("Nepavyko ROLLBACK!!!!");
        }
    }
}