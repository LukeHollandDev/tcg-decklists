package dev.lukeholland.tcg.decklists.api.pokemon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Response DTO for paginated card search results.
 * Wraps the list of cards with pagination metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardSearchResponse {

    /**
     * List of cards matching the search criteria
     */
    private List<CardResponse> results;

    /**
     * Total number of cards matching the search (across all pages)
     */
    private long totalResults;

    /**
     * Total number of pages available
     */
    private int totalPages;

    /**
     * Current page number (0-indexed)
     */
    private int currentPage;

    /**
     * Number of results per page
     */
    private int pageSize;

    /**
     * Whether there is a next page
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page
     */
    private boolean hasPrevious;

    /**
     * Default constructor for Jackson deserialization
     */
    public CardSearchResponse() {
    }

    /**
     * Constructor that builds response from Spring Data Page object
     *
     * @param page Spring Data Page containing CardResponse objects
     */
    public CardSearchResponse(Page<CardResponse> page) {
        this.results = page.getContent();
        this.totalResults = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }

    // ===== Getters and Setters =====

    public List<CardResponse> getResults() {
        return results;
    }

    public void setResults(List<CardResponse> results) {
        this.results = results;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
