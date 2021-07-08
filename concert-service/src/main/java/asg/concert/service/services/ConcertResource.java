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
import asg.concert.service.jaxrs.LocalDateTimeParam;
import asg.concert.service.mapper.PerformerMapper;
import asg.concert.service.mapper.SeatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.service.mapper.BookingMapper;
import asg.concert.service.mapper.ConcertInfoSubscriptionMapper;
import asg.concert.service.mapper.ConcertMapper;
import asg.concert.service.common.Config;

@Path("/concert-service")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class ConcertResource {
	private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
	EntityManager em = PersistenceManager.instance().createEntityManager();
	//For asynchronous responses
	private static final List<AsyncResponse> subs = new ArrayList<AsyncResponse>();

	/**
	 * Class to implement a simple REST Web service for managing Concerts using Java technologies like JAX-RS and JPQL.
	 * <p>
	 * ConcertResource implements a WEB service with the following interface:
	 
	 * 
	 * <p>
	 * - POST   <base-uri>/concerts
	 * Creates a new Concert. The HTTP post message contains a
	 * representation of the Concert to be created. The HTTP Response
	 * message returns a Location header with the URI of the new Concert
	 * and a status code of 201.
	 * <p>
	 * - DELETE <base-uri>/concerts
	 * Deletes all Concerts, returning a status code of 204.
	 * <p>
	 * Where a HTTP request message doesn't contain a cookie named clientId
	 * (Config.CLIENT_COOKIE), the Web service generates a new cookie, whose value
	 * is a randomly generated UUID. The Web service returns the new cookie as part
	 * of the HTTP response message.
	 */
	
	/*GET    <base-uri>/concerts/{id}
	 Retrieves a Concert based on its unique id. 
	 The HTTP response message has a status code of either 200 OK or 404 Not Found, depending on
	 whether the specified Concert is found.
	 */
	@GET
	@Path("concerts/{id}")
	public Response retrieveConcert(@PathParam("id") Long id) {
		try {
			em.getTransaction().begin();
			Concert concert = em.find(Concert.class, id);
			em.getTransaction().commit();
			//Check if concert with the given id exists
			if(concert == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			//Convert to ConcertDTO, attach a list of Performers and concert dates to it to send back to client
			ConcertDTO concertDto = ConcertMapper.toDto(concert);
			List<PerformerDTO> performers = new ArrayList<>();
			for (Performer performer: concert.getPerformers()) {
				performers.add(PerformerMapper.toDto(performer));
			};
			concertDto.setPerformers(performers);
			List<LocalDateTime> dates = new ArrayList<>();
			for (LocalDateTime date: concert.getDates()) {
				dates.add(date);
			};
			concertDto.setDates(dates);
			return Response.ok(concertDto).build();
		}
		finally {
			em.close();
		}
	}

	/*
	 * GET <base-uri>/concerts 
	 * Retrieves a collection of Concerts. 
	 * The HTTP response message returns 200 OK.
	 */
	@GET
	@Path("concerts")
	public Response retrieveAllConcerts() {
		try {
			em.getTransaction().begin();
			//Query all concerts
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);	
			List<Concert> concerts = concertQuery.getResultList(); 
			em.getTransaction().commit();
			//Convert to a list of ConcertDTOs, attach a list of Performers and concert dates to each ConcertDTO to send back to client
			List<ConcertDTO> concertDtos = new ArrayList<ConcertDTO>();
			for(Concert concert : concerts) {
				ConcertDTO concertDto = ConcertMapper.toDto(concert);	
				List<PerformerDTO> performerDtos = new ArrayList<>();
				for (Performer performer: concert.getPerformers()) {
					performerDtos.add(PerformerMapper.toDto(performer));	
				};
				concertDto.setPerformers(performerDtos);
				List<LocalDateTime> dates = new ArrayList<>();
				for (LocalDateTime date: concert.getDates()) {
					dates.add(date);
				};
				concertDto.setDates(dates);
				concertDtos.add(concertDto);
			}
			/*Wrap the list by a javax.ws.rs.core.GenericEntity that stores the generic type info
			 * to marshal/unmarshal generically-typed objects correctly
			*/
			GenericEntity<List<ConcertDTO>> entity = new GenericEntity<List<ConcertDTO>>(concertDtos) {};
			return Response.ok(entity).build();
		}
		finally {
			em.close();
		}
	}

	/*
	 * GET <base-uri>/concerts/summaries 
	 * Retrieves a collection of all concert summaries. 
	 * The HTTP response message returns 200 OK.
	 */
	@GET
	@Path("concerts/summaries")
	public Response retrieveSummaries() {
		try {
			em.getTransaction().begin();
			//Retrieve all concerts and convert to ConcertSummaryDTO objects.
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class); 
			List<Concert> concerts = concertQuery.getResultList();	
			em.getTransaction().commit();
			List<ConcertSummaryDTO> summaries = new ArrayList<ConcertSummaryDTO>();
			for(Concert concert : concerts) {
				ConcertSummaryDTO summary = new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
				summaries.add(summary);
			}
			//Wrap the list by a javax.ws.rs.core.GenericEntity that stores the generic type info	
			GenericEntity<List<ConcertSummaryDTO>> entity = new GenericEntity<List<ConcertSummaryDTO>>(summaries) {};
			return Response.ok(entity).build(); 
		}
		finally {
			em.close();
		}
	}

	/*GET    <base-uri>/performers/{id}
	 Retrieves a Performer based on its unique id. 
	 The HTTP response message has a status code of either 200 OK or 404 Not Found, depending on
	 whether the specified Performer is found.
	 */
	@GET
	@Path("performers/{id}")
	public Response retrievePerformer(@PathParam("id") Long id){
		try {
			em.getTransaction().begin();
			Performer performer = em.find(Performer.class, id); 
			em.getTransaction().commit();
			if(performer == null) {								
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			//return a performerDTO
			PerformerDTO performerDto = PerformerMapper.toDto(performer);		
			return Response.ok(performerDto).build();	
		}
		finally {
			em.close();
		}
	}

	/*
	 * GET <base-uri>/performers 
	 * Retrieves a collection of Performers. 
	 * The HTTP response message returns 200 OK.
	 */
	@GET
	@Path("performers")
	public Response retrieveAllPerformers() {
		try {
			em.getTransaction().begin();
			//Query for all performers and convert to PerformerDtos to send back to client
			TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class); 
			List<Performer> performers = performerQuery.getResultList();
			em.getTransaction().commit();
			List<PerformerDTO> performerDtos = new ArrayList<PerformerDTO>();
			for(Performer performer: performers) {
				performerDtos.add(PerformerMapper.toDto(performer)); 
			}
			//Wrap the list by a javax.ws.rs.core.GenericEntity that stores the generic type info	
			GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performerDtos) {};
			return Response.ok(entity).build(); 
		}
		finally {
			em.close();
		}
	}
	
	/*POST    <base-uri>/login
	 The user posts their credentials to log in. 
	 The HTTP response message has a status code of either 200 OK or 401 Unauthorized, depending on
	 whether the credentials are correct.
	 */
	@POST
	@Path("login")
	public Response login(UserDTO userDto){
		try {
			em.getTransaction().begin();
			TypedQuery<User> userQuery = em.createQuery("select u from User u", User.class).setLockMode(LockModeType.OPTIMISTIC); 
			List<User> users = userQuery.getResultList();
			em.getTransaction().commit();
			//Checks if a user has that password and username, if they match then create an authentication token i.e. cookie.
			for(User user: users) {
				//Use .equals() to compare the values only, while == compares the memory address as well.
				if( user.getUsername().equals(userDto.getUsername()) && user.getPassword().equals(userDto.getPassword()) ) {
					NewCookie authToken = new NewCookie("auth", user.getId().toString());	
					return Response.status(Response.Status.OK).cookie(authToken).build();
				}
			}
			//When credentials is wrong
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		finally {
			em.close();
		}
	}

	/*GET    <base-uri>/seats/{date}
	 Retrieves all seats for a particular concert date. 
	 The HTTP response message has a status code of 200 OK.
	 */
	@GET
	@Path("seats/{date}")
	public Response retrieveSeats(@PathParam("date") LocalDateTimeParam dateParam, @QueryParam("status") BookingStatus status) {
		try {
			em.getTransaction().begin();
			LocalDateTime date = dateParam.getLocalDateTime();
			//Query all seats on the given date
			TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = :date", Seat.class).setParameter("date", date)
					.setLockMode(LockModeType.OPTIMISTIC); 
			List<Seat> seats = seatQuery.getResultList();
			em.getTransaction().commit();
			//Depending on which BookingStatus the client wants to see, we construct a list of SeatDtos that meet their requirements.
			List<SeatDTO> seatDtos = new ArrayList<SeatDTO>();
			for(Seat seat : seats) {
				if(status.equals(BookingStatus.Booked) && seat.getIsBooked()){
					seatDtos.add(SeatMapper.toDto(seat));
				}
				else if(status.equals(BookingStatus.Unbooked) && !seat.getIsBooked()) {
					seatDtos.add(SeatMapper.toDto(seat));
				}
				else if(status.equals(BookingStatus.Any)) {
					seatDtos.add(SeatMapper.toDto(seat));
				}
			}
			//Wrap the list by a javax.ws.rs.core.GenericEntity that stores the generic type info
			GenericEntity<List<SeatDTO>> entity = new GenericEntity<List<SeatDTO>>(seatDtos) {};
			return Response.ok(entity).build();
		}
		finally {
			em.close();
		}
	}

	/*POST    <base-uri>/bookings
	 Make a booking. 
	 The HTTP response message has a status code of either 201 Created or 400 Bad Requests, depending on
	 whether the concert booking is successful.
	 */
	@POST
	@Path("bookings")
	public Response makeBooking(BookingRequestDTO bookingReqDto, @CookieParam("auth") Cookie authToken) {
		//checks if client is logged in
		if(authToken == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		//extract info from the booking request
		long concertId = bookingReqDto.getConcertId();
		LocalDateTime date = bookingReqDto.getDate();
		List<String> seatLabels = bookingReqDto.getSeatLabels();
		try {
			em.getTransaction().begin();
			//Checks whether the concert and date exists
			Concert concert = em.find(Concert.class, concertId);	
			if(concert == null || !concert.getDates().contains(date)) 
				return Response.status(Response.Status.BAD_REQUEST).build();
			
			//Prepare the booking for the user
			User user = em.find(User.class, Long.parseLong(authToken.getValue()));	
			//Get each seat requested by the user and check if it has been booked yet
			List<Seat> bookingSeats = new ArrayList<Seat>();
			for(String label : seatLabels) {
				TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = :date and s.label = :label", Seat.class)
						.setParameter("date", date).setParameter("label", label).setLockMode(LockModeType.OPTIMISTIC); 
				Seat seat = seatQuery.getSingleResult();
				//If seat has been booked
				if(seat.getIsBooked()) {
					return Response.status(Response.Status.FORBIDDEN).build();
				}
				//If seat has not been booked, book it and update it
				seat.setIsBooked(true);
				bookingSeats.add(seat);
				em.merge(seat);
			}
			Booking booking = new Booking(concertId, date, bookingSeats); 
			booking.setUser(user);
			em.persist(booking);
			
			/*Count number of booked seats for the concert, and send to another method to deal with clients that have
			 * subscribed to "watch" this concert / date, 
			 * and be notified when those concert / date are about to sell out
			 */
			List<Seat> seats = em.createQuery("select s from Seat s where s.date = :date", Seat.class)
					.setParameter("date", date).getResultList();
			em.getTransaction().commit();
			int totalBooked = 0;
			for (Seat s: seats) {
				if(s.getIsBooked()) {
					totalBooked+=1;
				}
			}
			subscriptionNotify(concertId, date, totalBooked, seats.size());
			
			//Return the location header of a successful booking
			return Response.created( URI.create("/concert-service/bookings/" + booking.getId()) ).status(Response.Status.CREATED).build();
		}
		finally {
			em.close();
		}
	}

	/*GET    <base-uri>/bookings
	 Retrieves all bookings for a user. 
	 The HTTP response message has a status code of 200 OK.
	 */
	@GET
	@Path("bookings")
	public Response retrieveAllBookings(@CookieParam("auth") Cookie authToken) {
		if(authToken == null)									
			return Response.status(Response.Status.UNAUTHORIZED).build();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, Long.parseLong(authToken.getValue()));	
			TypedQuery<Booking> bookingsQuery = em.createQuery("select b from Booking b where b.user = :user", Booking.class)
					.setParameter("user", user); 		
			List<Booking> bookings = bookingsQuery.getResultList();
			//Convert to a list of bookingDtos
			List<BookingDTO> bookingDtos = new ArrayList<BookingDTO>();
			for(Booking booking : bookings)							
				bookingDtos.add(BookingMapper.toDto(booking));
			//Wrap the list by a javax.ws.rs.core.GenericEntity that stores the generic type info
			GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(bookingDtos) {};
			return Response.ok(entity).build();	
		}
		finally {
			em.close();
		}
	}
	
	/*GET    <base-uri>/bookings{id}
	 Retrieves a particular booking based on the id. 
	 The HTTP response message has a status code of 200 OK.
	 */
	@GET
	@Path("bookings/{id}")
	public Response retrieveBooking(@PathParam("id") Long id, @CookieParam("auth") Cookie authToken) {
		//Check if user is authenticated
		if(authToken == null)		
			return Response.status(Response.Status.UNAUTHORIZED).build();
		try {
			em.getTransaction().begin();
			/*Make sure that the user can only check their own booking
			 * and not someone else's.
			 */
			User user = em.find( User.class, Long.parseLong(authToken.getValue()) );
			Booking booking = em.find(Booking.class, id);	
			if( !(booking.getUser() == user))
				return Response.status(Response.Status.FORBIDDEN).build();
			//Return bookingDTO to the user
			return Response.ok(BookingMapper.toDto(booking)).build();
		}
		finally {
			em.close();
		}
	}
	
	/*POST   <base-uri>/subscribes/concertInfo
	 Users can subscribe to be notified when a concert reaches a certain percentage of seats being booked. 
	 This involves asynchronous responses.
	 */
	@POST
	@Path("subscribe/concertInfo")
	public void makeSubscription(@Suspended AsyncResponse sub, @CookieParam("auth") Cookie authToken, ConcertInfoSubscriptionDTO subDto) {
		if(authToken == null)	{	//checks login
			sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}
		try {
			em.getTransaction().begin();
			//Check if the concert and date exist	
			Concert concert = em.find(Concert.class, subDto.getConcertId());	
			if( (concert == null) || !concert.getDates().contains(subDto.getDate()) ) {
				sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
				return;
			}	
			//Adds sub to a list of AsyncResponses
			subs.add(sub);											
			em.persist(ConcertInfoSubscriptionMapper.toDomainModel(subDto));
			em.getTransaction().commit();
		}
		finally {
			em.close();
		}
	}
	
	/*This method is to check the % of seats booked after a new booking has been made for the given concert
	 * to see if a notification needs to be sent back to clients that are subscribed to be notified 
	 * when a certain % of seats booked for that concert has been reached
	 */
	public void subscriptionNotify(long concertId, LocalDateTime date, int seatsBooked, int totalSeats){
		try {
			em.getTransaction().begin();
			//Get all concertInfoSubscriptions for that concert
			TypedQuery<ConcertInfoSubscription> concertInfoSubsQuery = em.createQuery("select cs from ConcertInfoSubscription cs where cs.concertId = :concertId and cs.date = :date", ConcertInfoSubscription.class)
					.setParameter("concertId", concertId).setParameter("date", date);
			List<ConcertInfoSubscription> concertInfoSubs = concertInfoSubsQuery.getResultList();
			em.getTransaction().commit();
			for (ConcertInfoSubscription cs: concertInfoSubs) {	
				//Check whether the % of seats booked has been reached
				if((int)( ( (float)seatsBooked/(float)totalSeats )*100) >= cs.getPercentageBooked()){
					ConcertInfoNotificationDTO notification = new ConcertInfoNotificationDTO(totalSeats - seatsBooked); //seats remaining
					LOGGER.debug("Notify subscribers!");
					synchronized (subs) {
						for (AsyncResponse sub: subs) {
							sub.resume(Response.ok(notification).build());
						}
						subs.clear();
					}
				}
			}	
		}
		finally {
			em.close();
		}
	}

}

