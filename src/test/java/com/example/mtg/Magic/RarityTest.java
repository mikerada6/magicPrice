package com.example.mtg.Magic;

import org.junit.Assert;
import org.junit.Test;

public class RarityTest {
	@Test
	public void testStringConverstion() {
		Assert.assertEquals(Rarity.RARE, Rarity.fromLabel("rare"));
		Assert.assertEquals(Rarity.RARE, Rarity.fromLabel("RARE"));
		Assert.assertEquals(Rarity.COMMON, Rarity.fromLabel("COMMON"));
		Assert.assertEquals(Rarity.UNCOMMON, Rarity.fromLabel("unCOMMON"));
		Assert.assertEquals(Rarity.MYTHIC, Rarity.fromLabel("MYTHIC"));
		Assert.assertEquals(null, Rarity.fromLabel("something"));
		Assert.assertEquals(null, Rarity.fromLabel(""));
		Assert.assertEquals(null, Rarity.fromLabel(null));
	}
}
