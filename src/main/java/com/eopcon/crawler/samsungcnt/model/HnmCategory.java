package com.eopcon.crawler.samsungcnt.model;

public class HnmCategory extends Category {
	private static final long serialVersionUID = 2479253325858445240L;
	private boolean endOfNode = false;

	public boolean isEndOfNode() {
		return endOfNode;
	}

	public void setEndOfNode(boolean endOfNode) {
		this.endOfNode = endOfNode;
	}
	
}
