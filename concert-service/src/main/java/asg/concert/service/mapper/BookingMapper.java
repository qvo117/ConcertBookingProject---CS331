package asg.concert.service.mapper;

import java.util.ArrayList;
import java.util.List;

import asg.concert.common.dto.BookingDTO;
import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Booking;
import asg.concert.service.domain.Seat;

public class BookingMapper {
	public static BookingDTO toDto(Booking booking) {
		List<SeatDTO> seatDtos = new ArrayList<SeatDTO>();
		for(Seat seat : booking.getSeats())
			seatDtos.add(SeatMapper.toDto(seat));
		BookingDTO bookingDto = new BookingDTO(
				booking.getConcertId(), 
				booking.getDate(), 
				seatDtos);
		return bookingDto;
	}
}
