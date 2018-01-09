package com.eopcon.crawler.samsungcnt.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.eopcon.crawler.samsungcnt.exception.BizException;
import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.Comment;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

@Component
public class Assertion {
	
	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	@Autowired
	protected ExceptionBuilder exceptionBuilder;

	/**
	 * 유효성 체크를 수행한다.
	 * 
	 * @param type
	 * @param productDetail
	 */
	public void assertNotEmpty(OnlineStoreConst type, ProductDetail productDetail) {
		try {
			switch(type){
			case UNIQLO :
				Assert.hasText(productDetail.getGoodsNum(), "[Assertion failed] - [GoodsNum] must have text; it must not be null, empty, or blank");
				Assert.hasText(productDetail.getGoodsName(), "[Assertion failed] - [GoodsName] must have text; it must not be null, empty, or blank");
				Assert.hasText(productDetail.getBrandCode(), "[Assertion failed] - [BrandCode] argument must have text; it must not be null, empty, or blank");
				Assert.notEmpty(productDetail.getStocks(), "[Assertion failed] - [Stocks] must not be empty: it must contain at least 1 element");
				break;
			default :
				Assert.hasText(productDetail.getGoodsNum(), "[Assertion failed] - [GoodsNum] must have text; it must not be null, empty, or blank");
				Assert.hasText(productDetail.getGoodsName(), "[Assertion failed] - [GoodsName] must have text; it must not be null, empty, or blank");
				Assert.hasText(productDetail.getGoodsImage(), "[Assertion failed] -[GoodsImage] must have text; it must not be null, empty, or blank");
				Assert.hasText(productDetail.getBrandCode(), "[Assertion failed] - [BrandCode] argument must have text; it must not be null, empty, or blank");
				Assert.notNull(productDetail.getPrice(), "[Assertion failed] - [Price] is required; it must not be null");
				Assert.notEmpty(productDetail.getStocks(), "[Assertion failed] - [Stocks] must not be empty: it must contain at least 1 element");
			}
			
			if(productDetail.getPrice() <= 0)
				throw new IllegalArgumentException("[Assertion failed] - [Price] must be greater than zero!!");
			
			for (Stock stock : productDetail.getStocks())
				assertNotEmpty(type, stock);
			for (Comment comment : productDetail.getComments())
				assertNotEmpty(type, comment);

		} catch (BizException e) {
			throw e;
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_FAIL, e);
		}
	}

	/**
	 * 유효성 체크를 수행한다.
	 * 
	 * @param type
	 * @param stock
	 */
	private void assertNotEmpty(OnlineStoreConst type, Stock stock) {
		try {
			Assert.hasText(stock.getColor(), "[Assertion failed] - [Color] must have text; it must not be null, empty, or blank");
			Assert.hasText(stock.getSize(), "[Assertion failed] - [Size] must have text; it must not be null, empty, or blank");
			
			int stockAmount = stock.getStockAmount();
			if(stockAmount > OnlineStoreConst.ERROR_STOCK_SIZE_LIMIT) {
				logger.warn("[Assertion warning] - [StockAmount] is too mouch inventory!! set zero!!");
				stock.setStockAmount(0);
			}
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_FAIL, e);
		}
	}

	/**
	 * 유효성 체크를 수행한다.
	 * 
	 * @param type
	 * @param comment
	 */
	public void assertNotEmpty(OnlineStoreConst type, Comment comment) { 
		try {
			Assert.hasLength(comment.getGoodsComment(), "[Assertion failed] - [GoodsComment] must have length; it must not be null or empty");
			Assert.notNull(comment.getGoodsRating(), "[Assertion failed] - [GoodsRating] is required; it must not be null");
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_FAIL, e);
		}
	}

	/**
	 * 유효성 체크를 수행한다.
	 * 
	 * @param type
	 * @param materials
	 */
	public void assertNotEmpty(OnlineStoreConst type, Materials materials) {
		try {
			Assert.hasText(materials.getColor(), "[Assertion failed] - [Color] must have text; it must not be null, empty, or blank");
			Assert.hasText(materials.getGoodsComposed(), "[Assertion failed] - [GoodsComposed] must have text; it must not be null, empty, or blank");
			Assert.hasText(materials.getMaterials(), "[Assertion failed] - [Materials] must have text; it must not be null, empty, or blank");
			Assert.notNull(materials.getRatio(), "[Assertion failed] - [Ratio] is required; it must not be null");
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_MATERIALS_FAIL, e);
		}
	}
}
