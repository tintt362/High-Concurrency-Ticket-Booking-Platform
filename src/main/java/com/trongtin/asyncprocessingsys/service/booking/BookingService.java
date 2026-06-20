package com.trongtin.asyncprocessingsys.service.booking;

import com.trongtin.asyncprocessingsys.dto.response.BookingDTO;
import com.trongtin.asyncprocessingsys.model.command.CreateBookingCommand;
import com.trongtin.asyncprocessingsys.model.entity.Booking;

public interface BookingService {

    BookingDTO createBooking(CreateBookingCommand command);

}
