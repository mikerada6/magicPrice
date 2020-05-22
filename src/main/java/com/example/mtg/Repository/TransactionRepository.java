package com.example.mtg.Repository;

import com.example.mtg.Magic.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface TransactionRepository  extends CrudRepository<Transaction, Long> {
	List<Transaction> findAll();
}
