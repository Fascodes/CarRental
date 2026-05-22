package dev.fascodes.carRental.listing.controller;

import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.dto.AddListingResponse;
import dev.fascodes.carRental.listing.dto.ListingResponse;
import dev.fascodes.carRental.listing.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listing")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @Valid
    @PostMapping("/add")
    public ResponseEntity<AddListingResponse> addListing(@RequestBody AddListingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(listingService.addListing(request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeListing(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        listingService.removeListing(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ListingResponse>> getListings(
            Pageable pageable,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer priceMax) {
        return ResponseEntity.ok(listingService.getListings(pageable, brand, priceMax));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(listingService.getListing(id, email));
    }
}
