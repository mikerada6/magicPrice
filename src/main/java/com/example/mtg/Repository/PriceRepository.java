package com.example.mtg.Repository;

import com.example.mtg.Magic.Price;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface PriceRepository extends CrudRepository<Price, Long> {

	Set<Price> findAllByCardId(String cardId);
}
