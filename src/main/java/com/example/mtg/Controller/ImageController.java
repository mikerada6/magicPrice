package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Color;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.ResourceNotFoundException;
import me.tongfei.progressbar.ProgressBar;
import nu.pattern.OpenCV;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgcodecs.Imgcodecs.imread;

@Controller
@RequestMapping(path = "/images")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private JSONHelper jsonHelper;

    @GetMapping(path = "/{cardId}")
    public @ResponseBody
    byte[] getCard(
            @PathVariable("cardId")
                    String cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("No such card "
                                                                                                    +
                                                                                                    "with id " +
                                                                                                    cardId));
        JSONObject json = JSONHelper.getJsonObject(jsonHelper.getRequest(card.getURI()));
        Image image = null;
        if (json.has("image_uris")) {
            JSONObject map = (JSONObject) json.get("image_uris");
            String urlString = "";
            if (map.has("png")) {
                urlString = (String) map.get("png");
            } else {
                urlString = (String) map.get("large");
            }

            try {
                String directory = "./main/resources/img/set/" + card.getSet();
                byte[] response = getImageFromURL(urlString);
                saveImageToFile(directory,
                                card.getId(),
                                response);
                return response;
            } catch (IOException e) {
            }
        }
        return null;
    }

    @GetMapping(path = "/saveAll")
    public @ResponseBody
    String saveAll() {

        String directory = "./main/resources/img/all";

        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }


        File file = new File(directory);
        List<File> fileList = Arrays.asList(file.listFiles());
        List<Card> cards = cardRepository.findAll();
        ProgressBar pb = new ProgressBar("Downloading images",
                                         cards.size());
        for (Card card : cards) {
            if (fileList.stream().filter(f -> f.toString().contains(card.getId())).count() == 0) {
                JSONObject json = JSONHelper.getJsonObject(jsonHelper.getRequest(card.getURI()));
                Image image = null;
                if (json.has("image_uris")) {
                    JSONObject map = (JSONObject) json.get("image_uris");
                    String urlString = "";
                    if (map.has("large")) {
                        urlString = (String) map.get("large");
                    } else {
                        urlString = (String) map.get("normal");
                    }

                    try {
                        byte[] response = getImageFromURL(urlString);
                        saveImageToFile(directory,
                                        card.getId(),
                                        response);
                    } catch (Exception e) {
                        logger.error("We had en error trying to save {}.  The error was {}.",
                                     card.getId(),
                                     e);
                    }
                }
            }
        }
        pb.close();
        return "done";
    }

    @GetMapping(path = "/pearson")
    public @ResponseBody
    Card getPearson() {

        double[] testImage = getArrayOfImage("src/main/resources/testImages/wind.jpg");

        ArrayList<double[]> allImagesFlat = new ArrayList<>();

        logger.debug("Getting all the cards from {}",
                    "iko");
        List<Card> cards = cardRepository.findAllBySet("iko");
        logger.debug("All cards loaded.  There are {} cards to compare to",
                    cards.size());
        double max = Integer.MIN_VALUE;
        Card bestCard = null;
        logger.debug("Starting compare");
            for (Card card : cards) {
                double corr = new PearsonsCorrelation().correlation(testImage,
                                                                    card.getImgArray());
                if (corr > max) {
                    max = corr;
                    bestCard = card;
                }
            }
        logger.debug("Card is {} with a coorleation of {}.",
                    bestCard.getName(),
                    max);
        return bestCard;
    }

    @GetMapping(path = "/set/{set}")
    public @ResponseBody
    byte[] getSet(
            @PathVariable("set")
                    String set) {
        List<Card> cards = cardRepository.findAllBySet(set);
        for (Card card : cards) {
            getCard(card.getId());
        }
        return null;
    }

    @GetMapping(path = "/set")
    public @ResponseBody
    byte[] getSet(
            ArrayList<String> sets) {
        for (String set : sets) {
            logger.info("Getting images for set {}.",
                        set);
            getSet(set);
        }
        return null;
    }


    @GetMapping(path = "/color")
    public @ResponseBody
    byte[] getColor() {
        Map<Color, List<Card>> cardMapColor = cardRepository
                .findAll()
                .stream()
                .filter(c -> c.getColor() != null && c.getURI() != null)
                .collect(groupingBy(Card::getColor));
        String PATH = "./src/main/resources/img/colors/";
        Set<String> folders = cardMapColor.keySet().stream().map(s -> s.toString()).collect(Collectors.toSet());
        folders.add("LAND");
        folders.add("TOKEN");
        for (String add : folders) {
            String directoryName = PATH.concat(add);
            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        for (Color color : cardMapColor.keySet()) {
            if (color.equals(Color.GOLD) || color.equals(Color.COLORLESS)) {
                continue;
            }
            String folder = color.toString();
            String directoryName = PATH.concat(folder);

            ProgressBar pb = new ProgressBar(color.toString(),
                                             cardMapColor.get(color).size());
            for (Card card : cardMapColor.get(color)) {
                directoryName = PATH.concat(folder);
                if (card.getTypeLine().contains("Vanguard") || (card.getTypeLine().contains("Plane"))) {
                    continue;
                } else if (card.getTypeLine().contains("Token") || card.getTypeLine().contains("Emblem")) {
                    directoryName = PATH.concat("TOKEN");
                } else if (card.getTypeLine().contains("Land")) {
                    directoryName = PATH.concat("LAND");
                }
                pb.step();
                JSONObject json = JSONHelper.getJsonObject(jsonHelper.getRequest(card.getURI()));
                Image image = null;
                if (json.has("image_uris")) {
                    JSONObject map = (JSONObject) json.get("image_uris");
                    String urlString = (String) map.get("large");

                    try {
                        byte[] response = getImageFromURL(urlString);
                        saveImageToFile(directoryName,
                                        card.getId(),
                                        response);
                    } catch (IOException e) {
                    }
                }
            }
            pb.close();
        }

        return null;
    }

    /**
     * @param folder
     * @param file
     * @param image
     * @throws IOException
     */
    private void saveImageToFile(String folder, String file, byte[] image) throws IOException {
        logger.info("Folder: " + folder);
        logger.info("\tfile: " + file);
        String PATH = "";
        String directoryName = PATH.concat(folder);
        String fileName = file + ".jpg";

        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        FileOutputStream fos =
                new FileOutputStream(folder + "/" + fileName);
        logger.info(folder + "/" + fileName);
        fos.write(image);
        fos.close();
    }

    private byte[] getImageFromURL(String s) throws IOException {
        URL url = new URL(s);
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf,
                      0,
                      n);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }

    private double[] getArrayOfImage(String path) {
        OpenCV.loadLocally();
        Mat img = imread(path);
        return getArrayOfImage(img);

    }

    private double[] getArrayOfImage(Mat src) {
        OpenCV.loadLocally();
        Mat resizeimage = new Mat();
        Size scaleSize = new Size(250,
                                  250);
        resize(src,
               resizeimage,
               scaleSize,
               0,
               0,
               INTER_AREA);
        String temp = resizeimage.dump();
        String[] split = temp.split(";");
        StringBuilder sb = new StringBuilder();
        for (String t : split) {
            sb.append(t + ",");
        }
        temp = sb.toString();
        temp = temp
                .replace("[",
                         "")
                .replace("]",
                         "")
                .replace(" ",
                         "");
        return Arrays.stream(temp.split(",")).mapToDouble(Double::parseDouble).toArray();
    }
}
