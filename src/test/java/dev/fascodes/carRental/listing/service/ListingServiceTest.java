package dev.fascodes.carRental.listing.service;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.mapper.AddListingMapper;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock private AddListingMapper addListingMapper;
    @Mock private ListingRepository listingRepository;
    @Mock private CarRepository carRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ListingService listingService;

    @Test
    void addListing_throwsForbidden_whenCarNotOwnedByUser() {
        User caller = new User();
        caller.setId(1L);
        caller.setEmail("caller@test.com");

        User actualOwner = new User();
        actualOwner.setId(2L);

        Car car = new Car();
        car.setOwner(actualOwner);

        AddListingRequest request = new AddListingRequest();
        request.setCarId(10L);

        when(userRepository.findByEmail("caller@test.com")).thenReturn(Optional.of(caller));
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> listingService.addListing(request, "caller@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void addListing_throwsNotFound_whenCarDoesNotExist() {
        User caller = new User();
        caller.setEmail("caller@test.com");

        AddListingRequest request = new AddListingRequest();
        request.setCarId(10L);

        when(userRepository.findByEmail("caller@test.com")).thenReturn(Optional.of(caller));
        when(carRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> listingService.addListing(request, "caller@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeListing_throwsNotFound_whenListingDoesNotExist() {
        when(listingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> listingService.removeListing(1L, "user@test.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeListing_throwsForbidden_whenCallerIsNotOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Listing listing = new Listing();
        listing.setUser(owner);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> listingService.removeListing(1L, "other@test.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // This is a repository test if a repository exclusive file is added move it there
    @Test
    void removeListing_deletesListing_whenCallerIsOwner() {
        User owner = new User();
        owner.setEmail("owner@test.com");

        Listing listing = new Listing();
        listing.setUser(owner);

        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        listingService.removeListing(1L, "owner@test.com");

        verify(listingRepository).delete(listing);
    }
}
