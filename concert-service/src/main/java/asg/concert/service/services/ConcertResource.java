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
	private static final List<AsyncResponse> subs = new ArrayList<AsyncResponse>();
  private static List<ConcertInfoSubscriptionDTO> subDtos = new ArrayList<ConcertInfoSubscriptionDTO>();

	//Gets the concert with the given id
	@GET
	@Path("concerts/{id}")
	public Response retrieveConcert(@PathParam("id") Long id, @CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			Concert concert = em.find(Concert.class, id);
			em.getTransaction().commit();
			//checks if concert with the given id is non existent
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

	//Retrieve all the concerts
	@GET
	@Path("concerts")
	public Response retrieveAllConcerts(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);	//Queries all the concerts
			List<Concert> concerts = concertQuery.getResultList(); //add it to list
			em.getTransaction().commit();
			List<ConcertDTO> dtos = new ArrayList<ConcertDTO>();
			for(Concert concert : concerts) {
				ConcertDTO dto = ConcertMapper.toDto(concert);	//transfers all the concert list to concertDTO list
				//get a list of Performers
				List<PerformerDTO> performers = new ArrayList<>();
				for (Performer performer: concert.getPerformers()) {
					performers.add(PerformerMapper.toDto(performer));	//get all the performer in concert
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

	//Take summaries of all the concerts
	@GET
	@Path("concerts/summaries")
	public Response retrieveSummaries(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class); //gets concert from query
			List<Concert> concerts = concertQuery.getResultList();	//Creates concert list out of query items
			em.getTransaction().commit();
			List<ConcertSummaryDTO> summaries = new ArrayList<ConcertSummaryDTO>();
			//adds summaries to summaryDTO
			for(Concert concert : concerts) {
				ConcertSummaryDTO summary = new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
				summaries.add(summary);
			}
			GenericEntity<List<ConcertSummaryDTO>> entity = new GenericEntity<List<ConcertSummaryDTO>>(summaries) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build(); //returns summaryDTO with all the summaries
		}
		finally {
			em.close();
		}
	}

	//Retrieves a performer with a specific id
	@GET
	@Path("performers/{id}")
	public Response retrievePerformer(@PathParam("id") Long id, @CookieParam("clientId") Cookie clientId){
		try {
			em.getTransaction().begin();
			Performer performer = em.find(Performer.class, id); //gets performer using the id
			em.getTransaction().commit();
			if(performer == null) {								//checks if performer is not null
				return Response
						.status(Response.Status.NOT_FOUND)
						.cookie(makeCookie(clientId))
						.build();
			}
			PerformerDTO dto = PerformerMapper.toDto(performer);		//maps it to a DTO
			return Response.ok(dto).cookie(makeCookie(clientId)).build();	//returns performer
		}
		finally {
			em.close();
		}
	}

	//retrieves all performers
	@GET
	@Path("performers")
	public Response retrieveAllPerformers(@CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class); //Query for all performers
			List<Performer> performers = performerQuery.getResultList();
			em.getTransaction().commit();
			List<PerformerDTO> dtos = new ArrayList<PerformerDTO>();
			for(Performer performer: performers) {
				dtos.add(PerformerMapper.toDto(performer)); //maps performers to performer dto
			}
			GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(dtos) {};
			return Response.ok(entity).cookie(makeCookie(clientId)).build(); //send dto back a generic entity
		}
		finally {
			em.close();
		}
	}
	//logs user in
	@POST
	@Path("login")
	public Response login(UserDTO dto, @CookieParam("clientId") Cookie clientId){
		try {
			em.getTransaction().begin();
			TypedQuery<User> userQuery = em.createQuery("select u from User u", User.class).setLockMode(LockModeType.OPTIMISTIC); //queries users
			List<User> users = userQuery.getResultList();
			em.getTransaction().commit();
			//check to see if all auth requirements match, i.e the password and username.
			for(User user: users) {
				if(user.getUsername().equals(dto.getUsername()) && user.getPassword().equals(dto.getPassword())){
					NewCookie authCookie = new NewCookie("auth", user.getId().toString());	//if the username and password match create new auth cookie
					return Response
							.status(Response.Status.OK)
							.cookie(authCookie)
							.build();
				}
			}
			//response when password or user is wrong
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}

	//Retrieves all seats by date
	@GET
	@Path("seats/{date}")
	public Response retrieveSeats(@PathParam("date") String dateString, @QueryParam("status") BookingStatus status,
								  @CookieParam("clientId") Cookie clientId) {
		try {
			em.getTransaction().begin();
			TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = '" + dateString + "'", Seat.class).setLockMode(LockModeType.OPTIMISTIC); //seat query by date
			List<Seat> seats = seatQuery.getResultList();
			em.getTransaction().commit();
			List<SeatDTO> dtos = new ArrayList<SeatDTO>();
			//Check for Booking status and seat availiblity and adds it to the seatDTO if the requirements fit
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

	//Makes a booking
	@POST
	@Path("bookings")
	public Response makeBooking(BookingRequestDTO dto, @CookieParam("auth") Cookie clientId) {
		//checks if client is logged in
		if(clientId == null)
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			Booking booking;
			Concert concert = em.find(Concert.class, dto.getConcertId());	//finds concert that matches the bookingRequestDTO
			if(concert == null || !concert.getDates().contains(dto.getDate())) //Check if there is a concert on a certain date
				return Response												//reponse if concert does not exsist
						.status(Response.Status.BAD_REQUEST)
						.cookie(makeCookie(clientId))
						.build();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));	//find user based on specified id
				List<Seat> bookingSeats = new ArrayList<Seat>();
				for(String label : dto.getSeatLabels()) {
					TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = '" + dto.getDate().toString()
																+ "' and s.label = '" + label + "'", Seat.class).setLockMode(LockModeType.OPTIMISTIC); //queries seats where the date and label match
					Seat seat = seatQuery.getSingleResult();
					if(seat.getIsBooked()) {
						return Response										//response if the seat is already booked
								.status(Response.Status.FORBIDDEN)
								.cookie(makeCookie(clientId))
								.build();
					}
					seat.setIsBooked(true); //if not booked
					bookingSeats.add(seat);
					em.merge(seat);
				}
				booking = new Booking(dto.getConcertId(), dto.getDate(), bookingSeats); //bookingDTO based on the the newly booked seats
				booking.setUser(user);
				em.persist(booking);
			}
			catch(NumberFormatException e) {						//when the user login fails
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.cookie(makeCookie(clientId))
						.build();
			}
			em.getTransaction().commit();
			return Response.created(URI											//response for successful booking of seats
					.create("/concert-service/bookings/" + booking.getId()))
					.status(Response.Status.CREATED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}

	//Retrieve all the bookings based on user id
	@GET
	@Path("bookings")
	public Response retrieveAllBookings(@CookieParam("auth") Cookie clientId) {
		if(clientId == null)									//checks login
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));	//gets user based on id
				TypedQuery<Booking> bookingsQuery = em.createQuery("select b from Booking b where b.user = :user", 
															 Booking.class).setParameter("user", user); 		//Queries all the bookings related to the user
				List<Booking> bookings = bookingsQuery.getResultList();
				List<BookingDTO> dtos = new ArrayList<BookingDTO>();
				for(Booking booking : bookings)							//all the booking list to bookingDTO list
					dtos.add(BookingMapper.toDto(booking));
				GenericEntity<List<BookingDTO>> entity = new GenericEntity<List<BookingDTO>>(dtos) {};
				return Response.ok(entity).cookie(makeCookie(clientId)).build();	//Returns all the bookings
			}
			catch(NumberFormatException e) {							//response for failed login
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

	//Retrieves booking based on booking id
	@GET
	@Path("bookings/{id}")
	public Response retrieveBooking(@PathParam("id") Long id, @CookieParam("auth") Cookie clientId) {
		if(clientId == null)		//checking login
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user;
			try {
				user = em.find(User.class, Long.parseLong(clientId.getValue()));
				Booking booking = em.find(Booking.class, id);	//finds booking that matches the given booking id
				if(!booking.getUser().equals(user))
					return Response										//if the user that booked it doesn't match the booking's user it return this
							.status(Response.Status.FORBIDDEN)
							.cookie(makeCookie(clientId))
							.build();
				//response if the booking with the given booking id matches with the user of that booking
				return Response.ok(BookingMapper.toDto(booking)).cookie(makeCookie(clientId)).build();
			}
			catch(NumberFormatException e) {		//failed login
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

	//Notifies that if the percentage if available seats is below a certain amount
	@GET
	@Path("subscribe/concertInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response subscriptionNotify(@CookieParam("auth") Cookie clientId){
		if(clientId == null)					//checks login
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, Long.parseLong(clientId.getValue()));
			synchronized (subs){
				for (int i = 0; i < subs.size(); i++) {							//loops through the subDTO and subs lists
					ConcertInfoSubscriptionDTO subdto = subDtos.get(i);
					//Query for seats matching date and if the seat is booked
					List<Seat> seats = em.createQuery("select s from seats s where s.date = '" +
							subdto.getDate().toString() + "' and s.isBooked = :isBooked", Seat.class).setLockMode(LockModeType.OPTIMISTIC).setParameter("isBooked", true).getResultList();
					if(seats.isEmpty() == true || em.find(Concert.class, subdto.getConcertId()) == null) //If no seats exist for that day or if the concert id refers to a concert that does not exsist
						return Response
								.status(Response.Status.BAD_REQUEST)
								.cookie(makeCookie(clientId))
								.build();
					//Otherwise it checks if seats left percentage is greater than or equal to the subscription percentage
					if((seats.size()/120)*100 >= subdto.getPercentageBooked()){
						ConcertInfoNotificationDTO dto = new ConcertInfoNotificationDTO(120 - seats.size()); //seats remaining
						subs.get(i).resume(dto);
					}
				}
				subs.clear();
			}
			em.getTransaction().commit();
			return Response.ok().cookie(makeCookie(clientId)).build();
		}
		catch(NumberFormatException e) {		//response failed login
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build();
		}
		finally {
			em.close();
		}
	}

	//User subscribes
	@POST
	@Path("subscribe/concertInfo")
	public void makeSubscription(final @Suspended AsyncResponse sub, @CookieParam("auth") Cookie clientId, ConcertInfoSubscriptionDTO dto) {
		if(clientId == null)		//checks login
			throw new WebApplicationException(Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build());
		User user;
		try {
			em.getTransaction().begin();
			user = em.find(User.class, Long.parseLong(clientId.getValue())); //finds user based on clientId
			subDtos.add(dto); 										//Adds subInfoDTO to subDTO list
			subs.add(sub);											//Adds sub asyncResponse to a sub responder list
			em.merge(user);
			em.getTransaction().commit();
		}
		catch(NumberFormatException e) {		//failed login
			throw new WebApplicationException(Response
					.status(Response.Status.UNAUTHORIZED)
					.cookie(makeCookie(clientId))
					.build());
		}
		finally {
			em.close();
		}
	}

	//Makes cookies
	private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie = null;

        if (clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
        }

        return newCookie;
    }
}

