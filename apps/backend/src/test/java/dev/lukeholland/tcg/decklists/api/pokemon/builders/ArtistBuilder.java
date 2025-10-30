package dev.lukeholland.tcg.decklists.api.pokemon.builders;

import dev.lukeholland.tcg.decklists.api.pokemon.entities.Artist;

/**
 * Fluent builder for creating {@link Artist} test entities.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create Ken Sugimori artist
 * Artist artist = ArtistBuilder.kenSugimori().build();
 *
 * // Create a custom artist
 * Artist custom = ArtistBuilder.anArtist()
 *     .withName("Custom Artist")
 *     .build();
 * }</pre>
 */
public class ArtistBuilder {

    private String name;

    private ArtistBuilder() {
    }

    public static ArtistBuilder anArtist() {
        return new ArtistBuilder();
    }

    public static ArtistBuilder kenSugimori() {
        return anArtist().withName("Ken Sugimori");
    }

    public static ArtistBuilder mitsuhiroArita() {
        return anArtist().withName("Mitsuhiro Arita");
    }

    public static ArtistBuilder koukiSaitou() {
        return anArtist().withName("Kouki Saitou");
    }

    public static ArtistBuilder atsukoNishida() {
        return anArtist().withName("Atsuko Nishida");
    }

    public ArtistBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Artist build() {
        Artist artist = new Artist();
        artist.setName(name);
        return artist;
    }
}
