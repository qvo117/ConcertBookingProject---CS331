package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.service.domain.Concert;

public class ConcertMapper {
	public static Concert toDomainModel(ConcertDTO dtoConcert) {
		Concert newConcert = new Concert(
				dtoConcert.getId(), 
				dtoConcert.getTitle(), 
				dtoConcert.getImageName(), 
				dtoConcert.getBlurb());
		return newConcert;
	}
	
	public static ConcertDTO toDto(Concert concert) {
		ConcertDTO dtoConcert = new ConcertDTO(
				concert.getId(),
				concert.getTitle(),
				concert.getImageName(),
				concert.getBlurb());
		return dtoConcert;
	}
}