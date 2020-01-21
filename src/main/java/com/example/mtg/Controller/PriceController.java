package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Price;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.Repository.PriceRepository;
import com.example.mtg.ResourceNotFoundException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller @RequestMapping(path = "/price") public class PriceController {

	private static final Logger logger = LoggerFactory.getLogger(PriceController.class);
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private JSONHelper jsonHelper;

	@GetMapping(path = "/history/{cardId}")
	public @ResponseBody
	Map<String, Object> history(
			@PathVariable("cardId")
					String cardId) {
		Set<Card> cards = cardRepository.findAllById(cardId);
		HashMap<String, Object> ans = new HashMap<>();
		Collection<Price> prices;
		for (Card card : cards) {
			ans.put("cardId", card.getId());
			ans.put("card", card.getName());
			HashMap<String, Object> history = new HashMap<>();
			prices = card.getPrice();
			for (Price price : prices) {
				HashMap<String, String> pricesMap = new HashMap<>();
				if (price.getUsd() >= 0) {
					pricesMap.put("usd", price.getUsd().toString());
				}
				if (price.getUsd_foil() >= 0) {
					pricesMap.put("usd_foil", price.getUsd_foil().toString());
				}
				if (price.getTix() >= 0) {
					pricesMap.put("tix", price.getTix().toString());
				}
				if (price.getEur() >= 0) {
					pricesMap.put("eur", price.getEur().toString());
				}
				history.put(price.getDate().toString(), pricesMap);
			}
			ans.put("history", history);
		}

		return ans;
	}

	@GetMapping(path = "/history/")
	public @ResponseBody
	Map<String, Object> history() {
		logger.info("Starting to get all cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All " + cards.size() + " have been retreived from the database");
		HashMap<String, Object> ans = new HashMap<>();
		HashMap<String, Object> finalAns = new HashMap<>();
		Collection<Price> prices;
		logger.info("Starting to loop through all the cards");
		for (Card card : cards) {
			if (finalAns.size() % 1000 == 0) {
				logger.info("We have looped through " + finalAns.size() + "cards so far");
			}
			ans.put("card", card.getName());
			HashMap<String, Object> history = new HashMap<>();
			prices = card.getPrice();
			for (Price price : prices) {
				HashMap<String, String> pricesMap = new HashMap<>();
				if (price.getUsd() >= 0) {
					pricesMap.put("usd", price.getUsd().toString());
				}
				if (price.getUsd_foil() >= 0) {
					pricesMap.put("usd_foil", price.getUsd_foil().toString());
				}
				if (price.getTix() >= 0) {
					pricesMap.put("tix", price.getTix().toString());
				}
				if (price.getEur() >= 0) {
					pricesMap.put("eur", price.getEur().toString());
				}
				history.put(price.getDate().toString(), pricesMap);
			}
			ans.put("history", history);
			finalAns.put(card.getId(), ans);
		}
		logger.info("All cards have been looped through");

		return finalAns;
	}

	@GetMapping(path = "/update")
	public @ResponseBody
	String update() {
		ArrayList<Price> priceArrayList = new ArrayList<Price>();
		logger.info("Start update");

		String url = "https://api.scryfall.com/cards";

		logger.info("Starting update");

		boolean cont = true;

		while (cont) {
			priceArrayList.clear();
			String result = jsonHelper.getRequest(url);
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
					object = temp.has("object") ? temp.get("object").getAsString() : null;
					if (!object.equalsIgnoreCase("card")) {
						int stop = 0 / 0;
					}

					String name = temp.has("name") ? temp.get("name").getAsString() : null;
					try {
						String languare = temp.has("lang") ? temp.get("lang").getAsString() : "";
						if (!languare.equalsIgnoreCase("en")) {
							continue;
						}
						String cardId = temp.has("id") ? temp.get("id").getAsString() : null;
						Card card = cardRepository.findById(cardId)
								.orElseThrow(() -> new ResourceNotFoundException("could not find card" + cardId));
						Price price = new Price();
						price.setCard(card);

						Double usd = getPrice(temp, "usd");
						Double usd_foil = getPrice(temp, "usd_foil");
						Double tix = getPrice(temp, "tix");
						Double eur = getPrice(temp, "eur");

						price.setDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
						price.setUsd(usd);
						price.setUsd_foil(usd_foil);
						price.setTix(tix);
						price.setEur(eur);
						if (usd + usd_foil + tix + eur > -4) {
							//only save a price if its not all null
							priceArrayList.add(price);
						}
					} catch (Exception e) {
						logger.error("Ran into error " + e + ".  With card " + name + "CARD:" + name);
					}
				}
				logger.info("Starting to save");
				priceRepository.saveAll(priceArrayList);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("End save");
			}
		}

		logger.info("Done price update");
		return "Done";
	}

	private Double getPrice(JsonObject element, String priceType) {
		if (element.get("prices").getAsJsonObject().has(priceType) && !element.get("prices").getAsJsonObject()
				.get(priceType).isJsonNull()) {
			return element.get("prices").getAsJsonObject().get(priceType).getAsDouble();
		}
		return -1.0;
	}

}
