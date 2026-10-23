package com.cibertec.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponse {

    //no es necesario poner @NotNull es response

    private String name;

    private String capital;

    private String region;

    private String flag;
}