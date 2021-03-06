package com.example.mtg.Helper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScryfallHelper {
    private static final Logger logger = LoggerFactory.getLogger(ScryfallHelper.class);

    @Autowired
    private JSONHelper jsonHelper;

    public void bruteForce() {
        String url = "https://archive.scryfall.com/bulk-data/default-cards/default-cards-";
        int year = 2020;
        int month = 8;
        int day = 30;
        for (int hour = 9; hour < 60; hour++) {
            for (int minute = 35; minute < 60; minute++) {
                for (int second = 0; second < 60; second++) {
                    String date = String.format("%04d",
                                                year) +
                                  String.format("%02d",
                                                month) +
                                  String.format("%02d",
                                                day) +
                                  String.format("%02d",
                                                hour) +
                                  String.format("%02d",
                                                minute) +
                                  String.format("%02d",
                                                second);
                    String attemptURL = url + date + ".json";
                    try {
                        Thread.sleep(100);
                        String result = jsonHelper.getRequest(attemptURL);
                        if (result.contains("AccessDenied")) {
                            int error = 0 / 0;
                        }
                        logger.info("tried {} worked.",
                                    attemptURL);
                        System.out.println("result: " + result);
                    } catch (Exception e) {
                        logger.error("tried {} and failed.",
                                     attemptURL);
                    }
                }
            }
        }
    }

    public JSONArray downloadDailyBulkData() throws ParseException, IOException {
        String url = "https://api.scryfall.com/bulk-data";
        String defaultCardsLocation = null;

        String result = jsonHelper.getRequest(url);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject object = (JSONObject) obj;
        String updateTime = "";
        if (object.containsKey("data")) {
            JSONArray data = (JSONArray) object.get("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject datum = (JSONObject) data.get(i);
                int stop = 0;
                if (datum.get("name").equals("Default Cards")) {
                    defaultCardsLocation = (String) datum.get("download_uri");
                    updateTime = (String) datum.get("updated_at");
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyLLdd'_'kkmm");
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(updateTime);
//                    LocalDateTime localDateTime = LocalDateTime.parse(updateTime);
                    updateTime = zonedDateTime.format(format);
                    break;

                }
            }
            int stop = 0;
        }

        String file = "/Users/mradas341/IdeaProjects/magicPrice/src/main/resources/tmp/" + updateTime + ".json";
//        file = "/../../resources/tmp/"+updateTime+".json";

        URL website = new URL(defaultCardsLocation);
        try (InputStream in = website.openStream()) {
            logger.info("Starting to download data from {}.",
                        defaultCardsLocation);
            Files.copy(in,
                       Paths.get(file),
                       StandardCopyOption.REPLACE_EXISTING);
            logger.info("Data finished downloading.");
        }

        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            //Read JSON file
            obj = jsonParser.parse(reader);

            return (JSONArray) obj;

        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException {}",
                         e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException {}",
                         e);
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error("ParseException {}",
                         e);
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject loadJson(String fileLocation)
    {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(fileLocation)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            return (JSONObject) obj;

        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException {}",
                         e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException {}",
                         e);
            e.printStackTrace();
        } catch (ParseException e) {
            logger.error("ParseException {}",
                         e);
            e.printStackTrace();
        }
        return null;
    }

}
