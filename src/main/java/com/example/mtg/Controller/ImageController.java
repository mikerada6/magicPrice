package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.ResourceNotFoundException;
import org.json.JSONObject;
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
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller @RequestMapping(path = "/images")
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
				+ "with id " + cardId));

		JSONObject json = JSONHelper.getJsonObject(jsonHelper.getRequest(card.getURI()));
		Image image = null;
		if(json.has("image_uris"))
		{
			JSONObject map = (JSONObject) json.get("image_uris");
			String urlString = "";
			if(map.has("png")) {
			urlString= (String) map.get("png");
			}
			else {
				urlString= (String) map.get("large");
			}

				try {
					byte[] response = getImageFromURL(urlString);
					saveImageToFile(card.getSet(),card.getId(),response);
					return response;
				} catch (IOException e) {
				}
			}
		int stop=0;
		return null;
	}

	@GetMapping(path = "/set/{set}")
	public @ResponseBody
	byte[] getSet(
			@PathVariable("set")
					String set) {
		List<Card> cards = cardRepository.findAllBySet(set);
		for(Card card: cards)
		{
			getCard(card.getId());
		}
		return null;
	}

	@GetMapping(path = "/set")
	public @ResponseBody
	byte[] getSet(
ArrayList<String> sets) {
		for(String set : sets)
		{
			logger.info("Getting images for set {}.", set);
			getSet(set);
		}
		return null;
	}

	/**
	 *
	 * @param folder
	 * @param file
	 * @param image
	 * @throws IOException
	 */
	private void saveImageToFile(String folder, String file, byte[] image) throws IOException {
		Random rand = new Random();
		String temp = "magicPriceborrowed_image.jpg";
		String PATH="";
		if(rand.nextDouble()<=.80) {
			PATH = "//Users//mradas341//IdeaProjects//pics//Training//";
		}
		else
		{
			PATH = "//Users//mradas341//IdeaProjects//pics//Validation//";
		}
		String directoryName = PATH.concat(folder);
		String fileName = file+".png";

		File directory = new File(directoryName);
		if (! directory.exists()){
			directory.mkdirs();
			// If you require it to make the entire directory path including parents,
			// use directory.mkdirs(); here instead.
		}
		FileOutputStream fos =
				new FileOutputStream(directoryName+"//"+fileName);
		fos.write(image);
		fos.close();
	}

	private byte[] getImageFromURL(String s) throws IOException {
		URL url = new URL(s);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1!=(n=in.read(buf)))
		{
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		return out.toByteArray();
	}
}
