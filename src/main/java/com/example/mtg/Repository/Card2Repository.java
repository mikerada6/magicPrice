package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Card2;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Card2Repository extends CrudRepository<Card2, String> {
    List<Card2> findAll();
}
