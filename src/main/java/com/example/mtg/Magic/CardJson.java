package com.example.mtg.Magic;

public class CardJson
{
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

    public CardJson(String typeLine, String set, String color, String oracleText, String released_at, String uri, String variation, String printNumber, String promo, String set_name, String collector_number, String name, double cmc, String id, String lang, String manaCost, String rarity, double usd, double usd_foil) {
        this.typeLine = typeLine;
        this.set = set;
        this.color = color;
        this.oracleText = oracleText;
        this.released_at = released_at;
        this.uri = uri;
        this.variation = variation;
        this.printNumber = printNumber;
        this.promo = promo;
        this.set_name = set_name;
        this.collector_number = collector_number;
        this.name = name;
        this.cmc = cmc;
        this.id = id;
        this.lang = lang;
        this.manaCost = manaCost;
        this.rarity = rarity;
        this.usd = usd;
        this.usd_foil = usd_foil;
    }

    public String getTypeLine ()
    {
        return typeLine;
    }

    public void setTypeLine (String typeLine)
    {
        this.typeLine = typeLine;
    }

    public String getSet ()
    {
        return set;
    }

    public void setSet (String set)
    {
        this.set = set;
    }

    public String getColor ()
    {
        return color;
    }

    public void setColor (String color)
    {
        this.color = color;
    }

    public String getOracleText ()
{
    return oracleText;
}

    public void setOracleText (String oracleText)
    {
        this.oracleText = oracleText;
    }

    public String getReleased_at ()
    {
        return released_at;
    }

    public void setReleased_at (String released_at)
    {
        this.released_at = released_at;
    }

    public String getUri ()
    {
        return uri;
    }

    public void setUri (String uri)
    {
        this.uri = uri;
    }

    public String getVariation ()
    {
        return variation;
    }

    public void setVariation (String variation)
    {
        this.variation = variation;
    }

    public String getPrintNumber ()
    {
        return printNumber;
    }

    public void setPrintNumber (String printNumber)
    {
        this.printNumber = printNumber;
    }

    public String getPromo ()
    {
        return promo;
    }

    public void setPromo (String promo)
    {
        this.promo = promo;
    }

    public String getSet_name ()
    {
        return set_name;
    }

    public void setSet_name (String set_name)
    {
        this.set_name = set_name;
    }

    public String getCollector_number ()
    {
        return collector_number;
    }

    public void setCollector_number (String collector_number)
    {
        this.collector_number = collector_number;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public double getCmc ()
    {
        return cmc;
    }

    public void setCmc (double cmc)
    {
        this.cmc = cmc;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getLang ()
{
    return lang;
}

    public void setLang (String lang)
    {
        this.lang = lang;
    }

    public String getManaCost ()
    {
        return manaCost;
    }

    public void setManaCost (String manaCost)
    {
        this.manaCost = manaCost;
    }

    public String getRarity ()
    {
        return rarity;
    }

    public void setRarity (String rarity)
    {
        this.rarity = rarity;
    }

    public double getUsd() {
        return usd;
    }

    public void setUsd(double usd) {
        this.usd = usd;
    }

    public double getUsd_foil() {
        return usd_foil;
    }

    public void setUsd_foil(double usd_foil) {
        this.usd_foil = usd_foil;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [typeLine = "+typeLine+", set = "+set+", color = "+color+", oracleText = "+oracleText+", released_at = "+released_at+", uri = "+uri+", variation = "+variation+", printNumber = "+printNumber+", promo = "+promo+", set_name = "+set_name+", collector_number = "+collector_number+", name = "+name+", cmc = "+cmc+", id = "+id+", lang = "+lang+", manaCost = "+manaCost+", rarity = "+rarity+"]";
    }
}
