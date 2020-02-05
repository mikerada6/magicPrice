package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.ResourceNotFoundException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller @RequestMapping(path = "/card") public class CardController {

	private static final Logger logger = LoggerFactory.getLogger(CardController.class);

	@Autowired
	Environment env;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private JSONHelper jsonHelper;

	@GetMapping(path = "/{cardId}")
	public @ResponseBody
	Card getCard(
			@PathVariable("cardId")
					String cardId) {
		return cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
	}

	@GetMapping(path = "/get")
	public @ResponseBody
	List<Card> getAllCards() {
		logger.info("Starting to fetch all the cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All " + cards.size() + " have been retrieved from the database");
		return cards;
	}

	@GetMapping(path = "/standard")
	public @ResponseBody
	List<Card> getAllStandardCards() {
		logger.info("Starting to fetch all the cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All " + cards.size() + " have been retrieved from the database");
		logger.info("Starting to filter");
		List<Card> ans = cards.stream().filter(c -> c.getLegalities() != null && c.getLegalities().get("standard"))
				.collect(Collectors.toList());
		logger.info("All " + ans.size() + " cards have been filtered from the list");
		return ans;
	}

	@GetMapping(path = "/modern")
	public @ResponseBody
	List<Card> getAllModernCards() {
		logger.info("Starting to fetch all the cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All " + cards.size() + " have been retrieved from the database");
		logger.info("Starting to filter");
		List<Card> ans = cards.stream().filter(c -> c.getLegalities() != null && c.getLegalities().get("modern"))
				.collect(Collectors.toList());
		logger.info("All " + ans.size() + " cards have been filtered from the list");
		return ans;
	}

	@GetMapping(path = "/update")
	public @ResponseBody
	ArrayList<Card> update() {
		ArrayList<Card> cards = new ArrayList<Card>();
		String url = "https://api.scryfall.com/cards";
		logger.info("Starting update");
		long lastScryFallCall = 0L;

		boolean cont = true;

		while (cont) {
			long timeSpent = System.currentTimeMillis() - lastScryFallCall;
			if (timeSpent < 50) {
				try {
					logger.info("sleeping for {}", 50 - timeSpent);
					Thread.sleep(50 - timeSpent);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				logger.info("did not sleep");
			}
			String result = jsonHelper.getRequest(url);
			lastScryFallCall = System.currentTimeMillis();
			if (!StringUtils.isEmpty(result)) {
				JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
				String object = jsonObject.has("object") ? jsonObject.get("object").getAsString() : null;
				int total_cards = jsonObject.has("total_cards") ? jsonObject.get("total_cards").getAsInt() : null;
				cont = jsonObject.has("has_more") && jsonObject.get("has_more").getAsBoolean();
				url = jsonObject.has("next_page") && cont ? jsonObject.get("next_page").getAsString() : null;
				logger.info("Next page is at " + url);
				JsonArray data = jsonObject.has("data") ? jsonObject.getAsJsonArray("data") : null;
				for (JsonElement datum : data) {
					JsonObject temp = datum.getAsJsonObject();
					String name = temp.has("name") ? temp.get("name").getAsString() : null;
					Card card = new Card();
					try {
						String id = temp.has("id") ? temp.get("id").getAsString() : null;
						String language = temp.has("lang") ? temp.get("lang").getAsString() : "";
						if (language.equalsIgnoreCase("en")) {
							//we are only going to save cards if they are in english.
							int cmc = temp.has("cmc") ? temp.get("cmc").getAsInt() : -1;
							String set = temp.has("set") ? temp.get("set").getAsString() : "";
							String type_line = temp.has("type_line") ? temp.get("type_line").getAsString() : "";
							String rarity = temp.has("rarity") ? temp.get("rarity").getAsString() : "";
							JsonArray colors = temp.has("color_identity") ?
									temp.get("color_identity").getAsJsonArray() :
									null;
							String collector_number = temp.has("collector_number") ?
									temp.get("collector_number").getAsString() :
									"";
							HashMap<String, Boolean> legality = getFormatLegaltiy(temp);
							Date released_at = temp.has("released_at") ?
									Date.valueOf(temp.get("released_at").getAsString()) :
									null;
							boolean promo = temp.has("promo") && temp.get("promo").getAsBoolean();
							boolean variation = temp.has("variation") && temp.get("variation").getAsBoolean();
							card.setName(name);
							card.setCmc(cmc);
							card.setId(id);
							card.setSet_name(set);
							card.setTypeFromScryFall(type_line);
							card.setRarity(rarity);
							card.setColorFromScryFall(colors);
							card.setCollector_number(collector_number);
							card.setReleased_at(released_at);
							card.setLegalities(legality);
							card.setPromo(promo);
							card.setVariation(variation);

							cards.add(card);
							if (cards.size() == 500) {
								logger.info("Starting saving of " + cards.size() + " cards.");
								cardRepository.saveAll(cards);

								logger.info("Ending saving");
								cards.clear();
							}
						}
					} catch (Exception e) {
						logger.error("Ran into error " + e + ".  With card " + name + "CARD:" + card);
					}
				}
			}

		}
		cardRepository.saveAll(cards);
		logger.info("Starting final saving of " + cards.size() + " cards.");
		cardRepository.saveAll(cards);

		logger.info("Ending saving");
		cards.clear();
		return cards;
	}

	private HashMap<String, Boolean> getFormatLegaltiy(JsonObject element) {
		HashMap<String, Boolean> ans = new HashMap<String, Boolean>();
		Set<String> keys = element.get("legalities").getAsJsonObject().keySet();
		for (String key : keys) {
			boolean legal = !element.get("legalities").getAsJsonObject().get(key).getAsString().equals("not_legal");
			ans.put(key, legal);
		}
		return ans;
	}

}
