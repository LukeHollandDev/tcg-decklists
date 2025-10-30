package dev.lukeholland.tcg.decklists.api.pokemon.testutils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.*;

/**
 * Custom Hamcrest matchers for complex validations in Pokemon card tests.
 * <p>
 * Provides specialized matchers for:
 * - Multiset matching (for attack costs with duplicates)
 * - Accent-insensitive string comparison
 * - Collection subset matching with quantities
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Multiset matching - verify at least 2 Fire energies in attack cost
 * assertThat(attackCostTypes, hasAtLeast(2, "Fire"));
 *
 * // Complex multiset subset matching
 * Map<String, Integer> expected = Map.of("Fire", 2, "Water", 1);
 * assertThat(attackCostTypes, hasAtLeastMultiset(expected));
 *
 * // Accent-insensitive string matching
 * assertThat("Flabébé", normalizedEquals("flabebe"));
 * }</pre>
 */
public class CustomMatchers {

    /**
     * Matches if a collection contains at least the specified quantity of a value.
     * This is useful for multiset matching in attack costs.
     *
     * @param expectedCount the minimum number of occurrences expected
     * @param value         the value to count
     * @param <T>           the type of elements in the collection
     * @return a Hamcrest matcher
     */
    public static <T> Matcher<Collection<T>> hasAtLeast(int expectedCount, T value) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            protected boolean matchesSafely(Collection<T> collection) {
                int actualCount = 0;
                for (T item : collection) {
                    if (Objects.equals(item, value)) {
                        actualCount++;
                    }
                }
                return actualCount >= expectedCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("collection with at least ")
                        .appendValue(expectedCount)
                        .appendText(" occurrence(s) of ")
                        .appendValue(value);
            }

            @Override
            protected void describeMismatchSafely(Collection<T> collection, Description mismatchDescription) {
                int actualCount = 0;
                for (T item : collection) {
                    if (Objects.equals(item, value)) {
                        actualCount++;
                    }
                }
                mismatchDescription.appendText("collection had ")
                        .appendValue(actualCount)
                        .appendText(" occurrence(s) of ")
                        .appendValue(value)
                        .appendText(" in ")
                        .appendValue(collection);
            }
        };
    }

    /**
     * Matches if a collection contains at least the specified quantities of multiple values.
     * This performs multiset subset matching: the actual collection must contain at least
     * the quantities specified in the expected map.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>[Fire, Fire, Water] matches {Fire: 2, Water: 1}</li>
     *   <li>[Fire, Fire, Fire, Water] matches {Fire: 2, Water: 1}</li>
     *   <li>[Fire, Water] does NOT match {Fire: 2, Water: 1}</li>
     * </ul>
     *
     * @param expectedCounts map of values to minimum expected counts
     * @param <T>            the type of elements in the collection
     * @return a Hamcrest matcher
     */
    public static <T> Matcher<Collection<T>> hasAtLeastMultiset(Map<T, Integer> expectedCounts) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            protected boolean matchesSafely(Collection<T> collection) {
                Map<T, Integer> actualCounts = countOccurrences(collection);

                for (Map.Entry<T, Integer> entry : expectedCounts.entrySet()) {
                    T value = entry.getKey();
                    int expectedCount = entry.getValue();
                    int actualCount = actualCounts.getOrDefault(value, 0);

                    if (actualCount < expectedCount) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("collection with at least these quantities: ")
                        .appendValue(expectedCounts);
            }

            @Override
            protected void describeMismatchSafely(Collection<T> collection, Description mismatchDescription) {
                Map<T, Integer> actualCounts = countOccurrences(collection);
                mismatchDescription.appendText("collection had quantities: ")
                        .appendValue(actualCounts);
            }

            private Map<T, Integer> countOccurrences(Collection<T> collection) {
                Map<T, Integer> counts = new HashMap<>();
                for (T item : collection) {
                    counts.put(item, counts.getOrDefault(item, 0) + 1);
                }
                return counts;
            }
        };
    }

    /**
     * Matches strings with accent-insensitive comparison.
     * This uses the same normalization logic as the PostgreSQL TRANSLATE function
     * used in the backend for accent-insensitive searching.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>"Flabébé" matches "flabebe" (case and accent insensitive)</li>
     *   <li>"Pokémon" matches "pokemon"</li>
     * </ul>
     *
     * @param expected the expected string (can contain accents)
     * @return a Hamcrest matcher
     */
    public static Matcher<String> normalizedEquals(String expected) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String actual) {
                return normalize(actual).equalsIgnoreCase(normalize(expected));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("string normalized equal to ")
                        .appendValue(expected)
                        .appendText(" (normalized: ")
                        .appendValue(normalize(expected))
                        .appendText(")");
            }

            @Override
            protected void describeMismatchSafely(String actual, Description mismatchDescription) {
                mismatchDescription.appendText("was ")
                        .appendValue(actual)
                        .appendText(" (normalized: ")
                        .appendValue(normalize(actual))
                        .appendText(")");
            }
        };
    }

    /**
     * Matches strings containing the expected substring with accent-insensitive comparison.
     *
     * @param expected the expected substring
     * @return a Hamcrest matcher
     */
    public static Matcher<String> normalizedContains(String expected) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String actual) {
                return normalize(actual).toLowerCase().contains(normalize(expected).toLowerCase());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("string normalized contains ")
                        .appendValue(expected)
                        .appendText(" (normalized: ")
                        .appendValue(normalize(expected))
                        .appendText(")");
            }

            @Override
            protected void describeMismatchSafely(String actual, Description mismatchDescription) {
                mismatchDescription.appendText("was ")
                        .appendValue(actual)
                        .appendText(" (normalized: ")
                        .appendValue(normalize(actual))
                        .appendText(")");
            }
        };
    }

    /**
     * Normalizes a string by removing accents.
     * This mimics the PostgreSQL TRANSLATE function used in the backend:
     * <pre>
     * TRANSLATE(lower(column_name),
     *   'àáâãäåāăąèéêëēĕėęěìíîïìĩīĭḩùúûüũūŭůäöüßÄÖÜẞ',
     *   'aaaaaaaaaeeeeeeeeeiiiiiiiihuuuuuuuuaousbAOUsB')
     * </pre>
     *
     * @param input the input string
     * @return the normalized string with accents removed
     */
    private static String normalize(String input) {
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
}
