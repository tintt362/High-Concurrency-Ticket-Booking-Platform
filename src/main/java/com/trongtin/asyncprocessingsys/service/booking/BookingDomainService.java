package com.trongtin.asyncprocessingsys.service.booking;

import com.trongtin.asyncprocessingsys.model.entity.Booking;

public interface BookingDomainService {

    Booking createBooking(Long ticketId, int quantity);

}
