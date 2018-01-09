package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

@Alias("materials")
public class Materials implements Serializable {

	private static final long serialVersionUID = 4109554188311203937L;

	private Long goodsId; // 상품아이디
	private Long skuId; // SKU 아이디
	private String goodsComposed; // 상품구성(전체/겉감/안감...)
	private String color; // 색상
	private String materials; // 소재
	private Float ratio; // 비율

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

	public String getGoodsComposed() {
		return goodsComposed;
	}

	public void setGoodsComposed(String goodsComposed) {
		this.goodsComposed = goodsComposed;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}

	public Float getRatio() {
		return ratio;
	}

	public void setRatio(Float ratio) {
		this.ratio = ratio;
	}
	
	public Materials copy(){
		Materials ma = new Materials();
		
		ma.setGoodsId(goodsId);
		ma.setColor(color);
		ma.setGoodsComposed(goodsComposed);
		ma.setMaterials(materials);
		ma.setRatio(ratio);
		
		return ma;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
