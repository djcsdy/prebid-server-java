package org.prebid.server.settings.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GdprConfig {

    @JsonProperty("host-vendor-id")
    String hostVendorId;

    Boolean enabled;

    @JsonProperty("default-value")
    String defaultValue;

    Purposes purposes;

    @JsonProperty("special-features")
    SpecialFeatures specialFeatures;

    @JsonProperty("purpose-one-treatment-interpretation")
    Boolean purposeOneTreatmentInterpretation;
}

