package asg.concert.service.mapper;

import asg.concert.common.dto.BookingDTO;
import asg.concert.service.domain.Booking;

public class BookingMapper {
	static Booking toDomainModel(BookingDTO dto) {
		Booking booking = new Booking(
				dto.getConcertId(),
				dto.getDate(),
				dto.getSeats());
	}
}