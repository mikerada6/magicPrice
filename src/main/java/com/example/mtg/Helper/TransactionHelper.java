package com.example.mtg.Helper;

import java.sql.Date;

public class TransactionHelper {

	private long vednorId;
	private Date date;
	private String description;
	private double cost;

	public long getVednorId() {
		return vednorId;
	}

	public void setVednorId(long vednorId) {
		this.vednorId = vednorId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
}
