package com.example.mtg.Controller;

import com.example.mtg.Helper.Helper;
import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Helper.PriceHelper;
import com.example.mtg.Helper.TransactionDetailsRequestModel;
import com.example.mtg.Helper.TransactionHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.CardPurchaseAssociation;
import com.example.mtg.Magic.Price;
import com.example.mtg.Magic.Rarity;
import com.example.mtg.Magic.Transaction;
import com.example.mtg.Magic.Vendor;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.Repository.PriceRepository;
import com.example.mtg.Repository.TransactionRepository;
import com.example.mtg.Repository.VendorRepository;
import com.example.mtg.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Controller
@RequestMapping(path = "/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private final NumberFormat percent = NumberFormat.getPercentInstance();
    @Autowired
    Environment env;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private JSONHelper jsonHelper;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private com.example.mtg.Repository.CardPurchaseAssociationRepository cardPurchaseAssociationRepository;

    @GetMapping(path = "/vendors")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    List<Vendor> vendors() {
        return vendorRepository.findAll();
    }

    @PostMapping(path = "/vendors")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    Vendor vendors(Vendor v) {
        return vendorRepository.save(v);
    }

    @GetMapping(path = "/transaction")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    List<Transaction> purchases() {
        return transactionRepository.findAll();
    }

    @PostMapping(path = "/transaction/{transID}/card/{cardId}/foil/{isFoil}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    CardPurchaseAssociation transaction(@PathVariable("transID") long transID, @PathVariable("cardId") String cardId, @PathVariable(
            "isFoil") boolean isFoil) {
        Transaction t = new Transaction();
        Card card = cardRepository
                .findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("could not find card"));
        Transaction transaction = transactionRepository
                .findById(transID)
                .orElseThrow(() -> new ResourceNotFoundException("could not find transaction"));
        CardPurchaseAssociation cardPurchaseAssociation = new CardPurchaseAssociation();
        cardPurchaseAssociation.setFoil(isFoil);
        cardPurchaseAssociation.setTransaction(transaction);
        cardPurchaseAssociation.setCard(card);
        return cardPurchaseAssociationRepository.save(cardPurchaseAssociation);

    }

    @GetMapping(path = "/transaction/dailyValue/{transID}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    void dailyValue(@PathVariable("transID") long transID) {
        Transaction transaction = transactionRepository
                .findById(transID)
                .orElseThrow(() -> new ResourceNotFoundException());
        Collection<CardPurchaseAssociation> cpas = transaction.getCardPurchaseAssociation();
        for (CardPurchaseAssociation cpa : cpas) {
            System.out.println(cpa.getCardId() +
                               '\t' +
                               cpa.getCard().getName() +
                               "\t" +
                               getCardsPrice(cpa.getCardId(),
                                             cpa.isFoil()));
        }
    }



    @GetMapping(path = "/transaction/set/{setName}/")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    void setCount(@PathVariable("setName") String setName) {
        Set<Card> cards =
                cardRepository
                        .findAllBySet(setName)
                        .stream()
                        .filter(c -> c.getRarity().equals(Rarity.RARE) || c.getRarity().equals(Rarity.MYTHIC))
                        .collect(
                                Collectors.toSet());
        Map<String, List<Card>> name = cards.stream().collect(groupingBy(Card::getName));
        Set<String> sortedList = cards.stream()
                                     .sorted(Comparator.comparing(Card::convertCollectorNumber))
                                     .map(c -> c.getName())
                                     .collect(Collectors.toSet());
        List<CardPurchaseAssociation> cpa = cardPurchaseAssociationRepository.findAll();
        for (String cardName : sortedList) {
            long count = cpa.stream().filter(c -> c.getCard().getName().equals(cardName)).count();
            Card card = cards
                    .stream()
                    .filter(c -> c.getName().equals(cardName))
                    .findFirst()
                    .orElse(null);
            if(card!=null) {
                System.out.println(card.getName() + "\t" + card.getRarity() + "\t" + count);
            }
            else
            {
                System.out.println(cardName + "\t" + "\t" + 0);
            }
        }
    }

    @GetMapping(path = "/transaction/stock")
    public @ResponseBody
    Map<Date, String> stock() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        List<CardPurchaseAssociation> associations = cardPurchaseAssociationRepository.findAll();
        List<Transaction> transactions = associations.stream().map(a -> a.getTransaction())
                                                     .collect(Collectors.toList());
        Transaction min = transactions
                .stream()
                .min((a, b) -> a.getDate().compareTo(b.getDate()))
                .orElseThrow(() -> new ResourceNotFoundException());
        double sum = 0;
        Date date = min.getDate();
        Date today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        ArrayList<Date> dates = new ArrayList<Date>();
        dates.add(date);
        while (date.before(today)) {
            date = addDays(date,
                           1);
            dates.add(date);
        }
        dates.remove(dates.size() - 1);
        HashMap<Date, String> ans = new LinkedHashMap<Date, String>();
        date = min.getDate();
        while (date.before(today)) {
            sum = 0;
            for (CardPurchaseAssociation cpa : associations) {
                Price price = null;
                try {
                    price =
                            priceRepository.findByDateAndAndCard(date,
                                                                 cpa.getCard()).orElse(null);
                } catch (Exception e) {
                    Date errorDate = date;
                    Card errorCard = cpa.getCard();
                }
                if (price != null) {
                    sum += cpa.isFoil() ? price.getUsd_foil() : price.getUsd();
                }
            }
            System.out.println(date.toString() + "\t" + sum);
            ans.put(date,
                    formatter.format(sum));
            date = addDays(date,
                           1);

        }
        return ans;
    }

    @PostMapping(path = "/transaction", consumes = "application/json")
    public @ResponseBody
    Transaction createNewTransaction(
            @RequestBody TransactionDetailsRequestModel transactionDetailsRequestModel) {
        Transaction t = new Transaction();
        t.setCost(transactionDetailsRequestModel.getCost());
        t.setDescription(transactionDetailsRequestModel.getDescription());
        Vendor vendor = vendorRepository
                .findById(transactionDetailsRequestModel.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("could not find vendor"));
        t.setVendor(vendor);
        t.setDate(transactionDetailsRequestModel.getDate());
        return transactionRepository.save(t);
    }

    @PostMapping(path = "/transaction/addCards")
    public @ResponseBody
    void transaction(
            @RequestBody
                    List<Helper> helpers) {
        for (Helper helper : helpers) {
            Optional<Card> card = cardRepository.findById(helper.getCardId());
            Optional<Transaction> t = transactionRepository.findById(helper.getTransactionId());
            boolean isFoil = helper.isFoil();
            for (int i = 0; i < helper.getCount(); i++) {
                CardPurchaseAssociation cardPurchaseAssociation = new CardPurchaseAssociation();

                if (card.isPresent() && t.isPresent()) {
                    cardPurchaseAssociation.setCard(card.get());
                    cardPurchaseAssociation.setTransaction(t.get());
                    cardPurchaseAssociation.setFoil(isFoil);
                }
                cardPurchaseAssociationRepository.save(cardPurchaseAssociation);
            }
        }
    }

    @DeleteMapping(path = "/transactionAssociation/")
    public @ResponseBody
    void delteAssociations() {
        cardPurchaseAssociationRepository.deleteAll();
    }

    @DeleteMapping(path = "/transaction/{id}")
    public @ResponseBody
    void deleteTransaction(@PathVariable("id")
                                   long id) {
        transactionRepository.deleteById(id);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(path = "/collection/export/deckstats")
    public @ResponseBody
    void exportdeckstats() {
        HashMap<String, String> replace = new HashMap<>();
        replace.put("MagicFest 2019","Grand Prix");
        replace.put("MagicFest 2020","Grand Prix");
        List<CardPurchaseAssociation> cards = cardPurchaseAssociationRepository.findAll();
        List<CardPurchaseAssociation> foils = cards.stream().filter(c -> c.isFoil()).collect(Collectors.toList());
        List<CardPurchaseAssociation> nonFoils = cards.stream().filter(c -> !c.isFoil()).collect(Collectors.toList());
        Set<String> cardIds = cards.stream().map(cpa -> cpa.getCardId()).collect(Collectors.toSet());
        System.out.println("Count,Tradelist Count,Name,Edition,Card Number,Condition,Language,Foil,Signed,Artist Proof,Altered Art,Misprint,Promo,Textless,My Price");
        for(String id: cardIds)
        {
            long foilCount = foils.stream().filter(c -> c.getCardId().equals(id)).count();
            long nonFoilCount = nonFoils.stream().filter(c -> c.getCardId().equals(id)).count();
            Card card = cards
                    .stream()
                    .filter(c -> c.getCardId().equals(id))
                    .map(c -> c.getCard())
                    .findFirst()
                    .orElse(null);
            if(nonFoilCount >0) {
                System.out.print(nonFoilCount);
                System.out.print("," + 0);
                System.out.print(",\"" + card.getName() + "\"");
                if(replace.keySet().contains(card.getSet_name())) {
                    System.out.print("," + replace.get(card.getSet_name()));
                }
                else
                {
                    System.out.print("," + card.getSet_name());
                }
                System.out.print("," + card.getCollector_number());
                System.out.print(",Near Mint");
                System.out.print(",English");
                System.out.println(",,,,,,,,<span class='note'>N/A</span>");
            }
            if(foilCount >0) {
                System.out.print(foilCount);
                System.out.print("," + 0);
                System.out.print(",\"" + card.getName() + "\"");
                System.out.print("," + card.getSet_name());
                System.out.print("," + card.getCollector_number());
                System.out.print(",Near Mint");
                System.out.print(",English");
                System.out.print(",Foil");
                System.out.println(",,,,,,,<span class='note'>N/A</span>");
            }
        }
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(path = "/collection")
    public @ResponseBody
    ArrayList<HashMap<String, String>> collection() {

        percent.setMinimumFractionDigits(2);
        percent.setMaximumFractionDigits(2);
        ArrayList<HashMap<String, String>> response = new ArrayList<>();
        List<CardPurchaseAssociation> cards = cardPurchaseAssociationRepository.findAll();


        Map<String, List<CardPurchaseAssociation>> normalCards = cards
                .stream()
                .filter(cpa -> !cpa.isFoil())
                .collect(groupingBy(CardPurchaseAssociation::getCardId));

        Map<String, List<CardPurchaseAssociation>> foilCards = cards
                .stream()
                .filter(cpa -> cpa.isFoil())
                .collect(groupingBy(CardPurchaseAssociation::getCardId));

        //.collect(groupingBy(CardPurchaseAssociation::getCardId));


        for (String key : normalCards.keySet()) {
            CardPurchaseAssociation cpa = normalCards.get(key).get(0);
            Card card = cpa.getCard();
            HashMap<String, String> singleResponse = new HashMap<String, String>();
            singleResponse.put("cardId",
                               cpa.getCardId());
            singleResponse.put("name",
                               cpa.getCard().getName());
            singleResponse.put("id",
                               cpa.getCardId());
            singleResponse.put("set",
                               card.getSet());
            singleResponse.put("rarity",
                               card.getRarity().toString());
            singleResponse.put("quantity",
                               normalCards.get(key).size()+"");

            List<Price> temp = priceRepository.findMostRecentWeekByCard(card.getId());
            if (temp.size() == 7) {
                    Price todaysPrice = temp.get(0);

                    Price yesterdayPrice = temp.get(1);
                    Price weekAgoPrice = temp.get(6);
                    if(todaysPrice.getUsd() != null) {
                        double dailyValue = todaysPrice.getUsd();
                        singleResponse.put("price",
                                           currency.format(dailyValue));
                        if(yesterdayPrice.getUsd() !=null)
                        {
                            double yesterdayValue = yesterdayPrice.getUsd();
                            singleResponse.put("dailyDollar",currency.format(dailyValue-yesterdayValue));
                            singleResponse.put("dailyPercent",percent.format((dailyValue-yesterdayValue)/dailyValue));
                            if(weekAgoPrice.getUsd() !=null)
                            {
                                double weekValue = weekAgoPrice.getUsd();
                                singleResponse.put("weeklyDollar",currency.format(dailyValue-weekValue));
                                singleResponse.put("weeklyPercent",percent.format((dailyValue-weekValue)/dailyValue));
                            }

                        }
                    }
            }
            response.add(singleResponse);
        }

        for (String key : foilCards.keySet()) {
            CardPurchaseAssociation cpa = foilCards.get(key).get(0);
            Card card = cpa.getCard();
            HashMap<String, String> singleResponse = new HashMap<String, String>();
            singleResponse.put("cardId",
                               cpa.getCardId());
            singleResponse.put("name",
                               cpa.getCard().getName()+"(F)");
            singleResponse.put("id",
                               cpa.getCardId());
            singleResponse.put("set",
                               card.getSet());
            singleResponse.put("rarity",
                               card.getRarity().toString());
            singleResponse.put("quantity",
                               foilCards.get(key).size()+"");

            List<Price> temp = priceRepository.findMostRecentWeekByCard(card.getId());
            if (temp.size() == 7) {
                Price todaysPrice = temp.get(0);

                Price yesterdayPrice = temp.get(1);
                Price weekAgoPrice = temp.get(6);
                if(todaysPrice.getUsd_foil() != null) {
                    double dailyValue = todaysPrice.getUsd_foil();
                    singleResponse.put("price",
                                       currency.format(dailyValue));
                    if(yesterdayPrice.getUsd_foil() !=null)
                    {
                        double yesterdayValue = yesterdayPrice.getUsd_foil();
                        singleResponse.put("dailyDollar",currency.format(dailyValue-yesterdayValue));
                        singleResponse.put("dailyPercent",percent.format((dailyValue-yesterdayValue)/dailyValue));
                        if(weekAgoPrice.getUsd_foil() !=null)
                        {
                            double weekValue = weekAgoPrice.getUsd_foil();
                            singleResponse.put("weeklyDollar",currency.format(dailyValue-weekValue));
                            singleResponse.put("weeklyPercent",percent.format((dailyValue-weekValue)/dailyValue));
                        }

                    }
                }
            }
            response.add(singleResponse);
        }

        return response;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(path = "/dumyTest")
    public @ResponseBody
    ArrayList<HashMap<Object, Object>> dummyTest() {
        Date today = new Date(Calendar.getInstance().getTime().getTime());
        List<Transaction> transactions = transactionRepository
                .findAll()
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());
        ArrayList<HashMap<Object, Object>> ans = new ArrayList<>();
        for (Transaction transaction : transactions) {
            HashMap<Object, Object> responce = new HashMap<>();
            transactionHelper.setTransaction(transaction);
            System.out.println(transaction.getDate() +
                               "\t" +
                               transaction.getDescription() +
                               "\t" +
                               currency.format(transaction.getCost()));
            Date date = transaction.getDate();
            ArrayList<HashMap<Object, Object>> prices = new ArrayList<>();
            while (date.compareTo(today) != 1) {
                String value = currency.format(transactionHelper.getCurrentValueAtDate(date));
                HashMap<Object, Object> priceData = new HashMap<>();
                System.out.println("\t" + date.toString() + "\t" + value);
                date = addDays(date,
                               1);
                priceData.put("date",
                              date);
                priceData.put("value",
                              value);
                prices.add(priceData);
            }
            responce.put("purchase",
                         transaction.getDate());
            responce.put("description",
                         transaction.getDescription());
            responce.put("cost",
                         transaction.getCost());
            responce.put("value",
                         prices);
            ans.add(responce);
        }
        return ans;
    }

    private Double getStringPrice(CardPurchaseAssociation cpa, Optional<Price> price) {

        if (price.isPresent()) {
            Price todaysPrice = price.get();
            if (cpa.isFoil()) {
                return todaysPrice.getUsd_foil();
            } else {
                return todaysPrice.getUsd();
            }

        }
        return null;
    }

    private Date addDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE,
              days);
        return new Date(c.getTimeInMillis());
    }

    private double getCardsPrice(String cardId, boolean isFoil) {
        Optional<Price> price = priceRepository.findMostRecentByCard(cardId);
        if (price.isPresent()) {
            if (isFoil && price.get().getUsd_foil() != null) {
                return price.get().getUsd_foil();
            } else if (!isFoil && price.get().getUsd() != null) {
                return price.get().getUsd();
            }
        }
        return 0.0;
    }

}
