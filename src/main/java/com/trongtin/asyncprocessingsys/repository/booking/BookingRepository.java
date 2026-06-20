package com.trongtin.asyncprocessingsys.repository.booking;

import com.trongtin.asyncprocessingsys.model.entity.Booking;

import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);
    Optional<Booking> findByBookingCode(String bookingCode);
}
