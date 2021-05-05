package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import asg.concert.service.domain.ConcertInfoSubscription;

public class ConcertInfoSubscriptionMapper {
	public static ConcertInfoSubscription toDomainModel(ConcertInfoSubscriptionDTO dto) {
		ConcertInfoSubscription sub = new ConcertInfoSubscription(
				dto.getConcertId(),
				dto.getDate(),
				dto.getPercentageBooked());
		return sub;
	}
	
	public static ConcertInfoSubscriptionDTO toDto(ConcertInfoSubscription sub) {
		ConcertInfoSubscriptionDTO dto = new ConcertInfoSubscriptionDTO(
				sub.getConcertId(),
				sub.getDate(),
				sub.getPercentageBooked());
		return dto;
	}
}