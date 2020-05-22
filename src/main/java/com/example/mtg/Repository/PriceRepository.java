package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Price;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository public interface PriceRepository extends CrudRepository<Price, Long> {
	List<Price> findAll();

	Set<Price> findAllByCardId(String cardId);

	Set<Price> findAllByDate(Date date);

	Optional<Price> findByDateAndAndCard(Date date, Card card);

	void deleteByIdIn(List<Long> ids);
}
