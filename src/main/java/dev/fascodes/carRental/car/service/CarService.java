package dev.fascodes.carRental.car.service;

import dev.fascodes.carRental.car.dto.AddCarRequest;
import dev.fascodes.carRental.car.dto.AddCarResponse;
import dev.fascodes.carRental.car.dto.CarDetailResponse;
import dev.fascodes.carRental.car.dto.CarResponse;
import dev.fascodes.carRental.car.dto.PatchCarRequest;
import dev.fascodes.carRental.car.mapper.CarMapper;
import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.listing.repository.ListingRepository;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CarService {
    private final CarMapper carMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ListingRepository listingRepository;

    public CarService(CarMapper carMapper, UserRepository userRepository, CarRepository carRepository, ListingRepository listingRepository) {
        this.carMapper = carMapper;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public AddCarResponse addCarToUser(AddCarRequest req, String email){
        User user = userRepository.findByEmail(email).orElseThrow();
        Car car = carMapper.toEntity(req, user);
        carRepository.save(car);
        return carMapper.toResponse(car);
    }

    @Transactional
    public void removeCar(Long carId, String email) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!car.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (listingRepository.existsByCarId(carId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car has listings");

        carRepository.delete(car);
    }

    @Transactional(readOnly = true)
    public CarDetailResponse getCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return carMapper.toCarDetailResponse(car);
    }

    @Transactional(readOnly = true)
    public List<CarDetailResponse> getMyCars(String email) {
        return carRepository.findByOwner_Email(email).stream()
                .map(carMapper::toCarDetailResponse)
                .toList();
    }

    @Transactional
    public CarResponse patchCar(Long carId, PatchCarRequest request, String email) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!car.getOwner().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        applyPatch(car, request);
        return carMapper.toCarResponse(car);
    }

    @Transactional
    public CarResponse patchCarAdmin(Long carId, PatchCarRequest request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        applyPatch(car, request);
        return carMapper.toCarResponse(car);
    }

    private void applyPatch(Car car, PatchCarRequest request) {
        if (request.getBrand() != null) car.setBrand(request.getBrand());
        if (request.getModel() != null) car.setModel(request.getModel());
        if (request.getModelYear() != null) car.setModelYear(request.getModelYear());
        if (request.getVin() != null) car.setVin(request.getVin());
        if (request.getSeatNumber() != null) car.setSeatNumber(request.getSeatNumber());
        if (request.getGearboxType() != null) car.setGearboxType(request.getGearboxType());
        if (request.getHorsePower() != null) car.setHorsePower(request.getHorsePower());
        if (request.getAvgLiters() != null) car.setAvgLiters(request.getAvgLiters());
        if (request.getInfo() != null) car.setInfo(request.getInfo());
    }
}
