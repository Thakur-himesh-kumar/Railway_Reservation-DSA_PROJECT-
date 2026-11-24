/**
 * Represents one passenger ticket. A ticket is CONFIRMED (has a seat),
 * WAITING (in the train's waiting queue) or CANCELLED.
 */
public class Ticket {
    public enum Status { CONFIRMED, WAITING, CANCELLED }

    private final String ticketId;     // e.g. TKT101
    private final String passengerName;
    private final String trainId;
    private Status status;

    public Ticket(String ticketId, String passengerName, String trainId, Status status) {
        this.ticketId = ticketId;
        this.passengerName = passengerName;
        this.trainId = trainId;
        this.status = status;
    }

    public String getTicketId()      { return ticketId; }
    public String getPassengerName() { return passengerName; }
    public String getTrainId()       { return trainId; }
    public Status getStatus()        { return status; }
    public void setStatus(Status s)  { this.status = s; }

    /** Numeric part of the ticket id (TKT101 -> 101); used to keep FIFO order on reload. */
    public int getSequence() {
        try {
            return Integer.parseInt(ticketId.substring(3));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return ticketId + " | " + passengerName + " | Train " + trainId + " | " + status;
    }
}
