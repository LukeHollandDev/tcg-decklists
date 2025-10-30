package dev.lukeholland.tcg.decklists.api.pokemon.testutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Common test utility methods for Pokemon card integration tests.
 * <p>
 * Provides helper methods for:
 * - JSON response parsing and assertions
 * - Pagination metadata validation
 * - Multiset counting utilities
 * - Accent normalization
 * - Common test data generation
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Parse JSON response
 * JsonNode results = TestUtils.parseJsonResponse(response);
 *
 * // Validate pagination metadata
 * TestUtils.assertPaginationMetadata(response, 0, 20, 100, 5, false, true);
 *
 * // Count multiset occurrences
 * Map<String, Integer> counts = TestUtils.countOccurrences(Arrays.asList("Fire", "Fire", "Water"));
 * // Returns {Fire=2, Water=1}
 * }</pre>
 */
public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a JSON response string into a JsonNode.
     *
     * @param jsonResponse the JSON response string
     * @return the parsed JsonNode
     * @throws RuntimeException if parsing fails
     */
    public static JsonNode parseJsonResponse(String jsonResponse) {
        try {
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    /**
     * Extracts the "results" array from a paginated JSON response.
     *
     * @param jsonResponse the JSON response string
     * @return the results array as JsonNode
     * @throws RuntimeException if parsing fails or results field is missing
     */
    public static JsonNode extractResults(String jsonResponse) {
        JsonNode root = parseJsonResponse(jsonResponse);
        JsonNode results = root.get("results");
        if (results == null) {
            throw new RuntimeException("JSON response does not contain 'results' field");
        }
        return results;
    }

    /**
     * Validates pagination metadata in a JSON response.
     *
     * @param jsonResponse       the JSON response string
     * @param expectedPage       expected current page number
     * @param expectedPageSize   expected page size
     * @param expectedTotal      expected total results
     * @param expectedTotalPages expected total pages
     * @param expectedHasPrev    expected hasPrevious value
     * @param expectedHasNext    expected hasNext value
     * @throws AssertionError if any metadata field doesn't match expected value
     */
    public static void assertPaginationMetadata(
            String jsonResponse,
            int expectedPage,
            int expectedPageSize,
            int expectedTotal,
            int expectedTotalPages,
            boolean expectedHasPrev,
            boolean expectedHasNext
    ) {
        JsonNode root = parseJsonResponse(jsonResponse);

        int actualPage = root.get("currentPage").asInt();
        int actualPageSize = root.get("pageSize").asInt();
        int actualTotal = root.get("totalResults").asInt();
        int actualTotalPages = root.get("totalPages").asInt();
        boolean actualHasPrev = root.get("hasPrevious").asBoolean();
        boolean actualHasNext = root.get("hasNext").asBoolean();

        if (actualPage != expectedPage) {
            throw new AssertionError(String.format(
                    "Expected currentPage=%d but was %d", expectedPage, actualPage));
        }
        if (actualPageSize != expectedPageSize) {
            throw new AssertionError(String.format(
                    "Expected pageSize=%d but was %d", expectedPageSize, actualPageSize));
        }
        if (actualTotal != expectedTotal) {
            throw new AssertionError(String.format(
                    "Expected totalResults=%d but was %d", expectedTotal, actualTotal));
        }
        if (actualTotalPages != expectedTotalPages) {
            throw new AssertionError(String.format(
                    "Expected totalPages=%d but was %d", expectedTotalPages, actualTotalPages));
        }
        if (actualHasPrev != expectedHasPrev) {
            throw new AssertionError(String.format(
                    "Expected hasPrevious=%b but was %b", expectedHasPrev, actualHasPrev));
        }
        if (actualHasNext != expectedHasNext) {
            throw new AssertionError(String.format(
                    "Expected hasNext=%b but was %b", expectedHasNext, actualHasNext));
        }
    }

    /**
     * Validates that a JSON response contains the expected number of results.
     *
     * @param jsonResponse  the JSON response string
     * @param expectedCount the expected number of results
     * @throws AssertionError if the count doesn't match
     */
    public static void assertResultCount(String jsonResponse, int expectedCount) {
        JsonNode results = extractResults(jsonResponse);
        int actualCount = results.size();
        if (actualCount != expectedCount) {
            throw new AssertionError(String.format(
                    "Expected %d results but found %d", expectedCount, actualCount));
        }
    }

    /**
     * Counts occurrences of each unique value in a collection (multiset counting).
     * This is useful for verifying attack costs and retreat costs.
     *
     * @param collection the collection to count
     * @param <T>        the type of elements
     * @return a map of values to their occurrence counts
     */
    public static <T> Map<T, Integer> countOccurrences(Collection<T> collection) {
        Map<T, Integer> counts = new HashMap<>();
        for (T item : collection) {
            counts.put(item, counts.getOrDefault(item, 0) + 1);
        }
        return counts;
    }

    /**
     * Normalizes a string by removing accents.
     * This should match the normalization used in CustomMatchers and the backend.
     *
     * @param input the input string
     * @return the normalized string with accents removed
     */
    public static String normalizeAccents(String input) {
        if (input == null) {
            return null;
        }

        String from = "àáâãäåāăąèéêëēĕėęěìíîïìĩīĭḩùúûüũūŭůäöüßÄÖÜẞéÉ";
        String to = "aaaaaaaaaeeeeeeeeeiiiiiiiihuuuuuuuuaousbAOUsBeeE";

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            int index = from.indexOf(c);
            if (index >= 0) {
                result.append(to.charAt(index));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Checks if two strings are equal with accent-insensitive comparison.
     *
     * @param s1 first string
     * @param s2 second string
     * @return true if strings are equal after accent normalization
     */
    public static boolean equalsIgnoringAccents(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return normalizeAccents(s1).equalsIgnoreCase(normalizeAccents(s2));
    }

    /**
     * Extracts card IDs from a JSON results array.
     *
     * @param results the JSON results array
     * @return list of card IDs
     */
    public static List<String> extractCardIds(JsonNode results) {
        List<String> ids = new ArrayList<>();
        for (JsonNode card : results) {
            ids.add(card.get("id").asText());
        }
        return ids;
    }

    /**
     * Extracts card names from a JSON results array.
     *
     * @param results the JSON results array
     * @return list of card names
     */
    public static List<String> extractCardNames(JsonNode results) {
        List<String> names = new ArrayList<>();
        for (JsonNode card : results) {
            names.add(card.get("name").asText());
        }
        return names;
    }

    /**
     * Checks if a JsonNode array contains a card with the specified ID.
     *
     * @param results the JSON results array
     * @param cardId  the card ID to search for
     * @return true if the card ID is found
     */
    public static boolean containsCardId(JsonNode results, String cardId) {
        return extractCardIds(results).contains(cardId);
    }

    /**
     * Checks if a JsonNode array contains a card with the specified name.
     *
     * @param results  the JSON results array
     * @param cardName the card name to search for
     * @return true if the card name is found
     */
    public static boolean containsCardName(JsonNode results, String cardName) {
        return extractCardNames(results).contains(cardName);
    }

    /**
     * Validates that all cards in the results have the expected supertype.
     *
     * @param results           the JSON results array
     * @param expectedSupertype the expected supertype
     * @throws AssertionError if any card doesn't match
     */
    public static void assertAllHaveSupertype(JsonNode results, String expectedSupertype) {
        for (JsonNode card : results) {
            String actualSupertype = card.get("supertype").asText();
            if (!actualSupertype.equals(expectedSupertype)) {
                throw new AssertionError(String.format(
                        "Card %s has supertype '%s' but expected '%s'",
                        card.get("id").asText(),
                        actualSupertype,
                        expectedSupertype
                ));
            }
        }
    }

    /**
     * Validates that all cards in the results have HP within the specified range.
     *
     * @param results the JSON results array
     * @param minHp   minimum HP (inclusive), or null for no minimum
     * @param maxHp   maximum HP (inclusive), or null for no maximum
     * @throws AssertionError if any card is outside the range
     */
    public static void assertAllHpInRange(JsonNode results, Integer minHp, Integer maxHp) {
        for (JsonNode card : results) {
            JsonNode hpNode = card.get("hpNumeric");
            if (hpNode == null || hpNode.isNull()) {
                continue; // Skip cards without HP (Trainers, Energy)
            }
            int hp = hpNode.asInt();
            if (minHp != null && hp < minHp) {
                throw new AssertionError(String.format(
                        "Card %s has HP %d which is below minimum %d",
                        card.get("id").asText(), hp, minHp
                ));
            }
            if (maxHp != null && hp > maxHp) {
                throw new AssertionError(String.format(
                        "Card %s has HP %d which is above maximum %d",
                        card.get("id").asText(), hp, maxHp
                ));
            }
        }
    }
}
