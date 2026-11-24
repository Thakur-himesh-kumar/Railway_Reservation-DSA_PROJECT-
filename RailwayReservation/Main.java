import java.io.IOException;
import java.util.Scanner;

/**
 * Console front-end.
 *   java Main        -> interactive menu (loads railway.txt, saves on exit)
 *   java Main demo   -> runs the 4 test cases from the project description
 */
public class Main {
    static final String DATA_FILE = "railway.txt";

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("demo")) {
            runDemo();
            return;
        }
        ReservationSystem sys = new ReservationSystem();
        try {
            FileHandler.load(DATA_FILE, sys);
        } catch (IOException e) {
            System.out.println("Could not read data file: " + e.getMessage());
        }
        runMenu(sys);
        try {
            FileHandler.save(DATA_FILE, sys);
            System.out.println("Data saved to " + DATA_FILE + ". Goodbye!");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    static void runMenu(ReservationSystem sys) {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== RAILWAY RESERVATION SYSTEM =====");
            System.out.println("1. View all trains");
            System.out.println("2. Sort trains by fare");
            System.out.println("3. Sort trains by departure time");
            System.out.println("4. Search route");
            System.out.println("5. Book ticket");
            System.out.println("6. Cancel ticket");
            System.out.println("7. Search ticket");
            System.out.println("8. View station network");
            System.out.println("9. View waiting list of a train");
            System.out.println("0. Save and exit");
            System.out.print("Enter choice: ");
            if (!in.hasNextLine()) return;          // end of input -> exit (and save)
            String c = in.nextLine().trim();
            switch (c) {
                case "1": System.out.println(sys.listTrains(new java.util.ArrayList<>(sys.getTrains().values()))); break;
                case "2": System.out.println(sys.listTrains(sys.sortedByFare())); break;
                case "3": System.out.println(sys.listTrains(sys.sortedByDeparture())); break;
                case "4":
                    System.out.println(sys.searchRoute(ask(in, "From station: "), ask(in, "To station: ")));
                    break;
                case "5":
                    String name = ask(in, "Passenger name: ");
                    String tid = ask(in, "Train id: ");
                    System.out.println(sys.bookTicket(name, tid));
                    break;
                case "6": System.out.println(sys.cancelTicket(ask(in, "Ticket id: "))); break;
                case "7": System.out.println(sys.searchTicket(ask(in, "Ticket id: "))); break;
                case "8": System.out.print(sys.getGraph().describe()); break;
                case "9": System.out.println(sys.waitingList(ask(in, "Train id: "))); break;
                case "0": return;
                default: System.out.println("Invalid choice. Please enter a number from 0 to 9.");
            }
        }
    }

    static String ask(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    /** Runs the four test cases from the project statement on a small in-memory data set. */
    static void runDemo() {
        ReservationSystem sys = new ReservationSystem();
        sys.addTrain(new Train("T101", "Malwa Express", "Indore", "Bhopal", Train.parseTime("06:30"), 350, 1));
        System.out.println("Test Case 1 (Normal): Book one seat on train T101 for Aman");
        System.out.println("  " + sys.bookTicket("Aman", "T101"));
        System.out.println("Test Case 2 (Boundary): Train T101 is full; Riya books");
        System.out.println("  " + sys.bookTicket("Riya", "T101"));
        System.out.println("Test Case 3 (Invalid): Search ticket TKT999");
        System.out.println("  " + sys.searchTicket("TKT999"));
        System.out.println("Test Case 4 (Different valid): Cancel Aman's ticket while Riya is waitlisted");
        System.out.println("  " + sys.cancelTicket("TKT101"));
    }
}
