package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Price;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.Repository.PriceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

public class PriceControllerTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	private PriceController priceController;
	@Mock
	private PriceRepository priceRepository;
	@Mock
	private JSONHelper jsonHelper;

	@Mock
	private CardRepository cardRepository;

	@Before
	public void setup() {
		priceController = new PriceController();
		ReflectionTestUtils.setField(priceController, "cardRepository", cardRepository);
		ReflectionTestUtils.setField(priceController, "priceRepository", priceRepository);
	}

	@Test
	public void cleanUpTest() {
		Card cardA = new Card();
		cardA.setId("cardA");
		Card cardB = new Card();
		cardB.setId("cardB");
		Price a = new Price();
		a.setDate(Date.valueOf("2020-02-22"));
		a.setCard(cardA);

		Price b = new Price();
		b.setDate(Date.valueOf("2020-02-22"));
		b.setCard(cardA);

		Price c = new Price();
		c.setDate(Date.valueOf("2020-02-23"));
		c.setCard(cardA);

		Price d = new Price();
		d.setDate(Date.valueOf("2020-02-23"));
		d.setCard(cardB);

		Price e = new Price();
		e.setDate(Date.valueOf("2020-02-22"));
		e.setCard(cardB);

		Price f = new Price();
		f.setDate(Date.valueOf("2020-02-23"));
		f.setCard(cardB);

		ArrayList<Price> prices = new ArrayList<>();
		prices.add(a);
		prices.add(b);
		prices.add(c);
		prices.add(d);
		prices.add(e);
		prices.add(f);

		when(priceRepository.findAll()).thenReturn(prices);

		List<Price> response = priceController.cleanup();
		Assert.assertNotNull(response);
		Assert.assertEquals("Trying to delete the wrong amount", 2, response.size());
		Mockito.verify(priceRepository, Mockito.times(1)).findAll();
		Mockito.verify(priceRepository, Mockito.times(1)).deleteAll(anyCollection());
	}

}
