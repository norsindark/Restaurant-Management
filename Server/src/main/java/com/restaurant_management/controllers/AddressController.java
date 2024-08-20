package com.restaurant_management.controllers;

import com.restaurant_management.dtos.AddressDto;
import com.restaurant_management.exceptions.DataExitsException;
import com.restaurant_management.payloads.responses.AddressByUserIdResponse;
import com.restaurant_management.payloads.responses.AddressResponse;
import com.restaurant_management.payloads.responses.ApiResponse;
import com.restaurant_management.services.interfaces.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/client/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> addAddress(@Valid @RequestBody AddressDto addressDto)
            throws DataExitsException {
        return ResponseEntity.ok(this.addressService.addAddress(addressDto));
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> updateAddress(@Valid @RequestBody AddressDto addressDto)
            throws DataExitsException {
        return ResponseEntity.ok(this.addressService.updateAddress(addressDto));
    }

    @GetMapping("/get-address/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable String addressId) throws DataExitsException {
        return ResponseEntity.ok(this.addressService.getAddress(addressId));
    }

    @GetMapping("/get-all-address")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<AddressByUserIdResponse>> getAllAddressByUserId(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) throws DataExitsException {
        Page<AddressByUserIdResponse> addressPage = addressService.getAllAddressByUserId(userId, pageNo, pageSize);
        return ResponseEntity.ok(addressPage);
    }


    @DeleteMapping("/delete/{addressId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> deleteAddress(@PathVariable String addressId) {
        return ResponseEntity.ok(this.addressService.deleteAddress(addressId));
    }

}