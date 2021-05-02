package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BookingRequest {
	
	private long concertId;
    private LocalDateTime date;
    private List<String> seatLabels = new ArrayList<>();

    public BookingRequest() {
    }

    public BookingRequest(long concertId, LocalDateTime date, List<String> seatLabels) {
        this.concertId = concertId;
        this.date = date;
        this.seatLabels = seatLabels;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<String> getSeatLabels() {
        return seatLabels;
    }

    public void setSeatLabels(List<String> seatLabels) {
        this.seatLabels = seatLabels;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Booking, concertId: ");
        buffer.append(concertId);
        buffer.append(", date: ");
        buffer.append(date.toString());
        buffer.append(", seatLabels: ");
        buffer.append(seatLabels.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BookingRequest))
            return false;
        if (obj == this)
            return true;

        BookingRequest rhs = (BookingRequest) obj;
        return new EqualsBuilder().
                append(concertId, rhs.concertId).append(date, rhs.date).append(seatLabels, rhs.seatLabels).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
        		append(concertId).append(date).append(seatLabels).hashCode();
    }
    
}