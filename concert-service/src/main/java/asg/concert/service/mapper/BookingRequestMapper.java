package asg.concert.service.mapper;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.domain.BookingRequest;

public class BookingRequestMapper {
	
	static BookingRequest toDomainModel(BookingRequestDTO dtoBookingRequest) {
		BookingRequest bookingRequest = new BookingRequest(
				dtoBookingRequest.getConcertId(),
				dtoBookingRequest.getDate(),
				dtoBookingRequest.getSeatLabels());
		return bookingRequest;
	}
	
	static BookingRequestDTO toDto(BookingRequest bookingRequest) {
		BookingRequestDTO dtoBookingRequest = new BookingRequestDTO(
				bookingRequest.getConcertId(),
				bookingRequest.getDate(),
				bookingRequest.getSeatLabels());
		return dtoBookingRequest;
	}
}