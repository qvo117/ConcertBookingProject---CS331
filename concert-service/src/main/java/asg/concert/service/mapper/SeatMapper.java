package asg.concert.service.mapper;

import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Seat;

public class SeatMapper {
	static Seat toDomainModel(SeatDTO dto) {
		Seat seat = new Seat(
				dto.getLabel(),
				dto.get
				dto.getPrice());
	}
}