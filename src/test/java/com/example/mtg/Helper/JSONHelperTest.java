package com.example.mtg.Helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONHelperTest {

	private JSONHelper jsonHelperUnderTest;

	@Before
	public void setUp() {
		jsonHelperUnderTest = new JSONHelper();
	}

	@Test
	public void testGetRequest() throws JSONException {
		// Setup

		// Run the test
		final String result = jsonHelperUnderTest.getRequest("https://api.scryfall.com/cards");
		JSONObject json= null;
		try {
			json = new JSONObject(result);
		} catch (JSONException e) {
			Assert.fail("reason: " + e);
		}
		// Verify the results
		Assert.assertTrue(json.has("object"));
		Assert.assertEquals("list",json.get("object"));
	}
}
