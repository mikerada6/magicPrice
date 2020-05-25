package com.example.mtg.Helper;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Dummy {

	public Dummy() {
	}

	public static JSONArray readFromFile(String file) {
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(file)) {
			//Read JSON file
			Object obj = jsonParser.parse(reader);

			JSONArray array = (JSONArray) obj;
			return array;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
