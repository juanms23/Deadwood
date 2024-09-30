package view;

import java.util.Scanner;

import java.io.Console;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public final class CommandLineUI {

    private CommandLineUI() {
    } // prevent instantiation

    private static Scanner s = new Scanner(System.in);

    private static String getUserInput(String msg) {
        System.out.print(msg);
        String res = s.nextLine();

        return res;
    }

    // handle getting data from user
    public static String prompt(String msg) {
        return getUserInput(msg + ": ");
    }

    public static String prompt() {
        System.out.println();
        return getUserInput("> ");
    }

    // prompt the user for a number between min and max
    public static int promptNum(String msg, int min, int max) {
        String res = null;
        Boolean validNum = false;
        do {
            res = getUserInput(msg + ": ");
            res = res.strip(); // remove leading/trailing spaces

            String numbersInRange = String.format("[%d-%d]", min, max); // numbers between [min,max]
            validNum = res.matches(numbersInRange);

            if (!validNum) {
                System.out.println("Invalid number passed. Please enter a valid number.");
            }

        } while (!validNum);

        return Integer.parseInt(res);
    }

    public static ArrayList<String> getNames(int numPlayers) {

        ArrayList<String> names = new ArrayList<String>();
        for (int i = 1; i <= numPlayers; i++) {

            // ensure name is unique
            String name = null;
            do {
                name = CommandLineUI.prompt("Enter Player " + i + "s name");
                name = name.toUpperCase(); // ensure consistant case

                if (names.contains(name)) {
                    name = null;
                    System.out.println("Names must be unique. Please enter a unique name.");
                }

            } while (name == null);

            names.add(name);

        } // got player names

        return names;
    }

    public static String getRoom(HashSet<String> rooms) {

        String room = null;
        do {
            String possibleRooms = String.join(", ", rooms);
            System.out.print("Enter a room to move to from the following: ");
            System.out.println(possibleRooms);

            room = CommandLineUI.prompt("Room");

            if (!rooms.contains(room)) {
                room = null;
                print("The room you entered is not valid.");
            }

        } while (room == null);

        print("You moved to the " + room + ".");

        return room;
    }

    // handles general selection between two options
    public static String chooseBetweenTwo(String msg, String prompt, String opt1, String opt2) {

        String ans = null;
        do {
            System.out.println(msg);
            ans = CommandLineUI.prompt(prompt);

            if (!opt1.equals(ans) && !opt2.equals(ans)) {
                ans = null;
                print("Select either " + opt1 + " or " + opt2 + ".");
            }

        } while (ans == null);

        print(ans + " selected.");

        return ans;
    }

    public static String getRole(List<String> onTitles, List<String> onLevels, List<String> offTitles,
            List<String> offLevels) {

        String role = null;
        do {

            print("Please choose one of the following roles.");

            // print off card role options with corresponding level
            System.out.print("Off-Card Roles | ");
            int numOffTitles = offTitles.size();
            for (int i = 0; i < numOffTitles; i++) {

                String fmtTitle = offTitles.get(i) + "(" + offLevels.get(i) + ")";
                System.out.print(fmtTitle);

                if (i == (numOffTitles - 1)) {
                    System.out.println();
                } else {
                    System.out.print(", ");
                }

            }

            // print on card role options with corresponding level
            System.out.print(" On-Card Roles | ");
            int numOnTitles = onTitles.size();
            for (int i = 0; i < numOnTitles; i++) {

                String fmtTitle = onTitles.get(i) + "(" + onLevels.get(i) + ")";
                System.out.print(fmtTitle);

                if (i == (numOnTitles - 1)) {
                    System.out.println();
                } else {
                    System.out.print(", ");
                }

            }

            role = CommandLineUI.prompt("Role");

            if (!onTitles.contains(role) && !offTitles.contains(role)) {
                role = null;
                print("The role you entered is not valid.");
            }

        } while (role == null);

        return role;
    }

    public static void print(String msg) {
        System.out.println(msg);
    }

    public static void displayActivePlayerStats(String loc, String roleName, String pChips, String score) {
        Console cnsl = System.console();
        String fmt = "%1$10s %2$20s %3$20s %4$10s%n";
        cnsl.format(fmt, "Location", "Role", "Practice Chips", "Score");
        cnsl.format(fmt, "--------", "----", "--------------", "-----");
        cnsl.format(fmt, loc, roleName, pChips, score);

        System.out.println();
    }

    public static void closeScanner() {
        s.close();
    }

}
