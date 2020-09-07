package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Helper.ScryfallHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.CardJson;
import com.example.mtg.Magic.CardJsonBuilder;
import com.example.mtg.Magic.Format;
import com.example.mtg.Magic.Price;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.Repository.FormatRepository;
import com.example.mtg.Repository.PriceRepository;
import com.example.mtg.ResourceNotFoundException;
import me.tongfei.progressbar.ProgressBar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Controller
@RequestMapping(path = "/cards")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);
    private static final String[] fieldsToBeUsed = {"name", "id", "type_line", "lang", "rarity", "collector_number",
            "cmc", "released_at", "promo", "variation", "set_name", "mana_cost", "uri", "oracle_text", "set"};
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private FormatRepository formatRepository;
    @Autowired
    PriceRepository priceRepository;
    @Autowired
    private JSONHelper jsonHelper;
    @Autowired
    private ScryfallHelper scryfallHelper;

    @GetMapping(path = "/{cardId}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    CardJson getCard(
            @PathVariable("cardId")
                    String cardId) {
        logger.info("got a request for card id: {}.", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
        CardJsonBuilder builder = new CardJsonBuilder();
        builder = builder.createCardJson(card);
        Optional<Price> price = priceRepository.findMostRecentByCard(card.getId());
        if(price.isPresent())
        {
            Price tempPrice = price.get();
            builder.setUsd(tempPrice.getUsd());
            builder.setUsd_foil(tempPrice.getUsd_foil());
        }
        return builder.createCardJson();
    }
    @GetMapping(path = "/{cardId}/price")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    Card getCardPrice(
            @PathVariable("cardId")
                    String cardId) {
        logger.info("got a request for card id: {}.", cardId);
        return cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException());
    }

    @GetMapping(path = "/sets")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    List<String> getCardsFromSet() {
        logger.info("got a request for all sets");

        return cardRepository.findAllSets().stream().sorted().collect(Collectors.toList());
    }

    @GetMapping(path = "/set/{setName}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    ArrayList<CardJson> getSets(
            @PathVariable("setName")
                    String setName) {
        logger.info("got a request for cards from set {}.", setName);

        List<Card> cards = cardRepository
                .findAllBySet(setName)
                .stream()
                .sorted(Card::compareTo)
                .collect(Collectors.toList());

            ArrayList<CardJson> cardJsons = new ArrayList<>();
            for(Card card: cards)
            {
                CardJsonBuilder builder = new CardJsonBuilder();
                builder = builder.createCardJson(card);
                Optional<Price> price = priceRepository.findMostRecentByCard(card.getId());
                if(price.isPresent())
                {
                    Price tempPrice = price.get();
                    if(tempPrice.getUsd() !=null)
                    builder.setUsd(tempPrice.getUsd());
                    if(tempPrice.getUsd_foil() !=null)
                    builder.setUsd_foil(tempPrice.getUsd_foil());
                }
                cardJsons.add(builder.createCardJson());
        }
            return cardJsons;
    }


    @GetMapping(path = "count/{num}")
    public @ResponseBody
    ArrayList<Card> getCard(
            @PathVariable("num")
                    int num) {
        List<Card> cards = cardRepository.findAll();
        return (ArrayList<Card>) cards.stream().filter(c -> cards.indexOf(c)<=num).collect(Collectors.toList());
    }

    @DeleteMapping(path = "/{cardId}")
    public @ResponseBody
    void deleteCard(
            @PathVariable("cardId")
                    String cardId) {
        cardRepository.deleteById(cardId);
        return;
    }

    @PostMapping(path = "/{cardId}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    Card addCard(
            @PathVariable("cardId")
                    String cardId) {
        String url = "https://api.scryfall.com/cards/" + cardId;
        Card c = new Card();
        return cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("No such card "
                                                                                                       + "with id " + cardId));

    }


    @GetMapping(path = "")
    public @ResponseBody
    List<Card> getAllCards() {
        logger.info("Starting to fetch all the cards");
        List<Card> cards = cardRepository.findAll();
        logger.info("All {} have been retrieved from the database", cards.size());
        return cards;
    }

    @PostMapping(path = "")
    public @ResponseBody
    Card addCard(Card c) {
        return cardRepository.save(c);
    }

    @GetMapping(path = "/bruteForce")
    public @ResponseBody
    void bruteForce() {
        scryfallHelper.bruteForce();
    }

    @PostMapping(path = "/today")
    public @ResponseBody
    ArrayList<Card> update() {
        ArrayList<Card> cards = new ArrayList<Card>();
        JSONArray arrayData = null;
        try {
            arrayData = scryfallHelper.downloadDailyBulkData();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arrayData != null) {
            ProgressBar pb = new ProgressBar("Updating all card prices", arrayData.size());
            for (Object arrayDatum : arrayData) {
                pb.step();
                JSONObject datum = ((JSONObject) arrayDatum);
                if (datum.get("lang").equals("en")) {
                    Card card = new Card();
                    card.setId((String) datum.get("id"));
                    card.setName((String) datum.get("name"));
                    card.setSet((String) datum.get("set"));
                    card.setSet_name((String) datum.get("set_name"));
                    card.setURI((String) datum.get("uri"));
                    card.setManaCost((String) datum.get("mana_cost"));
//                  card.setOracleText((String) datum.get("oracle_text"));
                    card.setRarity((String) datum.get("rarity"));
                    card.setCmc((Double) datum.get("cmc"));
                    card.setCollector_number((String) datum.get("collector_number"));
                    String released_at = (String) datum.get("released_at");
                    card.setReleased_at(Date.valueOf(released_at));
                    card.setTypeLine((String) datum.get("type_line"));
                    card.setReserved((boolean) datum.get("reserved"));
                    if (datum.containsKey("colors")) {
                        JSONArray JSONcolors = (JSONArray) datum.get("colors");
                        String colors = "";
                        for (Object tempColor : JSONcolors) {
                            colors += (String) tempColor;
                        }
                        card.setColor(colors);
                    }

                    JSONObject formats = ((JSONObject) datum.get("legalities"));
                    Set<String> keys = formats.keySet();
                    for(String key: keys)
                    {
                        if(formats.get(key).equals("legal")) {
                            Optional<Format> formatOptional = formatRepository.findByName(key);
                            if (!formatOptional.isPresent()) {
                                Format newFormat = new Format();
                                newFormat.setName(key);
                                formatOptional = Optional.of(formatRepository.save(newFormat));
                            }
                            Format format = formatOptional.get();
                            format.addCard(card);
                            card.addFormat(format);
                        }
                    }

                    cards.add(card);
                }
                if (cards.size() == 500) {
                    cardRepository.saveAll(cards);
                    cards.clear();
                }
            }
            cardRepository.saveAll(cards);
            logger.info("Ending saving");
            cards.clear();
            pb.close();
        }
        return cards;

    }
    @PostMapping(path = "/empty")
    public @ResponseBody
    String empty()
    {
        int stop =0;
        Map<String, List<Card>> map = formatRepository.findByName("standard").get().getCards().stream().collect(groupingBy(Card::getSet));
        return "done";
    }


    @PostMapping(path = "/format/{name}")
    public @ResponseBody
    Format createFormat(@PathVariable("name")
                                         String name)
    {
        Format newFormat = new Format();
        newFormat.setName(name);
        return formatRepository.save(newFormat);
    }

}
