package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import asg.concert.common.jackson.LocalDateTimeDeserializer;
import asg.concert.common.jackson.LocalDateTimeSerializer;

/*
Booking Domain Model
Each booking is linked to the user that made the booking and the list of seats booked.
 */
@Entity
@Table(name = "BOOKINGS")
public class Booking {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private long concertId;
	private LocalDateTime date;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL) // Eager fetching to load associate entities
    @JoinColumn(name = "USER_ID", nullable = true)
	private User user;
	
	@OneToMany //One booking for many seats
	private List<Seat> seats = new ArrayList<Seat>();

	public Booking() {
	}

	public Booking(Long id, long concertId, LocalDateTime date, List<Seat> seats) {
	   this.id = id;
       this.concertId = concertId;
       this.date = date;
       this.seats = seats;
    }
	
	public Booking(long concertId, LocalDateTime date, List<Seat> seats) {
        this(null, concertId, date, seats);
    }
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
    
    public User getUser() {
    	return user;
    }
    
    public void setUser(User user) {
    	this.user = user;
    }
}
