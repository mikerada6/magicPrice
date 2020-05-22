package com.example.mtg.domain;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Color;
import com.example.mtg.Magic.Rarity;
import com.example.mtg.Magic.Type;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

public class DomainUtils {
	public static <T> T getTestObject(Class<T> aClazz) {
		if (aClazz.getName().equals(Card.class.getName())) {
			Card c = new Card();
			c.setId("e9d5aee0-5963-41db-a22b-cfea40a967a3");
			//TODO add legalities
			c.setName("Dusk // Dawn");
			c.setColor(Color.WHITE);
			c.setRarity(Rarity.RARE);
			c.setCollector_number("63");
			c.setCmc(9);
			c.setTypes(new ArrayList<Type>(Arrays.asList(Type.SORCERY)));
			c.setName("Commander 2019");
			c.setReleased_at(Date.valueOf("2019-08-23"));
			c.setPrintNumber(0);
			c.setPromo(false);
			c.setVariation(false);
			c.setLang("en");
			c.setManaCost("{2}{W}{W} // {3}{W}{W}");
			c.setURI("https://api.scryfall.com/cards/e9d5aee0-5963-41db-a22b-cfea40a967a3");
			c.setOracleText(null);
			c.setSet("c19");

			return (T) c;
		}
		throw new UnsupportedOperationException(aClazz.getName() + " not expected");
	}
}
