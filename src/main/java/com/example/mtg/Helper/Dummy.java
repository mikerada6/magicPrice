package com.example.mtg.Helper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Dummy {

    public Dummy() {
    }

    public static Object readFromUrl(String _url) {
        // Connect to the URL using java's native library
        URL url = null;
        try {
            url = new URL(_url);

            URLConnection request = url.openConnection();
            request.connect();
			JSONParser jsonParser = new JSONParser(); //from gson

			Object root = jsonParser.parse(request.getContent().toString()); //Convert the input stream to a json element
		} catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
    }

    public static JSONObject readObjectFromFile(String file) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject object = (JSONObject) obj;
            return object;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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
