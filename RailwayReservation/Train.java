import java.util.LinkedList;
import java.util.Queue;

/**
 * A train with a fixed seat capacity and its own FIFO waiting queue.
 */
public class Train {
    private final String trainId;
    private final String name;
    private final String source;
    private final String destination;
    private final int departureMinutes;   // minutes after midnight, easy to sort
    private final double fare;
    private final int capacity;
    private int confirmedSeats;
    private final Queue<Ticket> waitingQueue = new LinkedList<>();   // FIFO

    public Train(String trainId, String name, String source, String destination,
                 int departureMinutes, double fare, int capacity) {
        this.trainId = trainId;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.departureMinutes = departureMinutes;
        this.fare = fare;
        this.capacity = capacity;
    }

    public String getTrainId()          { return trainId; }
    public String getName()             { return name; }
    public String getSource()           { return source; }
    public String getDestination()      { return destination; }
    public int getDepartureMinutes()    { return departureMinutes; }
    public double getFare()             { return fare; }
    public int getCapacity()            { return capacity; }
    public int getConfirmedSeats()      { return confirmedSeats; }
    public int getAvailableSeats()      { return capacity - confirmedSeats; }
    public Queue<Ticket> getWaitingQueue() { return waitingQueue; }

    public boolean hasFreeSeat() { return confirmedSeats < capacity; }

    /** Takes one seat. Never allows more than 'capacity' seats to be used. */
    public boolean occupySeat() {
        if (!hasFreeSeat()) return false;
        confirmedSeats++;
        return true;
    }

    public void releaseSeat() {
        if (confirmedSeats > 0) confirmedSeats--;
    }

    public String getDepartureTime() {
        return String.format("%02d:%02d", departureMinutes / 60, departureMinutes % 60);
    }

    /** Converts "HH:mm" into minutes after midnight. Throws IllegalArgumentException if invalid. */
    public static int parseTime(String hhmm) {
        String[] p = hhmm.trim().split(":");
        if (p.length != 2) throw new IllegalArgumentException("Bad time: " + hhmm);
        int h = Integer.parseInt(p[0].trim());
        int m = Integer.parseInt(p[1].trim());
        if (h < 0 || h > 23 || m < 0 || m > 59) throw new IllegalArgumentException("Bad time: " + hhmm);
        return h * 60 + m;
    }
}
