package com.dbvs;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/biblio?currentSchema=auto_nuoma";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void main(String[] args)
    {
        DbConnection dbConnection = new DbConnection(DB_URL, DB_USER, DB_PASSWORD);
        Connection conn = dbConnection.getConnection();
        DbExecutor dbExecutor = new DbExecutor(conn);

        while (true) {
            System.out.println("------------------------------------");
            System.out.println("Pasirinkite norima operacija: ");
            System.out.println("1 - Neuzbaigtu nuomu paieska");
            System.out.println("2 - Sukurti nauja nuoma");
            System.out.println("3 - Pakeisti automobilio kaina parai");
            System.out.println("4 - Pasalinti automobili");
            System.out.println("5 - Baigti darba");
            System.out.println("------------------------------------");
            System.out.println("Operacijos nr: ");
            int input = Utils.getIntInput();

            if (input == 5) {
                System.out.println("Darbas baigtas. Iki !");
                break;
            }

            dbExecutor.execute(input);
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException exp) {
                System.out.println("Can not close connection!");
            }
        }
    }


}