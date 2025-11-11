package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metadata describing a filter parameter for building dynamic search UIs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilterMetadata(
        String type,
        String operator,
        String description,
        String parameterName,
        String valuesRef,
        String matchAllParameter,
        String minParameter,
        String maxParameter,
        Boolean accentInsensitive
) {

    public static FilterMetadata string(String parameterName, String description, boolean accentInsensitive) {
        return new FilterMetadata(
                "string",
                "contains",
                description,
                parameterName,
                null,
                null,
                null,
                null,
                accentInsensitive
        );
    }

    public static FilterMetadata enumFilter(String parameterName, String description, String valuesRef) {
        return new FilterMetadata(
                "enum",
                "equals",
                description,
                parameterName,
                valuesRef,
                null,
                null,
                null,
                null
        );
    }

    public static FilterMetadata multiselect(String parameterName, String description, String valuesRef, String matchAllParameter) {
        return new FilterMetadata(
                "multiselect",
                "anyOf",
                description,
                parameterName,
                valuesRef,
                matchAllParameter,
                null,
                null,
                null
        );
    }

    public static FilterMetadata range(String description, String minParameter, String maxParameter) {
        return new FilterMetadata(
                "range",
                "range",
                description,
                null,
                null,
                null,
                minParameter,
                maxParameter,
                null
        );
    }

    public static FilterMetadata booleanFilter(String parameterName, String description) {
        return new FilterMetadata(
                "boolean",
                "equals",
                description,
                parameterName,
                null,
                null,
                null,
                null,
                null
        );
    }
}
