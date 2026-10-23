package com.cibertec.client;

import com.cibertec.dto.external.CountryExternal;
import com.cibertec.exception.CountryApiException;
import com.cibertec.exception.PaisNoEncontradoException;
import com.cibertec.mapper.CountryMapper;
import com.cibertec.service.impl.CountryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CountryClientTest {
    private MockRestServiceServer server;
    private RestTemplate restTemplate;
    private CountryClient client;
    private static final String BASE = "https://api.restcountries.com/countries/v5";
    private static final String COUNTRY = """
            {"names":{"common":"Peru","official":"Republic of Peru"},
             "capitals":[{"name":"Lima","coordinates":{"lat":-12.05}}],
             "region":"Americas",
             "flag":{"url_png":"https://flags.restcountries.com/v5/w640/pe.png"},
             "currencies":[{"code":"PEN"}]}
            """;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new CountryClient(restTemplate, BASE, "test-key");
    }

    private String page(String objects, boolean more) {
        return "{\"data\":{\"objects\":[" + objects + "],\"meta\":{\"more\":" + more + "}}}";
    }

    @ParameterizedTest
    @CsvSource({"pe,codes.alpha_2/PE", "per,codes.alpha_3/PER", "604,codes.ccn3/604"})
    void readsV5EnvelopeAndPreservesFrontendResponse(String code, String path) {
        server.expect(request -> assertEquals("/countries/v5/" + path, request.getURI().getPath()))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andRespond(withSuccess(page(COUNTRY, false), MediaType.APPLICATION_JSON));

        var response = CountryMapper.toResponse(client.getByCode(code)[0]);
        assertEquals("Peru", response.getName());
        assertEquals("Lima", response.getCapital());
        assertEquals("Americas", response.getRegion());
        assertEquals("https://flags.restcountries.com/v5/w640/pe.png", response.getFlag());
        server.verify();
    }

    @Test
    void collectsAllPagesInsteadOfOnlyFirstPage() {
        server.expect(queryParam("offset", "0"))
                .andExpect(queryParam("limit", "100"))
                .andRespond(withSuccess(page(COUNTRY, true), MediaType.APPLICATION_JSON));
        server.expect(queryParam("offset", "1"))
                .andRespond(withSuccess(page(COUNTRY.replace("Peru", "Canada"), false), MediaType.APPLICATION_JSON));
        CountryExternal[] countries = client.getAllCountries();
        assertEquals(2, countries.length);
        assertEquals("Canada", countries[1].getNames().getCommon());
        server.verify();
    }

    @Test
    void searchesNamesAndEncodesUserInput() {
        server.expect(request -> {
            assertEquals("/countries/v5/name", request.getURI().getPath());
            assertTrue(request.getURI().getRawQuery().contains("q=C%C3%B4te%20%2B%20%26%20%2F"));
        }).andRespond(withSuccess(page(COUNTRY, false), MediaType.APPLICATION_JSON));
        assertEquals(1, client.getByName("Côte + & /").length);
        server.verify();
    }

    @Test
    void regionUsesExactPropertyRouteAndPagination() {
        server.expect(request -> assertEquals("/countries/v5/region/Americas", request.getURI().getPath()))
                .andRespond(withSuccess(page(COUNTRY, true), MediaType.APPLICATION_JSON));
        server.expect(queryParam("offset", "1"))
                .andRespond(withSuccess(page(COUNTRY, false), MediaType.APPLICATION_JSON));
        assertEquals(2, client.getByRegion("Americas").length);
        server.verify();
    }

    @Test
    void noMatchesKeepDomainNotFoundBehavior() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(PaisNoEncontradoException.class,
                () -> new CountryServiceImpl(client).searchByName("inexistente"));
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"401,503", "403,503", "429,503", "500,502"})
    void translatesProviderErrorsWithoutExposingTheirBody(int upstream, int expected) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(upstream))
                .body("private-provider-detail").contentType(MediaType.TEXT_PLAIN));
        CountryApiException error = assertThrows(CountryApiException.class, client::getAllCountries);
        assertEquals(expected, error.getStatus().value());
        assertFalse(error.getMessage().contains("private-provider-detail"));
        server.verify();
    }

    @Test
    void missingKeyMakesNoExternalRequest() {
        client = new CountryClient(restTemplate, BASE, "");
        CountryApiException error = assertThrows(CountryApiException.class, client::getAllCountries);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatus());
        assertTrue(error.getMessage().contains("RESTCOUNTRIES_API_KEY"));
        server.verify();
    }

    @Test
    void rejectsDemoInsteadOfReturningCanadaForEveryQuery() {
        server.expect(anything()).andRespond(withSuccess("""
                {"data":{"_demo":{"message":"Sample only"},"objects":[],"meta":{"total":1}}}
                """, MediaType.APPLICATION_JSON));
        CountryApiException error = assertThrows(CountryApiException.class, () -> client.getByCode("PE"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatus());
        server.verify();
    }

    @Test
    void malformedResponseIsNotReportedAsCountryNotFound() {
        server.expect(anything()).andRespond(withSuccess("{\"data\":null}", MediaType.APPLICATION_JSON));
        CountryApiException error = assertThrows(CountryApiException.class, client::getAllCountries);
        assertEquals(HttpStatus.BAD_GATEWAY, error.getStatus());
        server.verify();
    }

    @Test
    void emptyPageWithMoreDoesNotLoopForever() {
        server.expect(anything()).andRespond(withSuccess(page("", true), MediaType.APPLICATION_JSON));
        assertThrows(CountryApiException.class, client::getAllCountries);
        server.verify();
    }

    @Test
    void supportsCountriesWithoutCapitalOrFlag() {
        server.expect(anything()).andRespond(withSuccess(page("""
                {"names":{"common":"Country"},"capitals":[],"region":"Americas"}
                """, false), MediaType.APPLICATION_JSON));
        var response = CountryMapper.toResponse(client.getAllCountries()[0]);
        assertNull(response.getCapital());
        assertNull(response.getFlag());
        server.verify();
    }
}
