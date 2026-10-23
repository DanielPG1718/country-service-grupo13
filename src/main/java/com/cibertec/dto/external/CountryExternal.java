package com.cibertec.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryExternal {
    private Name names;
    private List<Capital> capitals;
    private String region;
    private Flags flag;
}
