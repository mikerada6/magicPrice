package com.example.mtg.Magic;

public class Change {
	Card card;
	Price today;
	Price yesterday;
	Price weekAgo;

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Price getToday() {
		return today;
	}

	public void setToday(Price today) {
		this.today = today;
	}

	public Price getYesterday() {
		return yesterday;
	}

	public void setYesterday(Price yesterday) {
		this.yesterday = yesterday;
	}

	public Price getWeekAgo() {
		return weekAgo;
	}

	public void setWeekAgo(Price weekAgo) {
		this.weekAgo = weekAgo;
	}

	public double getDailyChange() {
		if (this.getToday().getUsd() != null && this.getYesterday().getUsd() != null) {
			return this.getToday().getUsd() - this.getYesterday().getUsd();
		}
		return -666;

	}

	public double getWeeklyChange() {
		if (this.getToday().getUsd() != null && this.getWeekAgo().getUsd() != null) {
			return this.getToday().getUsd() - this.getWeekAgo().getUsd();
		}
		return -666;

	}

	public String toString() {
		String ans = "";
		ans += card.getId() + "\t";
		ans += card.getName() + "\t";
		ans += card.getSet_name() + "\t";
		ans += today.getDate() + "\t";
		ans += today.getUsd() + "\t";
		ans += yesterday.getUsd() + "\t";
		ans += weekAgo.getUsd() + "\t";
		return ans;
	}

}
