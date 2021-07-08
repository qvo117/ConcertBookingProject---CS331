package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import asg.concert.service.domain.ConcertInfoSubscription;

public class ConcertInfoSubscriptionMapper {
	public static ConcertInfoSubscription toDomainModel(ConcertInfoSubscriptionDTO dtoConcert) {
		ConcertInfoSubscription newConcert = new ConcertInfoSubscription(
				dtoConcert.getConcertId(), 
				dtoConcert.getDate(), 
				dtoConcert.getPercentageBooked());
		return newConcert;
	}
	
	public static ConcertInfoSubscriptionDTO toDto(ConcertInfoSubscription concert) {
		ConcertInfoSubscriptionDTO dtoConcert = new ConcertInfoSubscriptionDTO(
				concert.getConcertId(),
				concert.getDate(),
				concert.getPercentageBooked());
		return dtoConcert;
	}
}