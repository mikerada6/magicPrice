package com.example.mtg.Helper;

public class Helper {

	private long transactionId;
	private String cardId;
	private boolean isFoil;
	private int count;

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public boolean isFoil() {
		return isFoil;
	}

	public void setFoil(boolean foil) {
		isFoil = foil;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
