package com.cibertec.service.impl;

import com.cibertec.client.CountryClient;
import com.cibertec.dto.external.CountryExternal;
import com.cibertec.dto.external.Capital;
import com.cibertec.dto.external.Flags;
import com.cibertec.dto.external.Name;
import com.cibertec.dto.response.CountryResponse;
import com.cibertec.exception.PaisNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private CountryClient client;

    @InjectMocks
    private CountryServiceImpl service;


    private CountryExternal buildCountry() {
        CountryExternal country = new CountryExternal();

        Name name = new Name();
        name.setCommon("Peru");

        Flags flags = new Flags();
        flags.setPng("flag.png");

        country.setNames(name);
        Capital capital = new Capital();
        capital.setName("Lima");
        country.setCapitals(List.of(capital));
        country.setRegion("Americas");
        country.setFlag(flags);

        return country;
    }


    @Test
    void getByCode_ok() {

        CountryExternal country = buildCountry();

        when(client.getByCode("PE"))
                .thenReturn(new CountryExternal[]{country});

        CountryResponse result = service.getByCode("PE");

        assertNotNull(result);
        assertEquals("Peru", result.getName());
        assertEquals("Lima", result.getCapital());
        assertEquals("Americas", result.getRegion());
        assertEquals("flag.png", result.getFlag());
    }

    @Test
    void getByCode_empty() {

        when(client.getByCode("XX"))
                .thenReturn(new CountryExternal[0]);

        assertThrows(PaisNoEncontradoException.class,
                () -> service.getByCode("XX"));
    }

    @Test
    void getByCode_notFound() {

        when(client.getByCode("XX"))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(PaisNoEncontradoException.class,
                () -> service.getByCode("XX"));
    }


    @Test
    void searchByName_ok() {

        CountryExternal country = buildCountry();

        when(client.getByName("Peru"))
                .thenReturn(new CountryExternal[]{country});

        List<CountryResponse> result = service.searchByName("Peru");

        assertEquals(1, result.size());
        assertEquals("Peru", result.get(0).getName());
    }

    @Test
    void searchByName_empty() {

        when(client.getByName("Nada"))
                .thenReturn(new CountryExternal[0]);

        assertThrows(PaisNoEncontradoException.class,
                () -> service.searchByName("Nada"));
    }

    @Test
    void getAll_ok() {

        CountryExternal country = buildCountry();

        when(client.getAllCountries())
                .thenReturn(new CountryExternal[]{country});

        List<CountryResponse> result = service.getAllCountries();

        assertEquals(1, result.size());
    }

    @Test
    void getAll_empty() {

        when(client.getAllCountries())
                .thenReturn(new CountryExternal[0]);

        assertThrows(PaisNoEncontradoException.class,
                () -> service.getAllCountries());
    }

    @Test
    void getByRegion_ok() {

        CountryExternal country = buildCountry();

        when(client.getByRegion("Americas"))
                .thenReturn(new CountryExternal[]{country});

        List<CountryResponse> result = service.getByRegion("Americas");

        assertEquals(1, result.size());
        assertEquals("Americas", result.get(0).getRegion());
    }
}