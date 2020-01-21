package com.example.mtg.Helper;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

public class JSONHelperTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	CloseableHttpClient httpClient;
	JSONHelper jsonHelper;

	@Before
	public void setup() {
		jsonHelper = new JSONHelper();
		httpClient = HttpClients.createDefault();
		ReflectionTestUtils.setField(jsonHelper, "httpClient", httpClient);
	}

	@Test
	public void testOne() {
		Assert.assertEquals(1, 1);
		String one = "https://api.scryfall.com/cards?page=2";
		jsonHelper.getRequest(one);
	}
}
