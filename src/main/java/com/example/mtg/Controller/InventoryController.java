package com.example.mtg.Controller;

import com.example.mtg.Helper.Helper;
import com.example.mtg.Helper.JSONHelper;
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

    @PostMapping(path = "/transaction")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    Transaction transaction(TransactionHelper th) {
        Transaction t = new Transaction();
        t.setCost(th.getCost());
        t.setDate(th.getDate());
        t.setDescription(th.getDescription());
        t.setVendor(vendorRepository.findById(th.getVednorId()).orElseGet(null));
        return transactionRepository.save(t);
    }

    @PostMapping(path = "/transaction/{transID}/card/{cardId}/foil/{isFoil}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    void transaction(@PathVariable("transID") long transID, @PathVariable("cardId") String cardId, @PathVariable(
            "isFoil") boolean isFoil) {
        Transaction t = new Transaction();
        transactionRepository.save(t);
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
        List<Card> sortedList = cards.stream()
                                     .sorted(Comparator.comparing(Card::convertCollectorNumber))
                                     .collect(Collectors.toList());
        List<CardPurchaseAssociation> cpa = cardPurchaseAssociationRepository.findAll();
        for (Card card : sortedList) {
            long count = cpa.stream().filter(c -> c.getCard().getName().equals(card.getName())).count();

            System.out.println(card.getName() + "\t" + count);
        }
        int stop = 0;

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

    @GetMapping(path = "/input/{setName}")
    public @ResponseBody
    String getInput(@PathVariable("setName") String setName) {
        List<Card> cards = cardRepository.findAllBySet(setName);
        cards = cards.stream().sorted(Comparator.comparing(Card::convertCollectorNumber))
                     .collect(Collectors.toList());
        String ans = "";
        for (Card card : cards.stream().filter(c -> c.getRarity().equals(Rarity.COMMON)).collect(Collectors.toList())) {
            ans += card.getId() +
                   "\t" +
                   card.getName() +
                   "\t" +
                   card.getCollector_number() +
                   "\t" +
                   card.getManaCost() +
                   "\t" +
                   card.getRarity() +
                   "\n";
        }
        for (Card card : cards
                .stream()
                .filter(c -> c.getRarity().equals(Rarity.UNCOMMON))
                .collect(Collectors.toList())) {
            ans += card.getId() +
                   "\t" +
                   card.getName() +
                   "\t" +
                   card.getCollector_number() +
                   "\t" +
                   card.getManaCost() +
                   "\t" +
                   card.getRarity() +
                   "\n";
        }
        for (Card card :
                cards
                        .stream()
                        .filter(c -> c.getRarity().equals(Rarity.RARE) || c.getRarity().equals(Rarity.MYTHIC))
                        .collect(Collectors.toList())) {
            ans += card.getId() +
                   "\t" +
                   card.getName() +
                   "\t" +
                   card.getCollector_number() +
                   "\t" +
                   card.getManaCost() +
                   "\t" +
                   card.getRarity() +
                   "\n";
        }
        System.out.println(ans);
        return ans;
    }

    @GetMapping(path = "/mtggoldfish/all")
    public @ResponseBody
    String mtgGoldfishAll() {
        String ans = "";
        Map<String, List<CardPurchaseAssociation>> cpas = cardPurchaseAssociationRepository.findAll().stream()
                                                                                           .collect(groupingBy(CardPurchaseAssociation::getCardId));
        Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
        Date yesteday = addDays(today,
                                -1);
        Date weekago = addDays(today,
                               -7);
        for (String key : cpas.keySet()) {
            Card card = cpas.get(key).get(0).getCard();
            long foils = cpas.get(key).stream().filter(c -> c.isFoil()).count();
            long nonFoils = cpas.get(key).stream().filter(c -> !c.isFoil()).count();
            Set<Price> prices = priceRepository.findAllByCardId(card.getId());
            if (foils > 0) {
                ans += card.getName() + " (F) \t";
                ans += card.getSet() + "\t";
                ans += card.getRarity() + "\t";
                ans += foils + "\t";
                Price todaysPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(today.toString()))
                        .findFirst()
                        .orElse(null);
                Price yestedaysPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(yesteday.toString()))
                        .findFirst()
                        .orElse(null);
                Price weekagoPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(weekago.toString()))
                        .findFirst()
                        .orElse(null);
                if (todaysPrice != null) {
                    ans += todaysPrice.getUsd_foil() + "\t";
				} else {
                    ans += "\t";
                }
                if (todaysPrice != null && yestedaysPrice != null) {
                    double a = todaysPrice.getUsd_foil();
                    double b = yestedaysPrice.getUsd_foil();
                    ans += (a - b) + "\t";
                    ans += ((a - b) / a) + "\t";
                } else {
                    ans += "\t\t";
                }
                if (todaysPrice != null && weekagoPrice != null) {
                    double a = todaysPrice.getUsd_foil();
                    double b = weekagoPrice.getUsd_foil();
                    ans += (a - b) + "\t";
                    ans += ((a - b) / a) + "\t";
                } else {
                    ans += "\t\t";
                }
                ans += "\n";
            }
            if (nonFoils > 0) {
                ans += card.getName() + "\t";
                ans += card.getSet() + "\t";
                ans += card.getRarity() + "\t";
                ans += nonFoils + "\t";
                Price todaysPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(today.toString()))
                        .findFirst()
                        .orElse(null);
                Price yestedaysPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(yesteday.toString()))
                        .findFirst()
                        .orElse(null);
                Price weekagoPrice = prices
                        .stream()
                        .filter(p -> p.getDate().toString().equals(weekago.toString()))
                        .findFirst()
                        .orElse(null);
                if (todaysPrice != null) {
                    ans += todaysPrice.getUsd_foil() + "\t";
				} else {
                    ans += "\t";
                }
                if (todaysPrice != null && yestedaysPrice != null) {
                    double a = todaysPrice.getUsd();
                    double b = yestedaysPrice.getUsd();
                    ans += (a - b) + "\t";
                    ans += ((a - b) / a) + "\t";
                } else {
                    ans += "\t\t";
                }
                if (todaysPrice != null && weekagoPrice != null) {
                    double a = todaysPrice.getUsd();
                    double b = weekagoPrice.getUsd();
                    ans += (a - b) + "\t";
                    ans += ((a - b) / a) + "\t";
                } else {
                    ans += "\t\t";
                }
                ans += "\n";
            }
        }

        List<String> names = cardPurchaseAssociationRepository
                .findAll()
                .stream()
                .map(c -> c.getCard().getName())
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .collect(Collectors.toList());
        System.out.println(ans);
        return ans;
    }

    @GetMapping(path = "/mtggoldfish")
    public @ResponseBody
    String mtgGoldfish() {
        String ans = "";
        ans += ("Card\tSet ID\tSet Name\tQuantity\tFoil\n");
        Map<String, List<CardPurchaseAssociation>> cpas = cardPurchaseAssociationRepository.findAll().stream()
                                                                                           .collect(groupingBy(CardPurchaseAssociation::getCardId));
        for (String key : cpas.keySet()) {
            Card card = cpas.get(key).get(0).getCard();
            long foils = cpas.get(key).stream().filter(c -> c.isFoil()).count();
            long nonFoils = cpas.get(key).stream().filter(c -> !c.isFoil()).count();
            if (foils > 0) {
                ans += card.getName() + "\t" + card.getSet() + "\t" + card.getSet_name() + "\t" + foils
                       + "\t" + "FOIL" + "\n";
            }
            if (nonFoils > 0) {
                ans += card.getName() + "\t" + card.getSet() + "\t" + card.getSet_name() + "\t" + nonFoils
                       + "\t" + "" + "\n";
            }
        }
        System.out.println(ans);
        return ans;
    }

    @GetMapping(path = "/temp")
    public @ResponseBody
    String temp() {

        List<Card> cards = cardRepository.findAllBySet("iko");
        List<CardPurchaseAssociation> temp = cardPurchaseAssociationRepository
                .findAll()
                .stream()
                .filter(c -> c
                                     .getCard()
                                     .getRarity()
                                     .equals(Rarity.COMMON) ||
                             c
                                     .getCard()
                                     .getRarity()
                                     .equals(Rarity.UNCOMMON))
                .collect(Collectors.toList());

        //		List<Card> cards = cardRepository.findAll().stream().filter(c -> c.getSet_name().equals("Ikoria: Lair of "
        //				+ "Behemoths")).collect(Collectors.toList());
        Date today = new java.sql.Date(Calendar.getInstance().getTime().getTime());
        Set<Price> prices = priceRepository.findAllByDate(today);
        String ans = "";
        for (Card card : cards) {
            ans += (card.getId() + "\t" + card.getName() + "\t" + card.getSet_name() + "\t" + card.isPromo() + "\t"
                    + card.isVariation() + "\t");

            Optional<Price> priceOptional = prices.stream().filter(p -> p.getCard().getId().equals(card.getId()))
                                                  .findFirst();
            if (priceOptional.isPresent()) {
                Price price = priceOptional.get();
                String usd = price.getUsd() == null ? " " : price.getUsd().toString();
                String foil = price.getUsd_foil() == null ? " " : price.getUsd_foil().toString();
                ans += (usd + "\t" + foil);
            } else {
                ans += ("\t");
            }
            ans += "\t" + (card.getCollector_number()) + "\n";

        }
        System.out.println(ans);
        return ans;
    }

    private Date addDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE,
              days);
        return new Date(c.getTimeInMillis());
    }
}
