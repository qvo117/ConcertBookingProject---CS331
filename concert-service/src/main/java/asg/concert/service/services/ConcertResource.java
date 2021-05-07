package asg.concert.service.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import asg.concert.common.dto.*;
import asg.concert.common.types.BookingStatus;
import asg.concert.service.domain.*;
import asg.concert.service.mapper.PerformerMapper;
import asg.concert.service.mapper.SeatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.mapper.ConcertMapper;
import asg.concert.service.common.Config;

@Path("/concert-service")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class ConcertResource {
	private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
	EntityManager em = PersistenceManager.instance().createEntityManager();
	
	@GET
	@Path("concerts/{id}")
	public Response retrieveConcert(@PathParam("id") Long id, @CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			Concert concert = em.find(Concert.class, id);
			em.getTransaction().commit();
			if(concert == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
			ConcertDTO dto = ConcertMapper.toDto(concert);
			return Response.ok(dto).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}
	
	@GET
	@Path("concerts")
	public Response retrieveAllConcerts(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
			List<Concert> concerts = concertQuery.getResultList();
			em.getTransaction().commit();
			List<ConcertDTO> dtos = new ArrayList<ConcertDTO>();
			for(Concert concert : concerts) {
				dtos.add(ConcertMapper.toDto(concert));
			}
			GenericEntity<List<ConcertDTO>> entity = new GenericEntity<List<ConcertDTO>>(dtos) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}
	
	@GET
	@Path("concerts/summaries")
	public Response retrieveSummaries(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
			List<Concert> concerts = concertQuery.getResultList();
			em.getTransaction().commit();
			List<ConcertSummaryDTO> summaries = new ArrayList<ConcertSummaryDTO>();
			for(Concert concert : concerts) {
				ConcertSummaryDTO summary = new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
				summaries.add(summary);
			}
			GenericEntity<List<ConcertSummaryDTO>> entity = new GenericEntity<List<ConcertSummaryDTO>>(summaries) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}


	@GET
	@Path("performers/{id}")
	public Response retrievePerformer(@PathParam("id") Long id, @CookieParam("clientId") Cookie clientId){
		try {
			em.getTransaction().begin();
			Performer performer = em.find(Performer.class, id);
			em.getTransaction().commit();
			if(performer == null) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
			PerformerDTO dto = PerformerMapper.toDto(performer);
			return Response.ok(dto).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}

	@GET
	@Path("performers")
	public Response retrieveAllPerformers(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
			List<Performer> performers = performerQuery.getResultList();
			em.getTransaction().commit();
			List<PerformerDTO> dtos = new ArrayList<PerformerDTO>();
			for(Performer performer: performers) {
				dtos.add(PerformerMapper.toDto(performer));
			}
			GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(dtos) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}

	@POST
	@Path("login")
	public Response login(UserDTO dto, @CookieParam("clientId") Cookie clientId){
		try {
			em.getTransaction().begin();
			TypedQuery<User> userQuery = em.createQuery("select u from User u", User.class);
			List<User> users = userQuery.getResultList();
			em.getTransaction().commit();
			for(User user: users) {
				if(user.getUsername() == dto.getUsername() && user.getPassword() == dto.getPassword()){
					return Response.created(URI
							.create("/login"))
							.status(Response.Status.OK)
							.cookie(new NewCookie("auth", user.getId().toString()))
							.build();
				}
			}
			return Response.created(URI
					.create("/login"))
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}

	@GET
	@Path("seats/{date}")
	public Response retrieveSeats(@PathParam("date") String dateString, @QueryParam("status") BookingStatus status,
								  @CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = " + dateString, Seat.class);
			List<Seat> seats = seatQuery.getResultList();
			em.getTransaction().commit();
			List<SeatDTO> dtos = new ArrayList<SeatDTO>();
			for(Seat seat : seats) {
				if(status == BookingStatus.Booked && seat.getIsBooked()){
					dtos.add(SeatMapper.toDto(seat));
				}
				else if(status == BookingStatus.Unbooked && !seat.getIsBooked()) {
					dtos.add(SeatMapper.toDto(seat));
				}
				else if(status == BookingStatus.Any) {
					dtos.add(SeatMapper.toDto(seat));
				}
			}
			GenericEntity<List<SeatDTO>> entity = new GenericEntity<List<SeatDTO>>(dtos) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build();
		}
		finally {
			em.close();
		}
	}

	@POST
	@Path("bookings")
	public Response makeBooking(BookingRequestDTO dto, @CookieParam("auth") Cookie clientId) {
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Integer.parseInt(clientId.getValue()));
				for(String label : dto.getSeatLabels()) {
					TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.label = " + label,
																Seat.class);
					Seat seat = seatQuery.getSingleResult();
					if(seat.getIsBooked()) {
						throw new WebApplicationException(Response.Status.FORBIDDEN);
					}
					seat.setIsBooked(true);
					seat.setBookedUser(user);
					em.merge(seat);
				}
			}
			catch(NumberFormatException e) {
				throw new WebApplicationException(Response.Status.UNAUTHORIZED);
			}
			em.getTransaction().commit();
			return Response.created(URI
					.create("/bookings"))
					.status(Response.Status.CREATED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}

	@GET
	@Path("bookings")
	public Response retrieveBookings(@CookieParam("auth") Cookie clientId) {
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Integer.parseInt(clientId.getValue()));
				TypedQuery<Seat> seatsQuery = em.createQuery("select s from Seat s where s.bookedUser = :userId", 
															 Seat.class).setParameter("userId", user.getId());
				List<Seat> seats = seatsQuery.getResultList();
				List<BookingDTO> dtos = new ArrayList<BookingDTO>();
				List<Integer> usedIndexes = new ArrayList<Integer>();
				for(int i = 0; i < seats.size(); i++) {
					if(usedIndexes.contains(i))
						continue;
					List<SeatDTO> seatDtos = new ArrayList<SeatDTO>();
					LocalDateTime concertDate = seats.get(i).getDate();
					TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where c.date = :concertDate", 
															 Concert.class).setParameter("concertDate", concertDate);
					Concert concert = concertQuery.getSingleResult();
					for(int j = i; j < seats.size(); j++) {
						if(usedIndexes.contains(j))
							continue;
						if(seats.get(j).getDate() == concertDate) {
							seatDtos.add(SeatMapper.toDto(seats.get(j)));
							usedIndexes.add(j);
						}
					}
					BookingDTO bookingDto = new BookingDTO(concert.getId(), concertDate, seatDtos);
					dtos.add(bookingDto);
					usedIndexes.add(i);
				}
				GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(dtos) {};
				return Response.ok(entity).cookie(makeCookie(clientId)).build();
			}
			catch(NumberFormatException e) {
				throw new WebApplicationException(Response.Status.UNAUTHORIZED);
			}
		}
		finally {
			em.close();
		}
	}

	private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie = null;

        if (clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }
}
