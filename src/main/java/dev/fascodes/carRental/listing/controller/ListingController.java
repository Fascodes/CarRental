package dev.fascodes.carRental.listing.controller;

import dev.fascodes.carRental.common.security.AuthenticatedUser;
import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.dto.AddListingResponse;
import dev.fascodes.carRental.listing.dto.ListingResponse;
import dev.fascodes.carRental.listing.dto.UpdateListingRequest;
import dev.fascodes.carRental.listing.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listing")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }


    // TODO: When adding scheduling into the system - listings ACTIVE for 30 days maximum unless extended by owner
    @PostMapping("/add")
    public ResponseEntity<AddListingResponse> addListing(@Valid @RequestBody AddListingRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(listingService.addListing(request, user.email()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeListing(@PathVariable Long id,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        listingService.removeListing(id, user.email());
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
    public ResponseEntity<ListingResponse> getListing(@PathVariable Long id,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(listingService.getListing(id, user.email()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ListingResponse> updateListing(@PathVariable Long id,
                                                         @RequestBody UpdateListingRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(listingService.updateListing(id, request, user.email()));
    }
}
