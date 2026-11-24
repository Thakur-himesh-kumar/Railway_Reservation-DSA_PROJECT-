import java.util.*;

/**
 * Core logic: bookings, cancellations, waiting-list handling, searching and sorting.
 *  - Graph      : stations and routes
 *  - Queue      : one FIFO waiting list per train (inside Train)
 *  - HashMap    : ticketId -> Ticket and trainId -> Train (O(1) lookup)
 *  - Sorting    : merge sort by fare / departure time
 */
public class ReservationSystem {
    private final RailwayGraph graph = new RailwayGraph();
    private final Map<String, Train> trains = new LinkedHashMap<>();   // LinkedHashMap keeps file order
    private final Map<String, Ticket> tickets = new HashMap<>();
    private int nextTicketNumber = 101;

    // ---------- data set-up ----------
    public RailwayGraph getGraph()          { return graph; }
    public Map<String, Train> getTrains()   { return trains; }
    public Map<String, Ticket> getTickets() { return tickets; }

    public void addTrain(Train t) {
        trains.put(t.getTrainId(), t);
        graph.addStation(t.getSource());
        graph.addStation(t.getDestination());
    }

    /** Used when loading from file. Keeps queues in FIFO order and counts seats. */
    public void restoreTicket(Ticket t) {
        tickets.put(t.getTicketId(), t);
        Train train = trains.get(t.getTrainId());
        if (train != null) {
            if (t.getStatus() == Ticket.Status.CONFIRMED) train.occupySeat();
            else if (t.getStatus() == Ticket.Status.WAITING) train.getWaitingQueue().add(t);
        }
        nextTicketNumber = Math.max(nextTicketNumber, t.getSequence() + 1);
    }

    // ---------- booking ----------
    public String bookTicket(String passenger, String trainId) {
        if (passenger == null || passenger.trim().isEmpty()) return "Invalid input: passenger name is empty.";
        Train train = trains.get(trainId.trim().toUpperCase());
        if (train == null) return "Train " + trainId.trim().toUpperCase() + " not found.";

        String id = "TKT" + nextTicketNumber++;
        passenger = passenger.trim();
        if (train.hasFreeSeat()) {
            train.occupySeat();
            Ticket t = new Ticket(id, passenger, train.getTrainId(), Ticket.Status.CONFIRMED);
            tickets.put(id, t);
            return "Ticket booked: " + id + " | " + passenger + " | Train " + train.getTrainId();
        }
        Ticket t = new Ticket(id, passenger, train.getTrainId(), Ticket.Status.WAITING);
        tickets.put(id, t);
        train.getWaitingQueue().add(t);          // enqueue at the tail
        return "Train full. Passenger added to waiting queue.\n"
             + "  (Waiting ticket " + id + " | " + passenger + " | position "
             + train.getWaitingQueue().size() + ")";
    }

    // ---------- cancellation ----------
    public String cancelTicket(String ticketId) {
        Ticket t = tickets.get(ticketId.trim().toUpperCase());
        if (t == null) return "Ticket " + ticketId.trim().toUpperCase() + " not found.";
        if (t.getStatus() == Ticket.Status.CANCELLED) return "Ticket " + t.getTicketId() + " is already cancelled.";

        Train train = trains.get(t.getTrainId());
        if (t.getStatus() == Ticket.Status.WAITING) {
            train.getWaitingQueue().remove(t);
            t.setStatus(Ticket.Status.CANCELLED);
            return "Ticket cancelled. (It was on the waiting list.)";
        }
        // confirmed ticket: free the seat, then offer it to the head of the queue (FIFO)
        t.setStatus(Ticket.Status.CANCELLED);
        train.releaseSeat();
        Ticket next = train.getWaitingQueue().poll();
        if (next != null) {
            next.setStatus(Ticket.Status.CONFIRMED);
            train.occupySeat();
            return "Ticket cancelled. Seat allocated to " + next.getPassengerName()
                 + " from waiting queue.\n  (" + next.getTicketId() + " is now CONFIRMED)";
        }
        return "Ticket cancelled. Seat is now available.";
    }

    // ---------- searching ----------
    public String searchTicket(String ticketId) {
        Ticket t = tickets.get(ticketId.trim().toUpperCase());
        if (t == null) return "Ticket " + ticketId.trim().toUpperCase() + " not found.";
        return "Ticket found: " + t;
    }

    public String searchRoute(String from, String to) {
        from = canonical(from);
        to = canonical(to);
        if (from == null || to == null) return "Station not found in the network.";
        StringBuilder sb = new StringBuilder();
        RailwayGraph.PathResult r = graph.shortestPath(from, to);
        if (r == null) return "No route between " + from + " and " + to + ".";
        sb.append("Shortest route: ").append(String.join(" -> ", r.path))
          .append(" (").append(r.distance).append(" km)\n");
        List<Train> direct = new ArrayList<>();
        for (Train t : trains.values())
            if (t.getSource().equals(from) && t.getDestination().equals(to)) direct.add(t);
        if (direct.isEmpty()) sb.append("No direct train; take trains along the route above.");
        else {
            sb.append("Direct trains:\n");
            for (Train t : Sorter.mergeSort(direct, Comparator.comparingInt(Train::getDepartureMinutes)))
                sb.append(trainRow(t)).append("\n");
        }
        return sb.toString().trim();
    }

    /** Case-insensitive station name lookup. */
    private String canonical(String name) {
        for (String s : graph.getStations()) if (s.equalsIgnoreCase(name.trim())) return s;
        return null;
    }

    // ---------- listing / sorting ----------
    public List<Train> sortedByFare() {
        return Sorter.mergeSort(new ArrayList<>(trains.values()), Comparator.comparingDouble(Train::getFare));
    }

    public List<Train> sortedByDeparture() {
        return Sorter.mergeSort(new ArrayList<>(trains.values()), Comparator.comparingInt(Train::getDepartureMinutes));
    }

    public static String header() {
        return String.format("%-6s %-18s %-10s %-10s %-6s %-8s %-6s %-6s",
                "ID", "Name", "From", "To", "Dep", "Fare", "Seats", "Wait");
    }

    public static String trainRow(Train t) {
        return String.format("%-6s %-18s %-10s %-10s %-6s %-8.2f %-6s %-6d",
                t.getTrainId(), t.getName(), t.getSource(), t.getDestination(), t.getDepartureTime(),
                t.getFare(), t.getAvailableSeats() + "/" + t.getCapacity(), t.getWaitingQueue().size());
    }

    public String listTrains(List<Train> list) {
        StringBuilder sb = new StringBuilder(header()).append("\n");
        for (Train t : list) sb.append(trainRow(t)).append("\n");
        return sb.toString().trim();
    }

    public String waitingList(String trainId) {
        Train t = trains.get(trainId.trim().toUpperCase());
        if (t == null) return "Train " + trainId.trim().toUpperCase() + " not found.";
        if (t.getWaitingQueue().isEmpty()) return "Waiting list for " + t.getTrainId() + " is empty.";
        StringBuilder sb = new StringBuilder("Waiting list for " + t.getTrainId() + " (front first):\n");
        int pos = 1;
        for (Ticket w : t.getWaitingQueue())
            sb.append("  ").append(pos++).append(". ").append(w.getTicketId()).append(" | ")
              .append(w.getPassengerName()).append("\n");
        return sb.toString().trim();
    }
}
