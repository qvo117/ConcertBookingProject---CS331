package asg.concert.service.mapper;

import java.util.ArrayList;
import java.util.List;

import asg.concert.common.dto.BookingDTO;
import asg.concert.service.domain.Booking;
import asg.concert.service.domain.Seat;
import asg.concert.common.dto.SeatDTO;

public class BookingMapper {
	
	public static Booking toDomainModel(BookingDTO dtoBooking) {
		List<Seat> seats = new ArrayList<>();
		List<SeatDTO> seatsDTO = new ArrayList<>();
		seatsDTO = dtoBooking.getSeats();
		for (SeatDTO dtoSeat: seatsDTO) {
			Seat seat = SeatMapper.toDomainModel(dtoSeat);
			seats.add(seat);
		}
		Booking booking = new Booking(
				dtoBooking.getConcertId(),
				dtoBooking.getDate(),
				seats);
		return booking;
	}
	
	public static BookingDTO toDto(Booking booking) {
		List<SeatDTO> seatsDTO = new ArrayList<>();
		List<Seat> seats = new ArrayList<>();
		seats = booking.getSeats();
		for (Seat seat: seats) {
			SeatDTO dtoSeat = SeatMapper.toDto(seat);
			seatsDTO.add(dtoSeat);
		}
		BookingDTO dtoBooking = new BookingDTO(
				booking.getConcertId(),
				booking.getDate(),
				seatsDTO);
		return dtoBooking;
	}
}