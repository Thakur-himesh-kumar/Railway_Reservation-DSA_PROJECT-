import java.io.*;
import java.util.*;

/**
 * Loads and saves everything to railway.txt.
 * Line formats (comma separated, '#' starts a comment):
 *   ROUTE,StationA,StationB,distanceKm
 *   TRAIN,id,name,source,destination,HH:mm,fare,capacity
 *   TICKET,ticketId,passenger,trainId,STATUS
 */
public class FileHandler {

    public static void load(String path, ReservationSystem sys) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("Data file '" + path + "' not found. Starting with empty data.");
            return;
        }
        List<Ticket> loadedTickets = new ArrayList<>();
        int lineNo = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] p = line.split(",");
                try {
                    switch (p[0].trim().toUpperCase()) {
                        case "ROUTE":
                            sys.getGraph().addRoute(p[1].trim(), p[2].trim(), Integer.parseInt(p[3].trim()));
                            break;
                        case "TRAIN":
                            sys.addTrain(new Train(p[1].trim().toUpperCase(), p[2].trim(), p[3].trim(), p[4].trim(),
                                    Train.parseTime(p[5]), Double.parseDouble(p[6].trim()), Integer.parseInt(p[7].trim())));
                            break;
                        case "TICKET":
                            loadedTickets.add(new Ticket(p[1].trim(), p[2].trim(), p[3].trim().toUpperCase(),
                                    Ticket.Status.valueOf(p[4].trim().toUpperCase())));
                            break;
                        default:
                            System.out.println("Line " + lineNo + ": unknown record type, skipped.");
                    }
                } catch (Exception e) {
                    System.out.println("Line " + lineNo + ": invalid record, skipped (" + e.getMessage() + ").");
                }
            }
        }
        // restore in ticket-number order so waiting queues stay FIFO
        loadedTickets.sort(Comparator.comparingInt(Ticket::getSequence));
        for (Ticket t : loadedTickets) sys.restoreTicket(t);
    }

    public static void save(String path, ReservationSystem sys) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("# Railway Reservation System data file");
            pw.println("# ROUTE,StationA,StationB,km");
            Set<String> written = new HashSet<>();
            for (String a : sys.getGraph().getStations()) {
                for (RailwayGraph.Edge e : sys.getGraph().getNeighbours(a)) {
                    String key = a.compareTo(e.to) < 0 ? a + "|" + e.to : e.to + "|" + a;
                    if (written.add(key)) pw.println("ROUTE," + a + "," + e.to + "," + e.distance);
                }
            }
            pw.println("# TRAIN,id,name,source,destination,departure,fare,capacity");
            for (Train t : sys.getTrains().values())
                pw.println("TRAIN," + t.getTrainId() + "," + t.getName() + "," + t.getSource() + ","
                        + t.getDestination() + "," + t.getDepartureTime() + "," + t.getFare() + "," + t.getCapacity());
            pw.println("# TICKET,ticketId,passenger,trainId,status");
            List<Ticket> list = new ArrayList<>(sys.getTickets().values());
            list.sort(Comparator.comparingInt(Ticket::getSequence));
            for (Ticket t : list)
                pw.println("TICKET," + t.getTicketId() + "," + t.getPassengerName() + "," + t.getTrainId() + "," + t.getStatus());
        }
    }
}
