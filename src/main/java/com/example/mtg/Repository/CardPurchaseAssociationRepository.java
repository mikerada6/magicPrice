package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.CardPurchaseAssociation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface CardPurchaseAssociationRepository extends CrudRepository<CardPurchaseAssociation, String> {

	List<CardPurchaseAssociation> findAll();
}
