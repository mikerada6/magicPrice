package com.example.mtg.Repository;

import com.example.mtg.WordPress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordPressRepository  extends CrudRepository<WordPress, Integer> {
    Optional<WordPress> findById(int id);
    List<WordPress> findAll();
}
