package asg.concert.service.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Performer;
import asg.concert.service.mapper.PerformerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.common.dto.ConcertSummaryDTO;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.ConcertSummary;
import asg.concert.service.mapper.ConcertMapper;
import asg.concert.service.mapper.ConcertSummaryMapper;
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
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concerts c", Concert.class);
			List<Concert> concerts = concertQuery.getResultList();
			em.getTransaction().commit();
			List<ConcertDTO> dtos = new ArrayList<ConcertDTO>();
			for(Concert concert : concerts) {
				dtos.add(ConcertMapper.toDto(concert));
			}
			return Response.ok(dtos).cookie(makeCookie(clientId)).build();
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
			TypedQuery<Concert> concertQuery = em.createQuery("select c from Concerts c", Concert.class);
			List<Concert> concerts = concertQuery.getResultList();
			em.getTransaction().commit();
			List<ConcertSummaryDTO> summaries = new ArrayList<ConcertSummaryDTO>();
			for(Concert concert : concerts) {
				ConcertSummary summary = new ConcertSummary(concert.getId(), concert.getTitle(), concert.getImageName());
				summaries.add(ConcertSummaryMapper.toDto(summary));
			}
			return Response.ok(summaries).cookie(makeCookie(clientId)).build();
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
			TypedQuery<Performer> performerQuery = em.createQuery("select c from Performers c", Performer.class);
			List<Performer> performers = performerQuery.getResultList();
			em.getTransaction().commit();
			List<PerformerDTO> dtos = new ArrayList<PerformerDTO>();
			for(Performer performer: performers) {
				dtos.add(PerformerMapper.toDto(performer));
			}
			return Response.ok(dtos).cookie(makeCookie(clientId)).build();
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
