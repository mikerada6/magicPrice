package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository public interface CardRepository extends CrudRepository<Card, String> {
	List<Card> findAll();

	Set<Card> findAllById(String id);
}
