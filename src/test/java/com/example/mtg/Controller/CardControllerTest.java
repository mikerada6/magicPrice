package com.example.mtg.Controller;

import com.example.mtg.Helper.JSONHelper;
import com.example.mtg.Helper.Utils;
import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class) public class CardControllerTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	CardController cardController;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private JSONHelper jsonHelper;

	@Before
	public void setup() {
		cardController = new CardController();
		ReflectionTestUtils.setField(cardController, "jsonHelper", jsonHelper);
		ReflectionTestUtils.setField(cardController, "cardRepository", cardRepository);
	}

	@Test
	public void getCardTest() {
		Card card = new Card();
		String cardId = "id";
		String name = "name";
		card.setId(cardId);
		card.setName(name);
		when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

		Card returnCard = cardController.getCard(cardId);
		Assert.assertEquals("Cards did not match", card, returnCard);
		Mockito.verify(cardRepository, Mockito.times(1)).findById(cardId);
	}

	@Test
	public void getAllCards() {
		Card card = new Card();
		String cardId = "id";
		String name = "name";
		card.setId(cardId);
		card.setName(name);

		Card card1 = new Card();
		String cardId1 = "id1";
		String name1 = "name1";
		card1.setId(cardId1);
		card1.setName(name1);

		ArrayList<Card> cards = new ArrayList<>();

		cards.add(card);
		cards.add(card1);

		when(cardRepository.findAll()).thenReturn(cards);

		List<Card> cardsReturn = cardController.getAllCards();

		Assert.assertEquals(cards, cardsReturn);

		Mockito.verify(cardRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void addCardTest() {
		Card card = new Card();
		String cardId = "id";
		String name = "name";
		card.setId(cardId);
		card.setName(name);
		when(cardRepository.save(card)).thenReturn(card);
		cardController.addCard(card);
		Mockito.verify(cardRepository, Mockito.times(1)).save(card);
	}

	@Test
	public void countTest() {

		when(cardRepository.count()).thenReturn((long) 5);
		long count = cardController.count();
		Mockito.verify(cardRepository, Mockito.times(1)).count();
		Assert.assertEquals(5, count);
	}

	@Test
	public void updateTest() {
		String url = "https://api.scryfall.com/cards";
		String url2 = "https://api.scryfall.com/cards?page=2";
		when(jsonHelper.getRequest(url)).thenReturn(Utils.json1);
		when(jsonHelper.getRequest(url2)).thenReturn(Utils.json2);

		when(cardRepository.saveAll(any())).thenReturn(new ArrayList<Card>());

		cardController.update();
		Mockito.verify(jsonHelper, Mockito.times(1)).getRequest(url);
		Mockito.verify(jsonHelper, Mockito.times(1)).getRequest(url2);
		Mockito.verify(cardRepository, Mockito.times(1)).saveAll(any());
	}
}
