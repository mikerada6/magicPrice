package com.example.mtg.Magic;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Format {
    //    @Id @GeneratedValue(generator="system-uuid")
//    @GenericGenerator(name="system-uuid",
//            strategy = "uuid")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String name;
    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "formats")

    private List<Card> cards;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        if (cards == null) {
            cards = new ArrayList<Card>();
        }
        cards.add(card);
    }
}
