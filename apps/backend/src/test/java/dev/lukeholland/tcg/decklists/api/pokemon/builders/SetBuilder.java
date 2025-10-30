package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Set;

/**
 * Fluent builder for creating {@link Set} test entities.
 * <p>
 * Provides convenient factory methods for common Pokémon TCG sets
 * and supports full customization of all set fields for test scenarios.
 * </p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a Base Set
 * Set baseSet = SetBuilder.baseSet().build();
 *
 * // Create a custom set for testing
 * Set customSet = SetBuilder.aSet()
 *     .withSetId("custom1")
 *     .withName("Custom Set")
 *     .withSeries("Custom Series")
 *     .withTotal(100)
 *     .build();
 * }</pre>
 */
public class SetBuilder {

    private String setId;
    private String name;
    private String series;
    private Integer printedTotal;
    private Integer total;
    private String ptcgoCode;
    private String releaseDate;
    private String updatedAt;
    private String imageSymbol;
    private String imageLogo;

    private SetBuilder() {
    }

    /**
     * Creates a new SetBuilder instance.
     *
     * @return a new SetBuilder
     */
    public static SetBuilder aSet() {
        return new SetBuilder();
    }

    /**
     * Creates a Base Set.
     *
     * @return a SetBuilder configured for Base Set
     */
    public static SetBuilder baseSet() {
        return aSet()
                .withSetId("base1")
                .withName("Base Set")
                .withSeries("Base")
                .withPrintedTotal(102)
                .withTotal(102)
                .withPtcgoCode("BS")
                .withReleaseDate("1999-01-09");
    }

    /**
     * Creates a Jungle set.
     *
     * @return a SetBuilder configured for Jungle
     */
    public static SetBuilder jungle() {
        return aSet()
                .withSetId("base2")
                .withName("Jungle")
                .withSeries("Base")
                .withPrintedTotal(64)
                .withTotal(64)
                .withPtcgoCode("JU")
                .withReleaseDate("1999-06-16");
    }

    /**
     * Creates a Fossil set.
     *
     * @return a SetBuilder configured for Fossil
     */
    public static SetBuilder fossil() {
        return aSet()
                .withSetId("base3")
                .withName("Fossil")
                .withSeries("Base")
                .withPrintedTotal(62)
                .withTotal(62)
                .withPtcgoCode("FO")
                .withReleaseDate("1999-10-10");
    }

    /**
     * Creates a Sword & Shield base set.
     *
     * @return a SetBuilder configured for Sword & Shield
     */
    public static SetBuilder swordAndShield() {
        return aSet()
                .withSetId("swsh1")
                .withName("Sword & Shield")
                .withSeries("Sword & Shield")
                .withPrintedTotal(202)
                .withTotal(216)
                .withPtcgoCode("SSH")
                .withReleaseDate("2020-02-07");
    }

    /**
     * Creates a Scarlet & Violet base set.
     *
     * @return a SetBuilder configured for Scarlet & Violet
     */
    public static SetBuilder scarletAndViolet() {
        return aSet()
                .withSetId("sv1")
                .withName("Scarlet & Violet")
                .withSeries("Scarlet & Violet")
                .withPrintedTotal(198)
                .withTotal(258)
                .withPtcgoCode("SVI")
                .withReleaseDate("2023-03-31");
    }

    /**
     * Sets the set ID.
     *
     * @param setId the set identifier (e.g., "base1")
     * @return this builder for method chaining
     */
    public SetBuilder withSetId(String setId) {
        this.setId = setId;
        return this;
    }

    /**
     * Sets the set name.
     *
     * @param name the set name (e.g., "Base Set")
     * @return this builder for method chaining
     */
    public SetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the series name.
     *
     * @param series the series name (e.g., "Base", "Sword & Shield")
     * @return this builder for method chaining
     */
    public SetBuilder withSeries(String series) {
        this.series = series;
        return this;
    }

    /**
     * Sets the printed total.
     *
     * @param printedTotal the printed total of cards in the set
     * @return this builder for method chaining
     */
    public SetBuilder withPrintedTotal(Integer printedTotal) {
        this.printedTotal = printedTotal;
        return this;
    }

    /**
     * Sets the actual total.
     *
     * @param total the actual total of cards (including secret rares)
     * @return this builder for method chaining
     */
    public SetBuilder withTotal(Integer total) {
        this.total = total;
        return this;
    }

    /**
     * Sets the PTCGO code.
     *
     * @param ptcgoCode the Pokemon Trading Card Game Online code
     * @return this builder for method chaining
     */
    public SetBuilder withPtcgoCode(String ptcgoCode) {
        this.ptcgoCode = ptcgoCode;
        return this;
    }

    /**
     * Sets the release date.
     *
     * @param releaseDate the release date (ISO format)
     * @return this builder for method chaining
     */
    public SetBuilder withReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    /**
     * Sets the updated at timestamp.
     *
     * @param updatedAt the last update timestamp
     * @return this builder for method chaining
     */
    public SetBuilder withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Sets the set symbol image URL.
     *
     * @param imageSymbol the set symbol image URL
     * @return this builder for method chaining
     */
    public SetBuilder withImageSymbol(String imageSymbol) {
        this.imageSymbol = imageSymbol;
        return this;
    }

    /**
     * Sets the set logo image URL.
     *
     * @param imageLogo the set logo image URL
     * @return this builder for method chaining
     */
    public SetBuilder withImageLogo(String imageLogo) {
        this.imageLogo = imageLogo;
        return this;
    }

    /**
     * Builds the Set entity with configured values.
     *
     * @return a new Set instance
     */
    public Set build() {
        Set set = new Set();
        set.setSetId(setId);
        set.setName(name);
        set.setSeries(series);
        set.setPrintedTotal(printedTotal);
        set.setTotal(total);
        set.setPtcgoCode(ptcgoCode);
        set.setReleaseDate(releaseDate);
        set.setUpdatedAt(updatedAt);
        set.setImageSymbol(imageSymbol);
        set.setImageLogo(imageLogo);
        return set;
    }
}
