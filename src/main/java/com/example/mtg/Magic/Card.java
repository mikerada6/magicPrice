package com.example.mtg.Magic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Entity public class Card implements Comparable<Card> {

	private static final Logger logger = LoggerFactory.getLogger(Card.class);

	@Id
	String id;
	HashMap<String, Boolean> legalities;
	private String name;
	private Color color;
	private Rarity rarity;
	private String collector_number;
	private double cmc;
	//card type needs to be an array since a card can be of multiple types.  i.e. artifact creature
	private ArrayList<Type> types;
	private String set_name;
	private Date released_at;
	@JsonIgnore
	private Collection<Price> price;
	private double printNumber;
	private boolean promo;
	private boolean variation;
	private String lang;
	private String manaCost;
	private String uri;
	private String oracleText;

	public Card() {
		name = null;
		color = null;
		rarity = null;
		collector_number = "";
		types = new ArrayList<Type>();
		cmc = -1;
	}

	@javax.persistence.Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addCardType(Type t) {
		types.add(t);
	}

	@SuppressWarnings("JpaAttributeTypeInspection")
	public ArrayList<Type> getTypes() {
		return types;
	}

	public void setTypes(ArrayList types) {
		this.types = types;
	}

	public void setType(Type type) {
		ArrayList<Type> t = new ArrayList<>();
		t.add(type);
		setTypes(t);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Rarity getRarity() {
		return rarity;
	}

	public void setRarity(Rarity rarity) {
		this.rarity = rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = Rarity.fromLabel(rarity);
	}

	public String getCollector_number() {
		return collector_number;
	}

	public void setCollector_number(String printNumber) {
		this.collector_number = printNumber;
	}

	public double getCmc() {
		return cmc;
	}

	public void setCmc(double cmc) {
		this.cmc = cmc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Card o) {
		//TODO finish
		return 0;
	}

	private HashMap<String, Object> createMapping(String methodName, Object dataType) {
		HashMap<String, Object> temp = new HashMap<>();
		temp.put("methodName", methodName);
		temp.put("dataType", dataType);
		return temp;
	}

	public boolean setValueString(String variableName, String value) {
		String methodName = "set" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
		Method method = null;
		HashMap<String, HashMap<String, Object>> scryFallMapping = new HashMap<>();
		scryFallMapping.put("name", createMapping("setName", String.class));
		scryFallMapping.put("id", createMapping("setId", String.class));
		scryFallMapping.put("type_line", createMapping("setTypeFromScryFall", String.class));
		scryFallMapping.put("lang", createMapping("setLang", String.class));
		scryFallMapping.put("rarity", createMapping("setRarity", String.class));
		scryFallMapping.put("collector_number", createMapping("setCollector_number", String.class));
		scryFallMapping.put("cmc", createMapping("setCmc", double.class));
		scryFallMapping.put("released_at", createMapping("setReleased_at", Date.class));
		scryFallMapping.put("collector_number", createMapping("setCollector_number", String.class));
		scryFallMapping.put("promo", createMapping("setPromo", boolean.class));
		scryFallMapping.put("variation", createMapping("setVariation", boolean.class));
		scryFallMapping.put("set_name", createMapping("setSet_name", String.class));
		scryFallMapping.put("mana_cost", createMapping("setManaCost", String.class));
		scryFallMapping.put("uri", createMapping("setURI", String.class));
		scryFallMapping.put("oracle_text", createMapping("setOracleText", String.class));

		if (!scryFallMapping.containsKey(variableName)) {
			logger.error("Could not find where to put variable {}.  Error: ", variableName);
			return false;
		}
		Class dataType = (Class) scryFallMapping.get(variableName).get("dataType");

		try {
			method = Card.class
					.getDeclaredMethod(scryFallMapping.get(variableName).get("methodName").toString(), dataType);
		} catch (NoSuchMethodException ex) {
			logger.error("Could not find method {}.  Error: ", methodName, ex);
			return false;
		}
		try {
			if (dataType.equals(int.class)) {
				method.invoke(this, Integer.parseInt(value));
			} else if (dataType.equals(double.class)) {
				method.invoke(this, Double.parseDouble(value));
			} else if (dataType.equals(String.class)) {
				method.invoke(this, value);
			} else if (dataType.equals(boolean.class)) {
				method.invoke(this, Boolean.parseBoolean(value));
			} else if (dataType.equals(Date.class)) {
				method.invoke(this, Date.valueOf(value));
			} else {
				logger.error("Could not find an action for type {} ", dataType);
				return false;
			}
		} catch (IllegalAccessException ex) {
			logger.error("IllegalAccessException {}.  Error: ", methodName, ex);
			return false;
		} catch (InvocationTargetException ex) {
			logger.error("IllegalAccessException {}.  Error: ", methodName, ex);
			return false;
		} catch (Exception ex) {
			logger.error("Exception {}.  Error: ", methodName, ex);
			return false;
		}
		return true;
	}

	public Date getReleased_at() {
		return released_at;
	}

	public void setReleased_at(Date released_at) {
		this.released_at = released_at;
	}

	public void setTypeFromScryFall(String type_line) {
		if (type_line.contains("Creature")) {
			types.add(Type.CREATURE);
		}
		if (type_line.contains("Land")) {
			types.add(Type.LAND);
		}
		if (type_line.contains("Artifact")) {
			types.add(Type.ARTIFACT);
		}
		if (type_line.contains("Instant")) {
			types.add(Type.INSTANT);
		}
		if (type_line.contains("Sorcery")) {
			types.add(Type.SORCERY);
		}
		if (type_line.contains("Enchantment")) {
			types.add(Type.ENCHANTMENT);
		}
		if (type_line.contains("Planeswalker")) {
			types.add(Type.PLANESWALKER);
		}
		if (type_line.contains("Tribal")) {
			types.add(Type.TRIBAL);
		}
		if (type_line.contains("Token")) {
			types.add(Type.TOKEN);
		}
		if (type_line.contains("Emblem")) {
			types.add(Type.EMBLEM);
		}
		if (type_line.contains("Plane")) {
			types.add(Type.PLANE);
		}
		if (type_line.contains("Scheme")) {
			types.add(Type.SCHEME);
		}
		if (type_line.contains("Phenomenon")) {
			types.add(Type.PHENOMENON);
		}
		if (type_line.contains("Vanguard")) {
			types.add(Type.VANGUARD);
		}
		if (type_line.contains("Hero")) {
			types.add(Type.HERO);
		}
		if (type_line.contains("Conspiracy")) {
			types.add(Type.CONSPIRACY);
		}

		if (types.isEmpty()) {
			logger.warn("Did not set any type.  Was given {}", type_line);
		}
	}

	public void setColorFromScryFall(JSONArray colors) {
		int count = 0;
		if (colors.length() == 0) {
			color = Color.COLORLESS;
			return;
		}
		for (int i = 0; i < colors.length(); i++) {
			String tempColor = colors.get(i).toString();
			if (tempColor.equalsIgnoreCase("W")) {
				color = Color.WHITE;
				count++;
			}
			if (tempColor.equalsIgnoreCase("U")) {
				color = Color.BLUE;
				count++;
			}
			if (tempColor.equalsIgnoreCase("B")) {
				color = Color.BLACK;
				count++;
			}
			if (tempColor.equalsIgnoreCase("R")) {
				color = Color.RED;
				count++;
			}
			if (tempColor.equalsIgnoreCase("G")) {
				color = Color.GREEN;
				count++;
			}
		}

		if (count == 0) {
			color = Color.COLORLESS;
		}
		if (count > 2) {
			color = Color.GOLD;
		}
	}

	public String getSet_name() {
		return set_name;
	}

	public void setSet_name(String set) {
		this.set_name = set;
	}

	@OneToMany(mappedBy = "card")
	public Collection<Price> getPrice() {
		return price;
	}

	public void setPrice(Collection<Price> price) {
		this.price = price;
	}

	public HashMap<String, Boolean> getLegalities() {
		return legalities;
	}

	public void setLegalities(HashMap<String, Boolean> legalities) {
		this.legalities = legalities;
	}

	public class FirstNameSorter implements Comparator<Card> {
		public int compare(Card o1, Card o2) {
			return 0;
		}
	}

	public class LastNameSorter implements Comparator<Card> {
		public int compare(Card o1, Card o2) {
			return 0;
		}
	}

	public class AgeSorter implements Comparator<Card> {
		public int compare(Card o1, Card o2) {
			return 0;
		}
	}

	public Map<String, Price> priceHashMap() {
		return price.stream().collect(Collectors.toMap(s -> s.getDate().toString(), s -> s));
	}

	public double getPrintNumber() {
		return printNumber;
	}

	public void setPrintNumber(double printNumber) {
		this.printNumber = printNumber;
	}

	public boolean isPromo() {
		return promo;
	}

	public void setPromo(boolean promo) {
		this.promo = promo;
	}

	public boolean isVariation() {
		return variation;
	}

	public void setVariation(boolean variation) {
		this.variation = variation;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getManaCost() {
		return manaCost;
	}

	public void setManaCost(String manaCost) {
		this.manaCost = manaCost;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String URI) {
		this.uri = URI;
	}

	public String getOracleText() {
		return oracleText;
	}

	public void setOracleText(String oracleText) {
		this.oracleText = oracleText;
	}
}

