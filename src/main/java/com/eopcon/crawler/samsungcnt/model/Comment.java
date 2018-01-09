package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

import com.eopcon.crawler.samsungcnt.util.StringUtil;

@Alias("comment")
public class Comment implements Serializable {

	private static final long serialVersionUID = 8259851212046778678L;
	
	private Long goodsId; // 상품아이디
	private Float goodsRating; // 상품평점
	private String goodsComment; // 상품평

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public Float getGoodsRating() {
		return goodsRating;
	}

	public void setGoodsRating(Float goodsRating) {
		this.goodsRating = goodsRating;
	}

	public String getGoodsComment() {
		return goodsComment;
	}

	public void setGoodsComment(String goodsComment) {
        this.goodsComment = StringUtil.replaceSymbols(goodsComment);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
