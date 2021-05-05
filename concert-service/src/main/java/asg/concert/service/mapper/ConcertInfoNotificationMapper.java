package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertInfoNotificationDTO;
import asg.concert.service.domain.ConcertInfoNotification;

public class ConcertInfoNotificationMapper {
	public static ConcertInfoNotification toDomainModel(ConcertInfoNotificationDTO dto) {
		ConcertInfoNotification sub = new ConcertInfoNotification(
				dto.getNumSeatsRemaining());
		return sub;
	}
	
	public static ConcertInfoNotificationDTO toDto(ConcertInfoNotification sub) {
		ConcertInfoNotificationDTO dto = new ConcertInfoNotificationDTO(
				sub.getNumSeatsRemaining());
		return dto;
	}
}