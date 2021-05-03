package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import asg.concert.service.domain.ConcertInfoSubscription;

public class ConcertInfoSubscriptionMapper {
	static ConcertInfoSubscription toDomainModel(ConcertInfoSubscriptionDTO dto) {
		ConcertInfoSubscription sub = new ConcertInfoSubscription(
				dto.getConcertId(),
				dto.getDate(),
				dto.getPercentageBooked());
		return sub;
	}
	
	static ConcertInfoSubscriptionDTO toDto(ConcertInfoSubscription sub) {
		ConcertInfoSubscriptionDTO dto = new ConcertInfoSubscriptionDTO(
				sub.getConcertId(),
				sub.getDate(),
				sub.getPercentageBooked());
		return dto;
	}
}