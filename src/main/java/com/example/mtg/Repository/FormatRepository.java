package com.example.mtg.Repository;

import com.example.mtg.Magic.Card;
import com.example.mtg.Magic.Format;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormatRepository extends CrudRepository<Format, String> {

    Optional<Format> findByName(String name);
}
