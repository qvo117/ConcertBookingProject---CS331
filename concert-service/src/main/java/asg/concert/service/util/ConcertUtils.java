package asg.concert.service.util;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Performer;
import asg.concert.service.domain.Seat;
import asg.concert.service.domain.User;
import asg.concert.service.services.ConcertApplication;
import asg.concert.service.services.PersistenceManager;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConcertUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertUtils.class);

    /**
     * This method will clear all seat and booking data from the database. Then, it will create all Seat objects for
     * all concerts and dates.
     */
    public static void initConcerts() {
        LOGGER.debug("initConcerts(): Creating the Application");

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            // Get all concerts
            em.getTransaction().begin();
            TypedQuery<Concert> query = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = query.getResultList();
            LOGGER.debug("initConcerts(): There are " + concerts.size() + " concerts");

            // Get all dates for all concerts
            Set<LocalDateTime> allDates = new HashSet<>();
            for (Concert c : concerts) {
                Set<LocalDateTime> dates = c.getDates();
                allDates.addAll(dates);
            }
            em.getTransaction().commit();

            LOGGER.debug("initConcerts(): There are " + allDates.size() + " concert dates");

            // For each concert date, create the seats for that date and persist them.
            int seatCount = 0;
            for (LocalDateTime date : allDates) {

                em.getTransaction().begin();
                Set<Seat> seatsForDate = TheatreLayout.createSeatsFor(date);
                for (Seat s : seatsForDate) {
                    em.persist(s);
                    seatCount++;
                }
                em.getTransaction().commit();

                // Ensures we aren't braking the EM with thousands of seat entities.
                em.clear();
            }
            //Get all performers 
            TypedQuery<Performer> query1 = em.createQuery("select c from Performer c", Performer.class);
            List<Performer> performers = query1.getResultList();
            LOGGER.debug("initConcerts(): There are " + performers.size() + " performers");
            
            //Get all seats created for all concert dates - 120 seats * 13 concert dates
            LOGGER.debug("initConcerts(): Created " + seatCount + " seats!");
            
            //Get all users 
            TypedQuery<User> query3 = em.createQuery("select c from User c", User.class);
            List<User> users = query3.getResultList();
            LOGGER.debug("initConcerts(): There are " + users.size() + " users");
            
        } finally {
            em.close();
        }
    }
}
