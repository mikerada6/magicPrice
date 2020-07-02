package com.example.mtg.Controller;

import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/robot")
public class RobotController {

    private static final Logger logger = LoggerFactory.getLogger(RobotController.class);

    @Autowired
    private CardRepository cardRepository;

    @GetMapping(path = "/sortingOptions")
    public @ResponseBody
    ArrayList<String> getSortingOptions() {
        ArrayList<String> options = new ArrayList<String>();
        options.add("Color");
        options.add("Set");
        options.add("Type");
        options.add("Rarity");
        options.add("Foil");
        return options;
    }

    @GetMapping(path = "/name/{firstName}/{lastName}")
    @ResponseBody
    String getPile(@PathVariable("firstName")
                           String firstName, @PathVariable("lastName")
                           String lastName) {

        return " Hello from " + firstName + " " + lastName;

    }

    @GetMapping(path = "/pile/{num}")
    @ResponseBody
    ArrayList<Card> getPile(@PathVariable("num")
                                    int num) {
        List<Card> cards = getAllCards();
        int r = (int) (Math.random() * (100 - 55)) + 55;
        return (ArrayList<Card>) cards.stream().filter(c -> cards.indexOf(c) <= r).collect(Collectors.toList());
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping(path = "/pile/{x}/{y}")
    @ResponseBody
    ArrayList<Card> getPile(@PathVariable("x")
                                    int x, @PathVariable("y")
                                    int y) {
        logger.info("We are handling a request for pile {},{}",x,y);

        List<Card> cards = getAllCards();
        int r = (int) (Math.random() * (100 - 55)) + 55;
        return (ArrayList<Card>) cards.stream().filter(c -> cards.indexOf(c) <= r).collect(Collectors.toList());
    }

    @GetMapping(path = "/grid")
    @ResponseBody
    HashMap<String, Object> getGrid() {
        List<Card> cards = getAllCards();
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 1; i <= 100; i++) {
            HashMap<String, Object> cardMap = new HashMap<String, Object>();
            ArrayList<HashMap<String, Object>> list = new ArrayList<>();
            for (int j = 0; j < Math.floor(Math.random() * 101); j++) {
                Card card = cards.get(getRandomNumberUsingNextInt(0,
                                                                  cards.size() - 1));
                cardMap.put("_name",
                            card.getName());
                cardMap.put("_block",
                            card.getSet());
                list.add(cardMap);
            }
            map.put("Grid" + i,
                    list);

        }
        return map;
    }

    private List<Card> getAllCards() {
        logger.info("Starting to fetch all the cards");
        List<Card> cards = cardRepository.findAll();
        logger.info("All {} have been retrieved from the database",
                    cards.size());
        return cards;
    }


    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

}
