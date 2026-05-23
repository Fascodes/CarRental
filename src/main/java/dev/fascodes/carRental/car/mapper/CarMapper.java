package dev.fascodes.carRental.car.mapper;

import dev.fascodes.carRental.car.dto.AddCarRequest;
import dev.fascodes.carRental.car.dto.AddCarResponse;
import dev.fascodes.carRental.car.dto.CarDetailResponse;
import dev.fascodes.carRental.car.dto.CarResponse;
import dev.fascodes.carRental.car.model.Car;
import dev.fascodes.carRental.car.model.GearboxType;
import dev.fascodes.carRental.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {

    public Car toEntity(AddCarRequest request, User owner) {
        Car car = new Car();
        car.setOwner(owner);
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setModelYear(request.getModelYear());
        car.setVin(request.getVin());
        car.setSeatNumber(request.getSeatNumber());
        car.setGearboxType(GearboxType.valueOf(request.getGearboxType()));
        car.setHorsePower(request.getHorsePower());
        car.setAvgLiters(request.getAvgLiters());
        car.setInfo(request.getInfo());
        return car;
    }
    public AddCarResponse toResponse(Car car){
        AddCarResponse response = new AddCarResponse();
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setModelYear(car.getModelYear());
        return response;
    }

    public CarResponse toCarResponse(Car car) {
        CarResponse response = new CarResponse();
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setModelYear(car.getModelYear());
        response.setGearboxType(car.getGearboxType());
        return response;
    }

    public CarDetailResponse toCarDetailResponse(Car car) {
        CarDetailResponse response = new CarDetailResponse();
        response.setBrand(car.getBrand());
        response.setModel(car.getModel());
        response.setModelYear(car.getModelYear());
        response.setGearboxType(car.getGearboxType());
        response.setSeatNumber(car.getSeatNumber());
        response.setHorsePower(car.getHorsePower());
        response.setAvgLiters(car.getAvgLiters());
        response.setInfo(car.getInfo());
        return response;
    }
}
