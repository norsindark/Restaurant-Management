package com.restaurant_management.services.interfaces;

import com.restaurant_management.dtos.LocationRestaurantDto;
import com.restaurant_management.entites.LocationRestaurant;
import com.restaurant_management.exceptions.DataExitsException;
import com.restaurant_management.payloads.requests.SettingRequest;
import com.restaurant_management.payloads.responses.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface LocationRestaurantService {
    LocationRestaurant getLocation() throws DataExitsException;
    ApiResponse createLocation(LocationRestaurantDto locationRestaurantDto) throws DataExitsException;
    ApiResponse updateLocation(LocationRestaurantDto locationRestaurantDto) throws DataExitsException;
    ApiResponse deleteLocation(String id) throws DataExitsException;

    ApiResponse settingRestaurant(SettingRequest request) throws DataExitsException;

    ApiResponse settingLogo(MultipartFile file) throws DataExitsException, IOException;
}