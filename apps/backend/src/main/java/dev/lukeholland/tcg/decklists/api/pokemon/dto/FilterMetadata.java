package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Metadata describing a single filter parameter.
 * Used to dynamically build filter UI components on the frontend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterMetadata {

    /**
     * The filter type (e.g., "string", "enum", "multiselect", "range", "boolean")
     */
    private String type;

    /**
     * The query operator (e.g., "contains", "equals", "anyOf", "range")
     */
    private String operator;

    /**
     * Human-readable description of the filter
     */
    private String description;

    /**
     * The query parameter name for this filter
     */
    private String parameterName;

    /**
     * Reference to static data values (e.g., "static.types")
     * Only applicable for enum/multiselect types
     */
    private String valuesRef;

    /**
     * The parameter name for "match all" logic (for multiselect filters)
     * Only applicable for multiselect types
     */
    private String matchAllParameter;

    /**
     * The minimum value parameter name (for range filters)
     * Only applicable for range types
     */
    private String minParameter;

    /**
     * The maximum value parameter name (for range filters)
     * Only applicable for range types
     */
    private String maxParameter;

    /**
     * Whether this filter supports accent-insensitive matching
     */
    private Boolean accentInsensitive;

    // ===== Constructors =====

    public FilterMetadata() {
    }

    // ===== Builder Methods =====

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

    // ===== Getters and Setters =====

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
