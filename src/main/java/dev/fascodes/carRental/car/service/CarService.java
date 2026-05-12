package dev.fascodes.carRental.car.service;

import dev.fascodes.carRental.car.dto.AddCarRequest;
import dev.fascodes.carRental.car.dto.AddCarResponse;
import dev.fascodes.carRental.car.mapper.AddCarMapper;
import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.repository.CarRepository;
import dev.fascodes.carRental.common.utility.JwtUtil;
import dev.fascodes.carRental.user.model.User;
import dev.fascodes.carRental.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CarService {
    private final AddCarMapper addCarMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    public CarService(AddCarMapper addCarMapper, JwtUtil jwtUtil, UserRepository userRepository, CarRepository carRepository) {
        this.addCarMapper = addCarMapper;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
    }

    public AddCarResponse addCarToUser(AddCarRequest req, String email){
        User user = userRepository.findByEmail(email).orElseThrow();
        Car car = addCarMapper.toEntity(req, user);
        carRepository.save(car);
        return addCarMapper.toResponse(car);
    }
}
