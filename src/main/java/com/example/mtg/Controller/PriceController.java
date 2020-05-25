package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Change;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller @RequestMapping(path = "/price") public class PriceController {

	private static final Logger logger = LoggerFactory.getLogger(PriceController.class);
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private JSONHelper jsonHelper;

	private static Date subtractDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -days);
		return new Date(c.getTimeInMillis());
	}

	@GetMapping(path = "/")
	public @ResponseBody
	List<Price> getAll() {
		logger.info("Starting to fetch all the prices");
		List<Price> prices = priceRepository.findAll();
		logger.info("All {} have been retrieved from the database", prices.size());
		return prices;
	}

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

	@GetMapping(path = "/topten/{format}")
	public @ResponseBody
	List<Change> topTenStandard(
			@PathVariable("format")
					String format) {
		String ans = "";
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		Date yesterday = subtractDays(today, 1);
		Date weekAgo = subtractDays(today, 7);
		List<Card> cards = getAllCards();
		logger.info("Starting to filter");
		List<Card> cardsInFormat = cards.stream()
				.filter(c -> c.getLegalities() != null && c.getLegalities().get(format) && !c.isPromo())
				.collect(Collectors.toList());
		logger.info("All {} cards have been filtered from the list", cardsInFormat.size());
		ArrayList<Change> changes = new ArrayList<Change>();
		for (Card card : cardsInFormat) {
			card.getPrice();
			Change change = new Change();
			Map<String, Price> map = card.priceHashMap();
			if (map.containsKey(today.toString()) && map.containsKey(yesterday.toString()) && map
					.containsKey(weekAgo.toString())) {
				change.setCard(card);
				change.setToday(map.get(today.toString()));
				change.setYesterday(map.get(yesterday.toString()));
				change.setWeekAgo(map.get(weekAgo.toString()));
				changes.add(change);
			}
		}
		List<Change> temp = changes.stream()
				.filter(c -> c.getToday().getUsd() != null && c.getWeekAgo().getUsd() != null)
				.sorted(Comparator.comparingDouble(Change::getDailyChange).reversed()).collect(Collectors.toList());
		return temp.subList(0, 10);
	}

	@GetMapping(path = "/count")
	public @ResponseBody
	long count() {
		return priceRepository.count();
	}

	@GetMapping(path = "/history")
	public @ResponseBody
	Map<String, Object> history() {
		List<Card> cards = getAllCards();
		HashMap<String, Object> ans = new HashMap<>();
		HashMap<String, Object> finalAns = new HashMap<>();
		Collection<Price> prices;
		logger.info("Starting to loop through all the cards");
		for (Card card : cards) {
			if (finalAns.size() % 1000 == 0) {
				logger.info("We have looped through {} cards so far", finalAns.size());
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

	@PostMapping(path = "/extrapolateMissingData")
	public @ResponseBody
	String extrapolateMissingData(String startDate, String endDate) {
		List<Price> prices = getAll();
		Map<String, List<Price>> priceMap = prices.stream().collect(Collectors.groupingBy(t -> t.getCard().getId()));
		Date start = Date.valueOf(startDate);
		Date end = Date.valueOf(endDate);
		Map<String, List<Price>> startPriceMap = prices.stream().filter(p -> p.getDate().equals(start))
				.collect(Collectors.groupingBy(t -> t.getCard().getId()));
		Map<String, List<Price>> endPriceMap = prices.stream().filter(p -> p.getDate().equals(end))
				.collect(Collectors.groupingBy(t -> t.getCard().getId()));

		ArrayList<String> both = new ArrayList<>();
		both.addAll(startPriceMap.keySet());
		both.addAll(endPriceMap.keySet());
		Set<String> set = both.stream().filter(s -> endPriceMap.containsKey(s) && startPriceMap.containsKey(s))
				.collect(Collectors.toSet());
		Date day1 = Date.valueOf("2020-02-22");
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.DATE, 1);
		Date newDate = new Date(cal.getTimeInMillis());
		ArrayList<Price> priceUpdates = new ArrayList<>();
		for (String key : set) {

			Price a = startPriceMap.get(key).get(0);
			Price b = endPriceMap.get(key).get(0);
			logger.info("Adding price data for {}", a.getCard().getName());
			Price one = new Price();

			Card c = cardRepository.findById(a.getCard().getId()).orElse(null);
			one.setCard(c);
			one.setDate(day1);
			double usd = a.getUsd() > 0 && b.getUsd() > 0 ? linearize(0, a.getUsd(), 2, a.getUsd(), 1) : -1;
			double usd_foil = a.getUsd_foil() > 0 && b.getUsd_foil() > 0 ?
					linearize(0, a.getUsd_foil(), 2, a.getUsd_foil(), 1) :
					-1;
			double tix = a.getTix() > 0 && b.getTix() > 0 ? linearize(0, a.getTix(), 2, a.getTix(), 1) : -1;
			double eur = a.getEur() > 0 && b.getEur() > 0 ? linearize(0, a.getEur(), 2, a.getEur(), 1) : -1;
			one.setUsd(usd);
			one.setUsd_foil(usd_foil);
			one.setTix(tix);
			one.setEur(eur);

			usd = a.getUsd() > 0 && b.getUsd() > 0 ? linearize(0, a.getUsd(), 2, a.getUsd(), 2) : -1;
			usd_foil = a.getUsd_foil() > 0 && b.getUsd_foil() > 0 ?
					linearize(0, a.getUsd_foil(), 2, a.getUsd_foil(), 2) :
					-1;
			tix = a.getTix() > 0 && b.getTix() > 0 ? linearize(0, a.getTix(), 2, a.getTix(), 2) : -1;
			eur = a.getEur() > 0 && b.getEur() > 0 ? linearize(0, a.getEur(), 2, a.getEur(), 2) : -1;

			priceUpdates.add(one);
			if (priceUpdates.size() > 500) {
				logger.info("Saving prices");
				priceRepository.saveAll(priceUpdates);
				priceUpdates.clear();

			}
		}
		logger.info("Saving prices leftover");
		priceRepository.saveAll(priceUpdates);
		priceUpdates.clear();
		logger.info("done");
		return "done";
	}
	@GetMapping(path="/change/set/{set}")
	public @ResponseBody
	List<HashMap<String, String>> getsetChange(@PathVariable("set")
			String set)
	{
		List<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
		List<Card> cards = cardRepository.findAllBySet(set);
		for(Card card: cards)
		{
			try {
				maps.add(getCardChange(card.getId()));
			}catch(Exception e)
			{
				logger.error("Had an error with card {}. Id of {}", card.getName(), card.getId());
			}
		}
		maps = maps.stream().filter(c -> c.containsKey("daily")).sorted((a, b) -> a.get("daily").compareTo(b.get("daily")))
				.collect(Collectors.toList());
		return maps;
	}

	@GetMapping(path="/change/card/{cardId}")
	public @ResponseBody
	HashMap<String,String> getCardChange(@PathVariable("cardId")
			String cardId)
	{
		Locale locale = new Locale("en", "US");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
		HashMap<String,String> map = new HashMap<>();
		Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
		map.put("card",card.getName());
		map.put("set", card.getSet().toUpperCase());
		map.put("rarity", card.getRarity().toString());
		Date today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
		Date yesteday = subtractDays(today,1);
		Date weekago = subtractDays(today,7);
		Price todayPrice = priceRepository.findByDateAndAndCard(today, card).orElse(null);
		Price yestedayPrice = priceRepository.findByDateAndAndCard(yesteday, card).orElse(null);
		Price weekagoPrice = priceRepository.findByDateAndAndCard(weekago, card).orElse(null);
		if(todayPrice !=null)
		{
			map.put("price",currencyFormatter.format(todayPrice.getUsd()));
		}
		if(todayPrice !=null && yestedayPrice!=null)
		{
			double temp = yestedayPrice.getUsd()-todayPrice.getUsd();
			map.put("daily",(currencyFormatter.format(temp)));
			map.put("dailyPercent",(temp/todayPrice.getUsd())+"");
		}
		if(todayPrice !=null && weekagoPrice!=null)
		{
			double temp = weekagoPrice.getUsd()-todayPrice.getUsd();
			map.put("weekly",(currencyFormatter.format(temp)));
			map.put("weeklyPercent",(temp/todayPrice.getUsd())+"");
		}

		return map;
	}

	@PostMapping(path = "/today")
	public @ResponseBody
	String update() {
		ArrayList<Price> priceArrayList = new ArrayList<Price>();
		logger.info("Start update");

		String url = "https://api.scryfall.com/cards";

		logger.info("Starting update");
		long lastScryFallCall = 0L;
		boolean cont = true;
		priceArrayList.clear();
		while (cont) {
			long timeSpent = System.currentTimeMillis() - lastScryFallCall;
			int minTime = 200;
			if (timeSpent < minTime) {
				try {
					double sleepTime = minTime - timeSpent;
					logger.info("sleeping for {}", sleepTime);
					Thread.sleep(minTime - timeSpent);
					logger.trace("done sleeping");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				logger.info("did not sleep");
			}
			lastScryFallCall = System.currentTimeMillis();
			String result = jsonHelper.getRequest(url);
			if (!StringUtils.isEmpty(result)) {
				JsonObject jsonObject=null;
				try {
					jsonObject = new JsonParser().parse(result).getAsJsonObject();
				}catch(Exception e)
				{
					System.out.println("Result: " + result);
					int errorOut=0/0;
				}
				String object = jsonObject.has("object") ? jsonObject.get("object").getAsString() : null;
				int total_cards = jsonObject.has("total_cards") ? jsonObject.get("total_cards").getAsInt() : null;
				cont = jsonObject.has("has_more") && jsonObject.get("has_more").getAsBoolean();
				url = jsonObject.has("next_page") && cont ? jsonObject.get("next_page").getAsString() : null;
				JsonArray data = jsonObject.has("data") ? jsonObject.getAsJsonArray("data") : null;
				for (JsonElement datum : data) {
					JsonObject temp = datum.getAsJsonObject();
					object = temp.has("object") ? temp.get("object").getAsString() : null;
					if (!object.equalsIgnoreCase("card")) {
						int stop = 0 / 0;
					}

					String name = temp.has("name") ? temp.get("name").getAsString() : null;
					try {
						String language = temp.has("lang") ? temp.get("lang").getAsString() : "";
						if (!language.equalsIgnoreCase("en")) {
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
						price.setUsd(usd == -1.0 ? null : usd);
						price.setUsd_foil(usd_foil == -1.0 ? null : usd_foil);
						price.setTix(tix == -1.0 ? null : tix);
						price.setEur(eur == -1.0 ? null : eur);
						if (usd + usd_foil + tix + eur > -4) {
							//only save a price if its not all null
							priceArrayList.add(price);
							if (priceArrayList.size() == 500) {
								logger.info("Starting to save");
								priceRepository.saveAll(priceArrayList);
								logger.info("End save");
								priceArrayList.clear();
							}
						}
					} catch (Exception e) {
						logger.error("Ran into error {}.  With card {} " + name, e);
					}
				}
			}
		}
		logger.info("Starting to save all the rest");
		priceRepository.saveAll(priceArrayList);
		logger.info("End save");
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

	@DeleteMapping(path = "/today")
	public @ResponseBody
	Set<Price> deleteToday() {
		//		List<Price> prices = getAll();
		Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		//		List<Price> toDelete = prices.stream().filter(p -> p.getDate().toString().equals(today.toString()))
		//				.collect(Collectors.toList());
		//		logger.info("Found {} that will be deleted", toDelete.size());
		//		logger.info("Size before delete {}", priceRepository.count());
		//		priceRepository.deleteAll(toDelete);
		//		logger.info("Size after delete {}", priceRepository.count());
		//		return;
		logger.info("Starting to fetch all the prices from {} ", today.toString());
		Set<Price> todays = priceRepository.findAllByDate(today);
		logger.info("All {} have been retrieved from the database", todays.size());
		logger.info("Start to delete");
		priceRepository.deleteAll(todays);
		logger.info("End delete");
		return todays;
	}

	@DeleteMapping(path = "/cleanup")
	public @ResponseBody
	List<Price> cleanup() {
		logger.info("Starting cleanup prices");
		List<Date> dates = priceRepository.findAllDate();
		ArrayList<Price> toDelete = new ArrayList<Price>();
		for (Date date : dates) {
			Set<Price> prices = priceRepository.findAllByDate(date);
			ArrayList<Price> deleteThese = new ArrayList<>();
			Map<String, List<Price>> map = prices.stream()
					.collect(Collectors.groupingBy(s -> s.getCard().getId() + "|" + s.getDate().toString()));
			List<Price> temp = map.entrySet().stream().filter(s -> s.getValue().size() > 1)
					.map(s -> s.getValue().get(0)).collect(Collectors.toList());
			logger.info("Found {} that will be deleted for date {}", temp.size(), date.toString());
			toDelete.addAll(temp);

		}
		logger.info("Going to delete a total of {} records", toDelete.size());
		logger.info("Size before delete {}", priceRepository.count());
		priceRepository.deleteAll(toDelete);
		logger.info("Size after delete {}", priceRepository.count());
		return toDelete;
	}

	private double linearize(double x1, double y1, double x2, double y2, double input) {
		if (x2 == x1)
			return x1;
		double slope = (y2 - y1) / (x2 - x1);
		double b = y2 - slope * x1;
		return input * slope + b;
	}

	private List<Card> getAllCards() {
		logger.info("Starting to fetch all the cards");
		List<Card> cards = cardRepository.findAll();
		logger.info("All {} have been retrieved from the database", cards.size());
		return cards;
	}
}
