package com.example.mtg.Magic;

import java.util.Optional;

public class CardJsonBuilder {
    private String typeLine;
    private String set;
    private String color;
    private String oracleText;
    private String released_at;
    private String uri;
    private String variation;
    private String printNumber;
    private String promo;
    private String set_name;
    private String collector_number;
    private String name;
    private double cmc;
    private String id;
    private String lang;
    private String manaCost;
    private String rarity;
    private double usd;
    private double usd_foil;

    public CardJsonBuilder setTypeLine(String typeLine) {
        this.typeLine = typeLine;
        return this;
    }

    public CardJsonBuilder setSet(String set) {
        this.set = set;
        return this;
    }

    public CardJsonBuilder setColor(String color) {
        this.color = color;
        return this;
    }

    public CardJsonBuilder setOracleText(String oracleText) {
        this.oracleText = oracleText;
        return this;
    }

    public CardJsonBuilder setReleased_at(String released_at) {
        this.released_at = released_at;
        return this;
    }

    public CardJsonBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public CardJsonBuilder setVariation(String variation) {
        this.variation = variation;
        return this;
    }

    public CardJsonBuilder setPrintNumber(String printNumber) {
        this.printNumber = printNumber;
        return this;
    }

    public CardJsonBuilder setPromo(String promo) {
        this.promo = promo;
        return this;
    }

    public CardJsonBuilder setSet_name(String set_name) {
        this.set_name = set_name;
        return this;
    }

    public CardJsonBuilder setCollector_number(String collector_number) {
        this.collector_number = collector_number;
        return this;
    }

    public CardJsonBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CardJsonBuilder setCmc(double cmc) {
        this.cmc = cmc;
        return this;
    }

    public CardJsonBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public CardJsonBuilder setLang(String lang) {
        this.lang = lang;
        return this;
    }

    public CardJsonBuilder setManaCost(String manaCost) {
        this.manaCost = manaCost;
        return this;
    }

    public CardJsonBuilder setRarity(String rarity) {
        this.rarity = rarity;
        return this;
    }

    public CardJsonBuilder setUsd(double usd) {
        this.usd = usd;
        return this;
    }

    public CardJsonBuilder setUsd_foil(double usd_foil) {
        this.usd_foil = usd_foil;
        return this;
    }

    public CardJsonBuilder createCardJson(Card card) {
        CardJsonBuilder builder = new CardJsonBuilder();
        builder.setTypeLine(card.getTypeLine())
               .setSet(card.getSet())
               .setCollector_number(card.getCollector_number())
               .setColor(card.getColor().toString())
               .setOracleText(card.getOracleText())
               .setReleased_at(card.getReleased_at().toString())
               .setUri(card.getURI())
               .setVariation(card.isVariation() + "")
               .setPrintNumber(card.getPrintNumber() + "")
               .setPromo(card.isPromo() + "")
               .setSet_name(card.getSet_name())
               .setName(card.getName())
               .setId(card.getId())
               .setLang(card.getLang())
               .setManaCost(card.getManaCost())
               .setRarity(card.getRarity().toString());

        return builder;
    }

    public CardJson createCardJson() {
        return new CardJson(typeLine,
                            set,
                            color,
                            oracleText,
                            released_at,
                            uri,
                            variation,
                            printNumber,
                            promo,
                            set_name,
                            collector_number,
                            name,
                            cmc,
                            id,
                            lang,
                            manaCost,
                            rarity,
                            usd,
                            usd_foil);
    }
}
