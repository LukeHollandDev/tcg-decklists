package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metadata describing a filter parameter for building dynamic search UIs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterMetadata {

    private String type;
    private String operator;
    private String description;
    private String parameterName;
    private String valuesRef;
    private String matchAllParameter;
    private String minParameter;
    private String maxParameter;
    private Boolean accentInsensitive;

    public FilterMetadata() {
    }

    public static FilterMetadata string(String parameterName, String description, boolean accentInsensitive) {
        FilterMetadata meta = new FilterMetadata();
        meta.type = "string";
        meta.operator = "contains";
        meta.parameterName = parameterName;
        meta.description = description;
        meta.accentInsensitive = accentInsensitive;
        return meta;
    }

    public static FilterMetadata enumFilter(String parameterName, String description, String valuesRef) {
        FilterMetadata meta = new FilterMetadata();
        meta.type = "enum";
        meta.operator = "equals";
        meta.parameterName = parameterName;
        meta.description = description;
        meta.valuesRef = valuesRef;
        return meta;
    }

    public static FilterMetadata multiselect(String parameterName, String description, String valuesRef, String matchAllParameter) {
        FilterMetadata meta = new FilterMetadata();
        meta.type = "multiselect";
        meta.operator = "anyOf";
        meta.parameterName = parameterName;
        meta.description = description;
        meta.valuesRef = valuesRef;
        meta.matchAllParameter = matchAllParameter;
        return meta;
    }

    public static FilterMetadata range(String description, String minParameter, String maxParameter) {
        FilterMetadata meta = new FilterMetadata();
        meta.type = "range";
        meta.operator = "range";
        meta.description = description;
        meta.minParameter = minParameter;
        meta.maxParameter = maxParameter;
        return meta;
    }

    public static FilterMetadata booleanFilter(String parameterName, String description) {
        FilterMetadata meta = new FilterMetadata();
        meta.type = "boolean";
        meta.operator = "equals";
        meta.parameterName = parameterName;
        meta.description = description;
        return meta;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getValuesRef() {
        return valuesRef;
    }

    public void setValuesRef(String valuesRef) {
        this.valuesRef = valuesRef;
    }

    public String getMatchAllParameter() {
        return matchAllParameter;
    }

    public void setMatchAllParameter(String matchAllParameter) {
        this.matchAllParameter = matchAllParameter;
    }

    public String getMinParameter() {
        return minParameter;
    }

    public void setMinParameter(String minParameter) {
        this.minParameter = minParameter;
    }

    public String getMaxParameter() {
        return maxParameter;
    }

    public void setMaxParameter(String maxParameter) {
        this.maxParameter = maxParameter;
    }

    public Boolean getAccentInsensitive() {
        return accentInsensitive;
    }

    public void setAccentInsensitive(Boolean accentInsensitive) {
        this.accentInsensitive = accentInsensitive;
    }
}
