package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Helper.ScryfallHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Price;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.Repository.PriceRepository;
import com.example.mtg.ResourceNotFoundException;
import me.tongfei.progressbar.ProgressBar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/price")
public class PriceController {

    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private JSONHelper jsonHelper;

    @Autowired
    private ScryfallHelper scryfallHelper;

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

    @GetMapping(path = "/short", produces = "text/csv")
    public @ResponseBody
    String getAllShort() throws IOException {
        FileWriter csvWriter = new FileWriter("new.csv");
        StringBuffer stringBuffer = new StringBuffer();
        List<Card> cards = getAllCards();
        csvWriter.append("cardId,date,usd,usd_foil,eur,tix" + System.lineSeparator());
//        cards = cards.stream().filter(c -> c.getSet().equals("iko")).collect(Collectors.toList());
        ProgressBar pb = new ProgressBar("Generating data", cards.size());
        for (Card card : cards) {
            pb.step();
            Collection<Price> prices = card.getPrice();
            for (Price price : prices) {
                String row = card.getId() + "," + price.getDate() + ",";

                row += price.getUsd() == null ? "," : price.getUsd() + ",";
                row += price.getUsd_foil() == null ? "," : price.getUsd_foil() + ",";
                row += price.getEur() == null ? "," : price.getEur() + ",";
                row += price.getTix() == null ? "," : price.getTix();
                csvWriter.append(row + System.lineSeparator());
            }
        }
        pb.close();
        logger.info("writting file");
        csvWriter.flush();
        csvWriter.close();
        return stringBuffer.toString();
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

    @GetMapping(path = "/change/card/{cardId}")
    public @ResponseBody
    HashMap<String, String> getCardChange(@PathVariable("cardId")
                                                  String cardId) {
        Locale locale = new Locale("en", "US");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        HashMap<String, String> map = new HashMap<>();
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
        map.put("card", card.getName());
        map.put("set", card.getSet().toUpperCase());
        map.put("rarity", card.getRarity().toString());
        Date today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        Date yesteday = subtractDays(today, 1);
        Date weekago = subtractDays(today, 7);
        Price todayPrice = priceRepository.findByDateAndAndCard(today, card).orElse(null);
        Price yestedayPrice = priceRepository.findByDateAndAndCard(yesteday, card).orElse(null);
        Price weekagoPrice = priceRepository.findByDateAndAndCard(weekago, card).orElse(null);
        if (todayPrice != null) {
            map.put("price", currencyFormatter.format(todayPrice.getUsd()));
        }
        if (todayPrice != null && yestedayPrice != null) {
            double temp = yestedayPrice.getUsd() - todayPrice.getUsd();
            map.put("daily", (currencyFormatter.format(temp)));
            map.put("dailyPercent", (temp / todayPrice.getUsd()) + "");
        }
        if (todayPrice != null && weekagoPrice != null) {
            double temp = weekagoPrice.getUsd() - todayPrice.getUsd();
            map.put("weekly", (currencyFormatter.format(temp)));
            map.put("weeklyPercent", (temp / todayPrice.getUsd()) + "");
        }

        return map;
    }

    @GetMapping(path = "/old")
    public @ResponseBody String getOldDate()
    {
        String good = "https://archive.scryfall.com/bulk-data/default-cards/default-cards-20200602050438.json";
        String bad ="https://archive.scryfall.com/bulk-data/default-cards/default-cards-20200602050437.json";
        String goodResult = jsonHelper.getRequest(good);
        String badResult = jsonHelper.getRequest(bad);
        String goodFile = "/Users/mradas341/IdeaProjects/magicPrice/src/main/resources/tmp/good.json";
        String badFile = "/Users/mradas341/IdeaProjects/magicPrice/src/main/resources/tmp/bad.json";

        try {
            URL website = new URL(goodResult);
            try (InputStream in = website.openStream()) {
                logger.info("Starting to download data from {}.", good);
                Files.copy(in, Paths.get(goodFile), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Data finished downloading.");
            }
            logger.info("Passed good");
        }catch(Exception e)
        {
            logger.error("Failed good with error {}", e);
        }

        try {
            URL website = new URL(badResult);
            try (InputStream in = website.openStream()) {
                logger.info("Starting to download data from {}.", bad);
                Files.copy(in, Paths.get(badFile), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Data finished downloading.");
            }
            logger.info("Passed bad");
        }catch(Exception e)
        {
            logger.error("Failed bad with error {}", e);
        }
        return "done";
    }

    @PostMapping(path = "/today")
    public @ResponseBody
    String updateBulk() {
        JSONArray arrayData = null;
        ArrayList<Price> priceArrayList = new ArrayList<Price>();
        try {
            arrayData = scryfallHelper.downloadDailyBulkData();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arrayData != null) {
            ProgressBar pb = new ProgressBar("Updating all card prices", arrayData.size());
            for (int i = 0; i < arrayData.size(); i++) {
                pb.step();
                JSONObject datum = ((JSONObject) arrayData.get(i));
                JSONObject prices = ((JSONObject) datum.get("prices"));
                String cardId = datum.containsKey("id") ? datum.get("id").toString() : null;
                double usd = -1;
                double tix = -1;
                double eur = -1;
                double usd_foil = -1;
                //TODO make this better
                try {
                    usd = prices.get("usd") == null ? null : Double.parseDouble((String) prices.get("usd"));
                } catch (Exception e) {

                }
                try {
                    tix = prices.get("tix") == null ? null : Double.parseDouble((String) prices.get("tix"));
                } catch (Exception e) {

                }
                try {
                    eur = prices.get("eur") == null ? null : Double.parseDouble((String) prices.get("eur"));
                } catch (Exception e) {

                }
                try {
                    usd_foil = prices.get("usd_foil") == null ? null : Double.parseDouble((String) prices.get("usd_foil"));
                } catch (Exception e) {

                }

                Price price = new Price();
                Card card = cardRepository.findById(cardId)
                        .orElse(null);
                if (card != null) {
                    price.setCard(card);
                    price.setDate(new java.sql.Date(Calendar.getInstance().getTime().getTime()));
                    price.setUsd(usd == -1.0 ? null : usd);
                    price.setUsd_foil(usd_foil == -1.0 ? null : usd_foil);
                    price.setTix(tix == -1.0 ? null : tix);
                    price.setEur(eur == -1.0 ? null : eur);
                    if (usd + usd_foil + tix + eur > -4) {
                        //only save a price if its not all null
                        priceArrayList.add(price);
                        if (priceArrayList.size() == 500) {
                            pb.setExtraMessage("Starting to save data");
                            priceRepository.saveAll(priceArrayList);
                            pb.setExtraMessage("Updating all card prices");
                            priceArrayList.clear();
                        }
                    }
                }
            }
            priceRepository.saveAll(priceArrayList);
            pb.close();
        }
        logger.info("Done price update");
        return "done";
    }
    @DeleteMapping(path = "/{priceId}")
    public @ResponseBody
    void deleteByid(@PathVariable("priceId") Long priceId)
    {
        priceRepository.deleteById(priceId);
    }


    @DeleteMapping(path = "/today")
    public @ResponseBody
    Set<Price> deleteToday() {
        Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
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
            if(temp.size()>0) {
                logger.info("Found {} that will be deleted for date {}", temp.size(), date.toString());
//                priceRepository.deleteAll(temp);
                for(Price del: temp)
                {
                    logger.info("Delete id {}", del.getId());
                    priceRepository.delete(del);
                }
            }
            else
            {
                logger.info("nothing to delete for date {}." , date.toString());
            }

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
