package com.dbvs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Utils {
    public static final String SQL_ERROR_MSG = "Ivyko klaida vykdant užklausa: %s";
    private static final Scanner scanner = new Scanner(System.in);

    private Utils() {}

    public static int getIntInput() {
        int input = -1;

        try {
            input = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Bloga įvestis !");
            return input;
        } catch (Exception e) {
            System.out.println("Sistemos klaida !");
            System.exit(1);
        }

        return input;
    }

    public static long getLongInput() {
        long input = -1;

        try {
            input = scanner.nextLong();
        } catch (InputMismatchException e) {
            System.out.println("Bloga įvestis !");
            return input;
        } catch (Exception e) {
            System.out.println("Sistemos klaida !");
            System.exit(1);
        }

        return input;
    }

    public static float getFloatInput() {
        float input = -1;

        try {
            input = scanner.nextFloat();
        } catch (InputMismatchException e) {
            System.out.println("Bloga įvestis !");
            return input;
        } catch (Exception e) {
            System.out.println("Sistemos klaida !");
            System.exit(1);
        }

        return input;
    }

    public static String getStringInput() {
        String input = "";

        try {
            input = scanner.next();
        } catch (InputMismatchException e) {
            System.out.println("Bloga įvestis !");
            return input;
        } catch (Exception e) {
            System.out.println("Sistemos klaida !");
            System.exit(1);
        }

        return input;
    }

    public static LocalDate getLocalDateInput() {
        LocalDate input = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Desired date format

        try {
            String dateString = scanner.next(); // Read the input as a string
            input = LocalDate.parse(dateString, formatter); // Try parsing the string into LocalDate
        } catch (DateTimeParseException e) {
            System.out.println("Blogas datos formatas! Įveskite datą formatu 'yyyy-MM-dd'.");
        } catch (Exception e) {
            System.out.println("Sistemos klaida !");
            System.exit(1);
        }

        return input;
    }

    public static void printList(List<?> list) {
        list.forEach(item -> System.out.println(item.toString()));
    }
}
