package dev.fascodes.carRental.listing.service;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.dto.AddListingResponse;
import dev.fascodes.carRental.listing.dto.ListingResponse;
import dev.fascodes.carRental.listing.mapper.AddListingMapper;
import dev.fascodes.carRental.listing.mapper.ListingMapper;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.listing.specification.ListingSpecification;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListingService {
    private final AddListingMapper addListingMapper;
    private final ListingMapper listingMapper;
    private final ListingRepository listingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public ListingService(AddListingMapper addListingMapper, ListingMapper listingMapper,
                          ListingRepository listingRepository, CarRepository carRepository,
                          UserRepository userRepository) {
        this.addListingMapper = addListingMapper;
        this.listingMapper = listingMapper;
        this.listingRepository = listingRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AddListingResponse addListing(AddListingRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        if (!car.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Listing listing = addListingMapper.toEntity(request, user, car);
        listingRepository.save(listing);
        return addListingMapper.toResponse(listing);
    }

    @Transactional
    public void removeListing(Long listingId, String email) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!listing.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        listingRepository.delete(listing);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getListings(Pageable pageable, String brand, Integer priceMax) {
        Specification<Listing> spec = Specification.where(ListingSpecification.isActive());
        if (priceMax != null) spec = spec.and(ListingSpecification.priceAtMost(priceMax));
        if (brand != null && !brand.isBlank()) spec = spec.and(ListingSpecification.hasBrand(brand));
        return listingRepository.findAll(spec, pageable).map(listingMapper::toResponse);
    }

    @Cacheable("listing")
    @Transactional(readOnly = true)
    public ListingResponse getListing(Long listingId, String email) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (listing.getStatus() == ListingStatus.INACTIVE && !listing.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return listingMapper.toResponse(listing);
    }
}
