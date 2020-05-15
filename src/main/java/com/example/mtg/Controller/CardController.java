package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.ResourceNotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Controller @RequestMapping(path = "/cards") public class CardController {

	private static final Logger logger = LoggerFactory.getLogger(CardController.class);

	@Autowired
	Environment env;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private JSONHelper jsonHelper;

	private static final String[] fieldsToBeUsed = { "name", "id", "type_line", "lang", "rarity", "collector_number",
			"cmc", "released_at", "promo", "variation", "set_name", "mana_cost", "uri", "oracle_text" };

	@GetMapping(path = "/{cardId}")
	public @ResponseBody
	Card getCard(
			@PathVariable("cardId")
					String cardId) {
		return cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
	}

	@GetMapping(path = "/count")
	public @ResponseBody
	long count() {
		return cardRepository.count();
	}

	@GetMapping(path = "")
	public @ResponseBody
	List<Card> getAllCards() {
		logger.info("Starting to fetch all the cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All {} have been retrieved from the database", cards.size());
		return cards;
	}

	@GetMapping("/sets")
	public @ResponseBody
	List<String> getAllSets() {
		ArrayList<String> sets = new ArrayList<>();
		List<Card> cards = getAllCards();
		return cards.stream().map(c -> c.getSet_name()).collect(Collectors.toList());
	}

	@PostMapping(path = "/card")
	public @ResponseBody
	Card addCard(Card c) {
		return cardRepository.save(c);
	}

	@PostMapping(path = "/today")
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
			String result = "go";
			lastScryFallCall = System.currentTimeMillis();
			if (!StringUtils.isEmpty(result)) {
				result = jsonHelper.getRequest(url);
				JSONObject jsonObject = getJsonObject(result);
				cont = jsonObject.has("has_more") && jsonObject.getBoolean("has_more");
				url = jsonObject.has("next_page") && cont ? jsonObject.getString("next_page") : null;
				JSONArray data = jsonObject.has("data") ? jsonObject.getJSONArray("data") : null;

				int stop = 0;

				for (int i = 0; i < data.length(); i++) {
					JSONObject datum = data.getJSONObject(i);
					Card card = new Card();
					try {
						String language = datum.has("lang") ? datum.getString("lang") : "";
						if (language.equalsIgnoreCase("en")) {

							//we are only going to save cards if they are in english.
							for (String field : fieldsToBeUsed) {
								if (datum.has(field)) {
									String fieldValue = datum.get(field).toString();
									logger.trace("Setting field {} with a value of {}", field, fieldValue);
									card.setValueString(field, fieldValue);
								} else {
									logger.warn("Could not find the value of {} for card {}", field, datum);
								}
							}
							JSONArray colors = datum.has("color_identity") ?
									datum.getJSONArray("color_identity") :
									null;
							HashMap<String, Boolean> legality = getFormatLegaltiy(datum);
							card.setColorFromScryFall(colors);
							card.setLegalities(legality);

							cards.add(card);

							if (cards.size() == 500) {
								logger.info("Starting saving of {} cards.", cards.size());
								cardRepository.saveAll(cards);

								logger.info("Ending saving");
								cards.clear();
							}
						}
					} catch (Exception e) {
						logger.error("Ran into error {} .  With card {} CARD:", e, datum.get("id"));
					}
				}
			}

		}
		logger.info("Starting final saving of {} cards.", cards.size());
		cardRepository.saveAll(cards);

		logger.info("Ending saving");
		cards.clear();
		return cards;
	}

	private HashMap<String, Boolean> getFormatLegaltiy(JSONObject element) {
		HashMap<String, Boolean> ans = new HashMap<String, Boolean>();
		Iterator<String> keys = element.getJSONObject("legalities").keys();
		for (Iterator<String> it = keys; it.hasNext(); ) {
			String key = it.next();
			boolean legal = !element.getJSONObject("legalities").get(key).toString().equals("not_legal");
			ans.put(key, legal);
		}
		return ans;
	}

	public JSONObject getJsonObject(String jsonString) {
		return new JSONObject(jsonString);
	}

}
