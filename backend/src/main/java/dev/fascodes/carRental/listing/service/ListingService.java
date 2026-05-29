package dev.fascodes.carRental.listing.service;

import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.dto.AddListingRequest;
import dev.fascodes.carRental.listing.dto.AddListingResponse;
import dev.fascodes.carRental.listing.dto.ListingDetailResponse;
import dev.fascodes.carRental.listing.dto.ListingResponse;
import dev.fascodes.carRental.listing.dto.UpdateListingRequest;
import dev.fascodes.carRental.listing.mapper.AddListingMapper;
import dev.fascodes.carRental.listing.mapper.ListingMapper;
import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.listing.specification.ListingSpecification;
import dev.fascodes.carRental.reservation.model.ReservationStatus;
import dev.fascodes.carRental.reservation.repository.ReservationRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ListingService {
    private final AddListingMapper addListingMapper;
    private final ListingMapper listingMapper;
    private final ListingRepository listingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public ListingService(AddListingMapper addListingMapper, ListingMapper listingMapper,
                          ListingRepository listingRepository, CarRepository carRepository,
                          UserRepository userRepository, ReservationRepository reservationRepository) {
        this.addListingMapper = addListingMapper;
        this.listingMapper = listingMapper;
        this.listingRepository = listingRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
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
        boolean hasActive = reservationRepository.existsByListingIdAndStatusIn(
                listingId, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
        if (hasActive) throw new ResponseStatusException(HttpStatus.CONFLICT, "Listing has active reservations");
        listingRepository.delete(listing);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getListings(Pageable pageable, String brand, Integer priceMax) {
        Specification<Listing> spec = Specification.where(ListingSpecification.isActive());
        if (priceMax != null) spec = spec.and(ListingSpecification.priceAtMost(priceMax));
        if (brand != null && !brand.isBlank()) spec = spec.and(ListingSpecification.hasBrand(brand));
        return listingRepository.findAll(spec, pageable).map(listingMapper::toResponse);
    }

    @Cacheable(value = "listing", key = "#listingId", unless = "#result.status.name() != 'ACTIVE'")
    @Transactional(readOnly = true)
    public ListingDetailResponse getListing(Long listingId, String email) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (listing.getStatus() == ListingStatus.INACTIVE && !listing.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return listingMapper.toDetailResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(String email, ListingStatus status) {
        List<Listing> listings = status != null
                ? listingRepository.findByUser_EmailAndStatus(email, status)
                : listingRepository.findByUser_Email(email);
        return listings.stream().map(listingMapper::toResponse).toList();
    }

    @CacheEvict(value = "listing", key = "#listingId")
    @Transactional
    public ListingResponse updateListing(Long listingId, UpdateListingRequest request, String email) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!listing.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if(request.getCarId() != null){
            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
            if (!car.getOwner().getEmail().equals(email)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
            }
            listing.setCar(car);
        }
        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getLocalization() != null) listing.setLocalization(request.getLocalization());
        if (request.getPrice() != null) listing.setPrice(request.getPrice());
        if (request.getBody() != null) listing.setBody(request.getBody());
        if (request.getStatus() != null) listing.setStatus(request.getStatus());
        return listingMapper.toResponse(listing);
    }
}
