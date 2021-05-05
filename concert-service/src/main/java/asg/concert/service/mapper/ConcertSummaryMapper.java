package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertSummaryDTO;
import asg.concert.service.domain.ConcertSummary;

public class ConcertSummaryMapper {
	public static ConcertSummary toDomainModel(ConcertSummaryDTO dtoSummary) {
		ConcertSummary summary = new ConcertSummary(
				dtoSummary.getId(),
				dtoSummary.getTitle(),
				dtoSummary.getImageName());
		return summary;
	}
	
	public static ConcertSummaryDTO toDto(ConcertSummary summary) {
		ConcertSummaryDTO dto = new ConcertSummaryDTO(
				summary.getId(),
				summary.getTitle(),
				summary.getImageName());
		return dto;
	}
}