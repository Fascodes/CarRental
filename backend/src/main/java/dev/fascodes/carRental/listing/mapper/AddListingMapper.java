package dev.fascodes.carRental.listing.mapper;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.dto.AddListingResponse;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import dev.fascodes.carRental.user.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AddListingMapper {

    public Listing toEntity(AddListingRequest request, User user, Car car) {
        Listing listing = new Listing();
        listing.setUser(user);
        listing.setCar(car);
        listing.setPrice(request.getPrice());
        listing.setTitle(request.getTitle());
        listing.setLocalization(request.getLocalization());
        listing.setBody(request.getBody());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setLastActive(LocalDateTime.now());
        return listing;
    }

    public AddListingResponse toResponse(Listing listing) {
        AddListingResponse response = new AddListingResponse();
        response.setTitle(listing.getTitle());
        response.setPrice(listing.getPrice());
        response.setStatus(listing.getStatus());
        return response;
    }
}
