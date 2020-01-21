package com.example.mtg.Controller;

import com.example.mtg.Magic.Card;
import com.example.mtg.Repository.CardRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class CardControllerTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	CardController cardController;
	@Mock
	private CardRepository cardRepository;

	@Before
	public void setup() {
		cardController = new CardController();
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
		card1.setId(cardId);
		card1.setName(name);

		ArrayList<Card> cards = new ArrayList<>();

		cards.add(card);
		cards.add(card1);

		when(cardRepository.findAll()).thenReturn(cards);

		List<Card> cardsReturn = cardController.getAllCards();

		Assert.assertEquals(cards, cardsReturn);

		Mockito.verify(cardRepository, Mockito.times(1)).findAll();
	}
}
