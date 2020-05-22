package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import com.example.mtg.domain.DomainUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CardControllerTest {

	@Mock
	private CardRepository mockCardRepository;
	@Mock
	private JSONHelper mockJsonHelper;

	@InjectMocks
	private CardController cardControllerUnderTest;

	@Before
	public void setUp() {
		initMocks(this);
	}

	@Test
	public void testGetCard() {
		Card c = DomainUtils.getTestObject(Card.class);
		// Setup
		when(mockCardRepository.findById(c.getId())).thenReturn(Optional.of(c));

		// Run the test
		final Card result = cardControllerUnderTest.getCard(c.getId());

		// Verify the results
		Assert.assertEquals(c.getId(), result.getId());
		Assert.assertEquals(c.getName(), result.getName());
		Assert.assertEquals(c.getColor(), result.getColor());
		Assert.assertEquals(c.getSet(), result.getSet());

	}

	@Test
	public void testDeleteCard() {
		// Setup

		// Run the test
		cardControllerUnderTest.deleteCard("cardId");

		// Verify the results
		verify(mockCardRepository).deleteById("cardId");
	}

	@Test
	public void testAddCard() {
		// Setup
		when(mockJsonHelper.getRequest("url")).thenReturn("result");

		// Run the test
		final Card result = cardControllerUnderTest.addCard("cardId");

		// Verify the results
	}

	@Test
	public void testGetAllCards() {
		// Setup
		when(mockCardRepository.findAll()).thenReturn(Arrays.asList(DomainUtils.getTestObject(Card.class)));

		// Run the test
		final List<Card> result = cardControllerUnderTest.getAllCards();

		// Verify the results
		Assert.assertEquals(1,result.size());
	}


	@Test
	public void testUpdate() {
		//TODO this test needs a total refactor
		// Setup
//		when(mockJsonHelper.getRequest("url")).thenReturn("result");
//		when(mockCardRepository.saveAll(Arrays.asList())).thenReturn(Arrays.asList(new Card()));
//
//		// Run the test
//		final ArrayList<Card> result = cardControllerUnderTest.update();
//
//		// Verify the results
	}

}
