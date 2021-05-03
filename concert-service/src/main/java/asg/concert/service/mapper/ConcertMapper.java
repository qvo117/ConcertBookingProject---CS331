package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.service.domain.Concert;

public class ConcertMapper {
	static Concert toDomainModel(ConcertDTO dto) {
		Concert newConcert = new Concert(
				dto.getId(), 
				dto.getTitle(), 
				dto.getImageName(), 
				dto.getBlurb());
		return newConcert;
	}
	
	static ConcertDTO toDto(Concert concert) {
		ConcertDTO dto = new ConcertDTO(
				concert.getId(),
				concert.getTitle(),
				concert.getImageName(),
				concert.getBlurb());
		return dto;
	}
}