package com.example.mtg.Repository;

import com.example.mtg.Magic.Price;
import org.springframework.data.repository.CrudRepository;

import java.sql.Date;
import java.util.List;
import java.util.Set;

public interface PriceRepository extends CrudRepository<Price, Long> {
	List<Price> findAll();

	Set<Price> findAllByCardId(String cardId);

	Set<Price> findAllByDate(Date date);

	void deleteByIdIn(List<Long> ids);
}
