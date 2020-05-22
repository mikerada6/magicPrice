package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository public interface CardRepository extends CrudRepository<Card, String> {
	List<Card> findAll();

	Set<Card> findAllById(String id);

	Optional<Card> findById(String id);

	List<Card> findAllBySet(String set);
}
