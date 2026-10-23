package com.cibertec.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryPage {
    private Boolean success;
    private PageData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageData {
        private List<CountryExternal> objects;
        private Meta meta;
        @JsonProperty("_demo")
        private Map<String, Object> demo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private Integer total;
        private Boolean more;
    }
}
