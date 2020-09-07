package com.example.mtg.Magic;

import io.magicthegathering.javasdk.resource.ForeignData;
import io.magicthegathering.javasdk.resource.Legality;
import io.magicthegathering.javasdk.resource.Ruling;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Card2 extends io.magicthegathering.javasdk.resource.Card {

    @Id
    private String id;
    private String layout;
    private String name;
    private String manaCost;
    private double cmc;
    private String type;
    private String rarity;
    private String text;
    private String originalText;
    private String flavor;
    private String artist;
    private String number;
    private String power;
    private String toughness;
    private String loyalty;
    private int multiverseid = -1;
    private String imageName;
    private String watermark;
    private String border;
    private boolean timeshifted;
    private int hand;
    private int life;
    private boolean reserved;
    private String releaseDate;
    private boolean starter;
    private String set;
    private String setName;
    private String imageUrl;
    private BigDecimal priceHigh;
    private BigDecimal priceMid;
    private BigDecimal priceLow;
    private BigDecimal onlinePriceHigh;
    private BigDecimal onlinePriceMid;
    private BigDecimal onlinePriceLow;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
    public void setLayout(String layout) {
        this.layout = layout;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getManaCost() {
        return manaCost;
    }

    @Override
    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    @Override
    public double getCmc() {
        return cmc;
    }

    @Override
    public void setCmc(double cmc) {
        this.cmc = cmc;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getRarity() {
        return rarity;
    }

    @Override
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getOriginalText() {
        return originalText;
    }

    @Override
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    @Override
    public String getFlavor() {
        return flavor;
    }

    @Override
    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String getPower() {
        return power;
    }

    @Override
    public void setPower(String power) {
        this.power = power;
    }

    @Override
    public String getToughness() {
        return toughness;
    }

    @Override
    public void setToughness(String toughness) {
        this.toughness = toughness;
    }

    @Override
    public String getLoyalty() {
        return loyalty;
    }

    @Override
    public void setLoyalty(String loyalty) {
        this.loyalty = loyalty;
    }

    @Override
    public int getMultiverseid() {
        return multiverseid;
    }

    @Override
    public void setMultiverseid(int multiverseid) {
        this.multiverseid = multiverseid;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String getWatermark() {
        return watermark;
    }

    @Override
    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

    @Override
    public String getBorder() {
        return border;
    }

    @Override
    public void setBorder(String border) {
        this.border = border;
    }

    @Override
    public boolean isTimeshifted() {
        return timeshifted;
    }

    @Override
    public void setTimeshifted(boolean timeshifted) {
        this.timeshifted = timeshifted;
    }

    @Override
    public int getHand() {
        return hand;
    }

    @Override
    public void setHand(int hand) {
        this.hand = hand;
    }

    @Override
    public int getLife() {
        return life;
    }

    @Override
    public void setLife(int life) {
        this.life = life;
    }

    @Override
    public boolean isReserved() {
        return reserved;
    }

    @Override
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public String getReleaseDate() {
        return releaseDate;
    }

    @Override
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public boolean isStarter() {
        return starter;
    }

    @Override
    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    @Override
    public String getSet() {
        return set;
    }

    @Override
    public void setSet(String set) {
        this.set = set;
    }

    @Override
    public String getSetName() {
        return setName;
    }

    @Override
    public void setSetName(String setName) {
        this.setName = setName;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public BigDecimal getPriceHigh() {
        return priceHigh;
    }

    @Override
    public void setPriceHigh(BigDecimal priceHigh) {
        this.priceHigh = priceHigh;
    }

    @Override
    public BigDecimal getPriceMid() {
        return priceMid;
    }

    @Override
    public void setPriceMid(BigDecimal priceMid) {
        this.priceMid = priceMid;
    }

    @Override
    public BigDecimal getPriceLow() {
        return priceLow;
    }

    @Override
    public void setPriceLow(BigDecimal priceLow) {
        this.priceLow = priceLow;
    }

    @Override
    public BigDecimal getOnlinePriceHigh() {
        return onlinePriceHigh;
    }

    @Override
    public void setOnlinePriceHigh(BigDecimal onlinePriceHigh) {
        this.onlinePriceHigh = onlinePriceHigh;
    }

    @Override
    public BigDecimal getOnlinePriceMid() {
        return onlinePriceMid;
    }

    @Override
    public void setOnlinePriceMid(BigDecimal onlinePriceMid) {
        this.onlinePriceMid = onlinePriceMid;
    }

    @Override
    public BigDecimal getOnlinePriceLow() {
        return onlinePriceLow;
    }

    @Override
    public void setOnlinePriceLow(BigDecimal onlinePriceLow) {
        this.onlinePriceLow = onlinePriceLow;
    }
}
