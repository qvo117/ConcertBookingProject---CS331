package asg.concert.service.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.persistence.Access;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

import asg.concert.common.dto.*;
import asg.concert.common.types.BookingStatus;
import asg.concert.service.domain.*;
import asg.concert.service.mapper.PerformerMapper;
import asg.concert.service.mapper.SeatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.mapper.BookingMapper;
import asg.concert.service.mapper.ConcertMapper;
import asg.concert.service.common.Config;

@Path("/concert-service")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class ConcertResource {
	private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
	EntityManager em = PersistenceManager.instance().createEntityManager();
	private static List<AsyncResponse> subs = new ArrayList<AsyncResponse>();
	private static List<ConcertInfoSubscriptionDTO> subDtos = new ArrayList<ConcertInfoSubscriptionDTO>();
	
	
	@GET
	@Path("concerts/{id}")
	public Response retrieveConcert(@PathParam("id") Long id, @CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			Concert concert = em.find(Concert.class, id);
			em.getTransaction().commit();
			if(concert == null) {
				return Response
						.status(Response.Status.NOT_FOUND)
						.cookie(makeCookie(clientId))
						.build();
			}
			ConcertDTO dto = ConcertMapper.toDto(concert);
			//get a list of Performers
			List<PerformerDTO> performers = new ArrayList<>();
			for (Performer performer: concert.getPerformers()) {
				performers.add(PerformerMapper.toDto(performer));
			};
			dto.setPerformers(performers);
			//get a list of Dates
			List<LocalDateTime> dates = new ArrayList<>();
			for (LocalDateTime date: concert.getDates()) {
				dates.add(date);
			};
			dto.setDates(dates);
			
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
				ConcertDTO dto = ConcertMapper.toDto(concert);
				//get a list of Performers
				List<PerformerDTO> performers = new ArrayList<>();
				for (Performer performer: concert.getPerformers()) {
					performers.add(PerformerMapper.toDto(performer));
				};
				dto.setPerformers(performers);
				//get a list of Dates
				List<LocalDateTime> dates = new ArrayList<>();
				for (LocalDateTime date: concert.getDates()) {
					dates.add(date);
				};
				dto.setDates(dates);
				//add the ConcertDto to the list of ConcertDTOs
				dtos.add(dto);
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
				return Response
						.status(Response.Status.NOT_FOUND)
						.cookie(makeCookie(clientId))
						.build();
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
			TypedQuery<User> userQuery = em.createQuery("select u from User u", User.class).setLockMode(LockModeType.OPTIMISTIC);
			List<User> users = userQuery.getResultList();
			em.getTransaction().commit();
			for(User user: users) {
				if(user.getUsername().equals(dto.getUsername()) && user.getPassword().equals(dto.getPassword())){
					NewCookie authCookie = new NewCookie("auth", user.getId().toString());
					return Response
							.status(Response.Status.OK)
							.cookie(authCookie)
							.build();
				}
			}
			return Response
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
			TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = '" + dateString + "'", Seat.class).setLockMode(LockModeType.OPTIMISTIC);
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
		if(clientId == null)
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			Booking booking;
			Concert concert = em.find(Concert.class, dto.getConcertId());
			if(concert == null || !concert.getDates().contains(dto.getDate()))
				return Response
						.status(Response.Status.BAD_REQUEST)
						.cookie(makeCookie(clientId))
						.build();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));
				List<Seat> bookingSeats = new ArrayList<Seat>();
				for(String label : dto.getSeatLabels()) {
					TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = '" + dto.getDate().toString()
																+ "' and s.label = '" + label + "'", Seat.class).setLockMode(LockModeType.OPTIMISTIC);
					Seat seat = seatQuery.getSingleResult();
					if(seat.getIsBooked()) {
						return Response
								.status(Response.Status.FORBIDDEN)
								.cookie(makeCookie(clientId))
								.build();
					}
					seat.setIsBooked(true);
					bookingSeats.add(seat);
					em.merge(seat);
				}
				booking = new Booking(dto.getConcertId(), dto.getDate(), bookingSeats);
				booking.setUser(user);
				em.persist(booking);
			}
			catch(NumberFormatException e) {
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.cookie(makeCookie(clientId))
						.build();
			}
			em.getTransaction().commit();
			return Response.created(URI
					.create("/concert-service/bookings/" + booking.getId()))
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
	public Response retrieveAllBookings(@CookieParam("auth") Cookie clientId) {
		if(clientId == null)
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));
				TypedQuery<Booking> bookingsQuery = em.createQuery("select b from Booking b where b.user = :user", 
															 Booking.class).setParameter("user", user);
				List<Booking> bookings = bookingsQuery.getResultList();
				List<BookingDTO> dtos = new ArrayList<BookingDTO>();
				for(Booking booking : bookings)
					dtos.add(BookingMapper.toDto(booking));
				GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(dtos) {};
				return Response.ok(entity).cookie(makeCookie(clientId)).build();
			}
			catch(NumberFormatException e) {
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.cookie(makeCookie(clientId))
						.build();
			}
		}
		finally {
			em.close();
		}
	}
	
	@GET
	@Path("bookings/{id}")
	public Response retrieveBooking(@PathParam("id") Long id, @CookieParam("auth") Cookie clientId) {
		if(clientId == null)
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));
				Booking booking = em.find(Booking.class, id);
				if(!booking.getUser().equals(user))
					return Response
							.status(Response.Status.FORBIDDEN)
							.cookie(makeCookie(clientId))
							.build();
				return Response.ok(BookingMapper.toDto(booking)).cookie(makeCookie(clientId)).build();
			}
			catch(NumberFormatException e) {
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.cookie(makeCookie(clientId))
						.build();
			}
		}
		finally {
			em.close();
		}
	}

	@GET
	@Path("subscribe/concertInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response makeSubscription(@CookieParam("auth") Cookie clientId){
		if(clientId == null)
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, Long.parseLong(clientId.getValue()));
			synchronized (subs){
				for (int i = 0; i < subs.size(); i++) {
					ConcertInfoSubscriptionDTO subdto = subDtos.get(i);
					List<Seat> seats = em.createQuery("select s from seats s where s.date = '" +
							subdto.getDate().toString() + "' and s.isBooked = :isBooked", Seat.class).setLockMode(LockModeType.OPTIMISTIC).setParameter("isBooked", true).getResultList();
					if(seats.isEmpty() == true || em.find(Concert.class, subdto.getConcertId()) == null)
						return Response
								.status(Response.Status.BAD_REQUEST)
								.cookie(makeCookie(clientId))
								.build();
					if((seats.size()/120)*100 >= subdto.getPercentageBooked()){
						ConcertInfoNotificationDTO dto = new ConcertInfoNotificationDTO(120 - seats.size());
						subs.get(i).resume(dto);
					}
				}
				subs.clear();
			}
			em.getTransaction().commit();
			return Response.ok().cookie(makeCookie(clientId)).build();
		}
		catch(NumberFormatException e) {
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}


	@POST
	@Path("subscribe/concertInfo")
	public void subscriptionNotify(final @Suspended AsyncResponse sub, @CookieParam("auth") Cookie clientId, ConcertInfoSubscriptionDTO dto) {
		if(clientId == null)
			throw new WebApplicationException(Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build());
		User user;
		try {
			em.getTransaction().begin();
			user = em.find(User.class, Long.parseLong(clientId.getValue()));
			subDtos.add(dto);
			subs.add(sub);
			em.merge(user);
			em.getTransaction().commit();
		}
		catch(NumberFormatException e) {
			throw new WebApplicationException(Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build());
		}
		finally {
			em.close();
		}
	}

	private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie = null;

        if (clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
        }

        return newCookie;
    }
}

