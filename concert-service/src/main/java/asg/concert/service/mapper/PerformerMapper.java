package asg.concert.service.mapper;

import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Performer;

public class PerformerMapper {
	public static Performer toDomainModel(PerformerDTO dto) {
		Performer newPerformer = new Performer(
				dto.getId(),
				dto.getName(),
				dto.getImageName(),
				dto.getGenre(),
				dto.getBlurb());
		return newPerformer;
	}
	
	public static PerformerDTO toDto(Performer performer) {
		PerformerDTO dto = new PerformerDTO(
				performer.getId(),
				performer.getName(),
				performer.getImage_Name(),
				performer.getGenre(),
				performer.getBlurb());
		return dto;
	}
}