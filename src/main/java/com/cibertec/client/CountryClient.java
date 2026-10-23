package com.cibertec.client;

import com.cibertec.dto.external.CountryExternal;
import com.cibertec.dto.external.CountryPage;
import com.cibertec.exception.CountryApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class CountryClient {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 20;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    @Autowired
    public CountryClient(
            @Value("${restcountries.v5.base-url:https://api.restcountries.com/countries/v5}") String baseUrl,
            @Value("${restcountries.v5.api-key:${RESTCOUNTRIES_API_KEY:}}") String apiKey) {
        this(createRestTemplate(), baseUrl, apiKey);
    }

    CountryClient(RestTemplate restTemplate, String baseUrl, String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    public CountryExternal[] getByCode(String code) {
        String value = code.trim().toUpperCase(Locale.ROOT);
        String property;
        if (value.matches("[A-Z]{2}")) {
            property = "codes.alpha_2";
        } else if (value.matches("[A-Z]{3}")) {
            property = "codes.alpha_3";
        } else if (value.matches("[0-9]{3}")) {
            property = "codes.ccn3";
        } else {
            return new CountryExternal[0];
        }
        return fetchAll(new String[]{property, value}, Map.of());
    }

    public CountryExternal[] getByName(String name) {
        return fetchAll(new String[]{"name"}, Map.of("q", name));
    }

    public CountryExternal[] getAllCountries() {
        return fetchAll(new String[0], Map.of());
    }

    public CountryExternal[] getByRegion(String region) {
        return fetchAll(new String[]{"region", region}, Map.of());
    }

    private CountryExternal[] fetchAll(String[] path, Map<String, String> filters) {
        if (apiKey.isBlank()) {
            throw new CountryApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura RESTCOUNTRIES_API_KEY en las variables de entorno de country-service.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        List<CountryExternal> countries = new ArrayList<>();
        int offset = 0;

        for (int page = 0; page < MAX_PAGES; page++) {
            UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl);
            Map<String, Object> values = new HashMap<>();
            for (int index = 0; index < path.length; index++) {
                uri.pathSegment("{path" + index + "}");
                values.put("path" + index, path[index]);
            }
            filters.forEach((key, value) -> {
                uri.queryParam(key, "{filter_" + key + "}");
                values.put("filter_" + key, value);
            });
            uri.queryParam("response_fields", "names.common,capitals,region,flag.url_png")
                    .queryParam("limit", PAGE_SIZE)
                    .queryParam("offset", offset);
            URI requestUri = uri.encode().buildAndExpand(values).toUri();

            CountryPage response;
            try {
                response = restTemplate.exchange(requestUri, HttpMethod.GET,
                        new HttpEntity<Void>(headers), CountryPage.class).getBody();
            } catch (HttpClientErrorException.NotFound ex) {
                if (page == 0) return new CountryExternal[0];
                throw upstreamFailure();
            } catch (HttpClientErrorException ex) {
                int status = ex.getStatusCode().value();
                if (status == 401 || status == 403) {
                    throw new CountryApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "REST Countries rechazo la API key. Revisa RESTCOUNTRIES_API_KEY y sus permisos.");
                }
                if (status == 429) {
                    throw new CountryApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "Se alcanzo el limite de consultas de REST Countries. Intenta mas tarde.");
                }
                throw upstreamFailure();
            } catch (RestClientException ex) {
                throw upstreamFailure();
            }

            if (response == null || Boolean.FALSE.equals(response.getSuccess())
                    || response.getData() == null || response.getData().getObjects() == null) {
                throw upstreamFailure();
            }
            CountryPage.PageData data = response.getData();
            if (data.getDemo() != null) {
                throw new CountryApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "La clave demo solo devuelve una muestra. Configura una API key propia de REST Countries.");
            }

            List<CountryExternal> objects = data.getObjects();
            if (objects.stream().anyMatch(country -> country == null || country.getNames() == null
                    || country.getNames().getCommon() == null)) {
                throw upstreamFailure();
            }
            countries.addAll(objects);
            CountryPage.Meta meta = data.getMeta();
            boolean more = meta != null && (Boolean.TRUE.equals(meta.getMore())
                    || (meta.getMore() == null && meta.getTotal() != null
                    && countries.size() < meta.getTotal()));
            if (!more) return countries.toArray(CountryExternal[]::new);
            if (objects.isEmpty()) throw upstreamFailure();
            offset += objects.size();
        }
        throw upstreamFailure();
    }

    private CountryApiException upstreamFailure() {
        return new CountryApiException(HttpStatus.BAD_GATEWAY,
                "No se pudo obtener una respuesta valida de REST Countries. Intenta mas tarde.");
    }
}
