package dev.lukeholland.tcg.decklists.api.pokemon.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon_set")
public class Set {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "set_id", nullable = false, unique = true)
    private String setId;

    @Column(name = "name")
    private String name;

    @Column(name = "series")
    private String series;

    @Column(name = "printed_total")
    private Integer printedTotal;

    @Column(name = "total")
    private Integer total;

    @Column(name = "ptcgo_code")
    private String ptcgoCode;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "image_symbol")
    private String imageSymbol;

    @Column(name = "image_logo")
    private String imageLogo;

    public Set() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public Integer getPrintedTotal() {
        return printedTotal;
    }

    public void setPrintedTotal(Integer printedTotal) {
        this.printedTotal = printedTotal;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getPtcgoCode() {
        return ptcgoCode;
    }

    public void setPtcgoCode(String ptcgoCode) {
        this.ptcgoCode = ptcgoCode;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getImageSymbol() {
        return imageSymbol;
    }

    public void setImageSymbol(String imageSymbol) {
        this.imageSymbol = imageSymbol;
    }

    public String getImageLogo() {
        return imageLogo;
    }

    public void setImageLogo(String imageLogo) {
        this.imageLogo = imageLogo;
    }
}