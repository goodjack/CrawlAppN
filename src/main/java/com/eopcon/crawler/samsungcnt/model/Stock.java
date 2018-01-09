package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

@Alias("stock")
public class Stock implements Serializable {

	private static final long serialVersionUID = 6157930343510156172L;

	private Long goodsId; // 상품아이디
	private Long skuId; // SKU 아이디
	private String collectDay; // 수집일
	private String color; // 색상
	private String size; // 사이즈
	private int stockAmount; // 재고량
	private int openMarketStockAmount; // 오픈마켓 재고량

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public Long getSkuId() {
		return skuId;
	}

	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	public String getCollectDay() {
		return collectDay;
	}

	public void setCollectDay(String collectDay) {
		this.collectDay = collectDay;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getStockAmount() {
		return stockAmount;
	}

	public void setStockAmount(int stockAmount) {
		this.stockAmount = stockAmount;
	}

	public int getOpenMarketStockAmount() {
		return openMarketStockAmount;
	}

	public void setOpenMarketStockAmount(int openMarketStockAmount) {
		this.openMarketStockAmount = openMarketStockAmount;
	}

	public Stock copy() {
		Stock stock = new Stock();

		stock.setGoodsId(goodsId);
		stock.setColor(color);
		stock.setCollectDay(collectDay);
		stock.setSize(size);
		stock.setStockAmount(stockAmount);
		stock.setOpenMarketStockAmount(openMarketStockAmount);

		return stock;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
