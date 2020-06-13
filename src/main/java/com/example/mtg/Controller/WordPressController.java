package com.example.mtg.Controller;

import com.example.mtg.Repository.WordPressRepository;
import com.example.mtg.ResourceNotFoundException;
import com.example.mtg.WordPress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(path = "/posts")
public class WordPressController {

    @Autowired
    private WordPressRepository wordPressRepository;

    private static final Logger logger = LoggerFactory.getLogger(WordPressController.class);

    @GetMapping(path = "")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    List<WordPress> getAllWordPress() {
        logger.info("getting all");
        return wordPressRepository.findAll();
    }

    @PostMapping(path = "")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    WordPress getAllWordPress(WordPress wordPress) {
        return wordPressRepository.save(wordPress);
    }

    @GetMapping(path = "/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public @ResponseBody
    WordPress getWordPress(@PathVariable("id") int id) {
        logger.info("id: {}.",id);
        return wordPressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException());
    }

}
