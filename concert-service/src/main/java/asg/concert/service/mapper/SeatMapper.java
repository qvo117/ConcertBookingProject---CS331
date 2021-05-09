package asg.concert.service.mapper;

import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Seat;

public class SeatMapper {
	public static Seat toDomainModel(SeatDTO dtoSeat) {
		Seat seat = new Seat(
				dtoSeat.getLabel(),
				dtoSeat.getPrice());
		return seat;
	}
	
	public static SeatDTO toDto(Seat seat) {
		SeatDTO dtoSeat = new SeatDTO(
				seat.getLabel(),
				seat.getPrice());
		return dtoSeat;
	}
}