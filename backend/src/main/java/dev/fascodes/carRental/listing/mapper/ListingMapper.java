package dev.fascodes.carRental.listing.mapper;

import dev.fascodes.carRental.listing.dto.ListingResponse;
import dev.fascodes.carRental.listing.model.Listing;
import org.springframework.stereotype.Component;


// TODO: this mapper could be combined with AddListingMapper into one ListingMapper with all combined methods
@Component
public class ListingMapper {

    public ListingResponse toResponse(Listing listing) {
        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setPrice(listing.getPrice());
        response.setBrand(listing.getCar().getBrand());
        response.setModel(listing.getCar().getModel());
        response.setModelYear(listing.getCar().getModelYear());
        response.setGearboxType(listing.getCar().getGearboxType());
        response.setStatus(listing.getStatus());
        return response;
    }
}
