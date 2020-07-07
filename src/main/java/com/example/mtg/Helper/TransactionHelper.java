package com.example.mtg.Helper;

import com.example.mtg.Controller.InventoryController;
import com.example.mtg.Magic.CardPurchaseAssociation;
import com.example.mtg.Magic.Price;
import com.example.mtg.Magic.Transaction;
import com.example.mtg.Repository.PriceRepository;
import com.example.mtg.Repository.TransactionRepository;
import com.example.mtg.Repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TransactionHelper {

	@Autowired
	private PriceRepository priceRepository;
	@Autowired
	private TransactionRepository transactionRepository;

	private Transaction transaction;

	public void setTransaction(Transaction transaction){
		this.transaction=transaction;
	}

	private static final Logger logger = LoggerFactory.getLogger(TransactionHelper.class);

	public double getCurrentValue()
	{
		double totalValue=0.0;
		Collection<CardPurchaseAssociation> cpas = transaction.getCardPurchaseAssociation();
		for(CardPurchaseAssociation cpa : cpas)
		{
			List<Price> prices = priceRepository
					.findAllByCardId(cpa.getCardId())
					.stream()
					.sorted(Comparator.comparing(Price::getDate, Comparator.reverseOrder()))
					.collect(Collectors.toList());
			if(prices.size()>0) {
				Price price = prices.get(0);
				totalValue+= cpa.isFoil() ? price.getUsd_foil() : price.getUsd();
			}

		}
		return totalValue;
	}

	public double getCurrentValueAtDate(Date date)
	{
		double totalValue=0.0;
		Collection<CardPurchaseAssociation> cpas = transaction.getCardPurchaseAssociation();
		for(CardPurchaseAssociation cpa : cpas)
		{
			Price price = priceRepository.findByDateAndAndCard(date,cpa.getCard()).orElse(null);
			if(price!=null) {
				try {
					totalValue += cpa.isFoil() ? price.getUsd_foil() : price.getUsd();
				}catch(Exception e)
				{
					logger.error("An error getting price for {} on date {}.", cpa.getCardId(), date);
				}
			}

		}
		return totalValue;
	}

}
