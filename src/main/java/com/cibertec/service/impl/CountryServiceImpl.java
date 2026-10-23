package com.cibertec.service.impl;

import com.cibertec.client.CountryClient;
import com.cibertec.dto.external.CountryExternal;
import com.cibertec.dto.response.CountryResponse;
import com.cibertec.exception.PaisNoEncontradoException;
import com.cibertec.mapper.CountryMapper;
import com.cibertec.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryClient client;

    @Override
    public CountryResponse getByCode(String code) {

        try {
            CountryExternal[] response = client.getByCode(code);

            if (response == null || response.length == 0) {
                throw new PaisNoEncontradoException("No se encontró el país con código: " + code);
            }

            return CountryMapper.toResponse(response[0]);

        } catch (HttpClientErrorException.NotFound e) {
            throw new PaisNoEncontradoException("No se encontró el país con código: " + code);
        }
    }

    @Override
    public List<CountryResponse> searchByName(String name) {

        CountryExternal[] response = client.getByName(name);

        validarRespuesta(response, "No se encontraron países con el nombre: " + name);

        return Arrays.stream(response)
                .map(CountryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CountryResponse> getAllCountries() {
        CountryExternal[] response = client.getAllCountries();

        validarRespuesta(response, "No se encontraron países");

        return Arrays.stream(response)
                .map(CountryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CountryResponse> getByRegion(String region) {
        CountryExternal[] response = client.getByRegion(region);

        validarRespuesta(response, "No se encontraron países en la región: " + region);

        return Arrays.stream(response)
                .map(CountryMapper::toResponse)
                .toList();
    }

    private void validarRespuesta(CountryExternal[] response, String mensaje) {
        if (response == null || response.length == 0) {
            throw new PaisNoEncontradoException(mensaje);
        }
    }
}