package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<Card, String> {
    List<Card> findAll();

    Optional<Card> findById(String id);

    List<Card> findAllBySet(String set);

    @Query(value = "SELECT DISTINCT set_name FROM card", nativeQuery = true)
    List<String> findAllSets();
}
