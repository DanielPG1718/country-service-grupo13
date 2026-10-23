package com.cibertec.mapper;

import com.cibertec.dto.external.Capital;
import com.cibertec.dto.external.CountryExternal;
import com.cibertec.dto.response.CountryResponse;
import java.util.Objects;

public class CountryMapper {

    public static CountryResponse toResponse(CountryExternal dto) {
        if (dto == null) return null;

        String capital = dto.getCapitals() == null ? null : dto.getCapitals().stream()
                .filter(Objects::nonNull)
                .map(Capital::getName)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);

        return new CountryResponse(
                dto.getNames() != null ? dto.getNames().getCommon() : null,
                capital,
                dto.getRegion(),
                dto.getFlag() != null ? dto.getFlag().getPng() : null
        );
    }
}
