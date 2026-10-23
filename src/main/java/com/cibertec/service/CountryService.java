package com.cibertec.service;

import com.cibertec.dto.response.CountryResponse;

import java.util.List;

public interface CountryService {

    CountryResponse getByCode(String code);

    List<CountryResponse> searchByName(String name);

    List<CountryResponse> getAllCountries();

    List<CountryResponse> getByRegion(String region);
}