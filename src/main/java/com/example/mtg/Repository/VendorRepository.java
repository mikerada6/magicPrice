package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Price;
import com.example.mtg.Magic.Vendor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface VendorRepository extends CrudRepository<Vendor, Long> {
	List<Vendor> findAll();
}
