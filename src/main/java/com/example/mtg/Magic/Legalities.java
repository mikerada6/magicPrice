package com.example.mtg.Magic;

public class Legalities {

	private boolean standard;
	private boolean future;
	private boolean historic;
	private boolean pioneer;
	private boolean modern;
	private boolean legacy;
	private boolean pauper;
	private boolean vintage;
	private boolean penny;
	private boolean commander;
	private boolean brawl;
	private boolean duel;
	private boolean oldschool;

	public boolean isStandard() {
		return standard;
	}

	public void setStandard(boolean standard) {
		this.standard = standard;
	}

	public boolean isFuture() {
		return future;
	}

	public void setFuture(boolean future) {
		this.future = future;
	}

	public boolean isHistoric() {
		return historic;
	}

	public void setHistoric(boolean historic) {
		this.historic = historic;
	}

	public boolean isPioneer() {
		return pioneer;
	}

	public void setPioneer(boolean pioneer) {
		this.pioneer = pioneer;
	}

	public boolean isModern() {
		return modern;
	}

	public void setModern(boolean modern) {
		this.modern = modern;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}

	public boolean isPauper() {
		return pauper;
	}

	public void setPauper(boolean pauper) {
		this.pauper = pauper;
	}

	public boolean isVintage() {
		return vintage;
	}

	public void setVintage(boolean vintage) {
		this.vintage = vintage;
	}

	public boolean isPenny() {
		return penny;
	}

	public void setPenny(boolean penny) {
		this.penny = penny;
	}

	public boolean isCommander() {
		return commander;
	}

	public void setCommander(boolean commander) {
		this.commander = commander;
	}

	public boolean isBrawl() {
		return brawl;
	}

	public void setBrawl(boolean brawl) {
		this.brawl = brawl;
	}

	public boolean isDuel() {
		return duel;
	}

	public void setDuel(boolean duel) {
		this.duel = duel;
	}

	public boolean isOldschool() {
		return oldschool;
	}

	public void setOldschool(boolean oldschool) {
		this.oldschool = oldschool;
	}

	public void setLegality(String format, String isLegal) {
		boolean legal = isLegal.equals("legal");
		if (format.equalsIgnoreCase("standard")) {
			standard = legal;
		} else if (format.equalsIgnoreCase("future")) {
			future = legal;
		} else if (format.equalsIgnoreCase("historic")) {
			historic = legal;
		} else if (format.equalsIgnoreCase("pioneer")) {
			pioneer = legal;
		} else if (format.equalsIgnoreCase("modern")) {
			modern = legal;
		} else if (format.equalsIgnoreCase("legacy")) {
			legacy = legal;
		} else if (format.equalsIgnoreCase("pauper")) {
			pauper = legal;
		} else if (format.equalsIgnoreCase("vintage")) {
			vintage = legal;
		} else if (format.equalsIgnoreCase("penny")) {
			penny = legal;
		} else if (format.equalsIgnoreCase("commander")) {
			commander = legal;
		} else if (format.equalsIgnoreCase("brawl")) {
			brawl = legal;
		} else if (format.equalsIgnoreCase("duel")) {
			duel = legal;
		} else if (format.equalsIgnoreCase("oldschool")) {
			oldschool = legal;
		}
	}
}
