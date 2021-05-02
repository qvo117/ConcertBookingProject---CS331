package asg.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConcertInfoNotification {

    private int numSeatsRemaining;

    public ConcertInfoNotification() {
    }

    public ConcertInfoNotification(int numSeatsRemaining) {
        this.numSeatsRemaining = numSeatsRemaining;
    }

    public int getNumSeatsRemaining() {
        return numSeatsRemaining;
    }

    public void setNumSeatsRemaining(int numSeatsRemaining) {
        this.numSeatsRemaining = numSeatsRemaining;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Booking, numSeatsRemaining: ");
        buffer.append(numSeatsRemaining);

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConcertInfoNotification))
            return false;
        if (obj == this)
            return true;

        ConcertInfoNotification rhs = (ConcertInfoNotification) obj;
        return new EqualsBuilder().
                append(numSeatsRemaining, rhs.numSeatsRemaining).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
        		append(numSeatsRemaining).hashCode();
    }
    
}