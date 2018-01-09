package com.eopcon.crawler.samsungcnt.service.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.eopcon.crawler.samsungcnt.model.Comment;
import com.eopcon.crawler.samsungcnt.model.Goods;
import com.eopcon.crawler.samsungcnt.model.LogDetail;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.NaverKeyword;
import com.eopcon.crawler.samsungcnt.model.Sku;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.ssfashion.bigdata.crawler.file.rawdata.WriteJsonObjectLocal;

@Repository
@Transactional(value = "dataSourceTransactionManager", propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class ProductDao extends SqlSessionDaoSupport {

	/**
	 * 맵핑 카테고리 정보를 반환한다.
	 * 
	 * @param site
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public Map<String, Object> getMappingCategories(String site) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);

		List<Map<String, Object>> list = getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectCateStd", param);

		Map<String, Object> result = new HashMap<>();

		for (Map<String, Object> map : list) {
			List<String> categories = new ArrayList<>();

			String cate1 = (String) map.get("cate1");
			String cate2 = (String) map.get("cate2");
			String cate3 = (String) map.get("cate3");
			String cate4 = (String) map.get("cate4");
			String stdCate1 = (String) map.get("stdCate1");
			String stdCate2 = (String) map.get("stdCate2");
			String stdCate3 = (String) map.get("stdCate3");
			String stdCate4 = (String) map.get("stdCate4");

			if (StringUtils.isNotEmpty(cate1))
				categories.add(cate1);
			if (StringUtils.isNotEmpty(cate2))
				categories.add(cate2);
			if (StringUtils.isNotEmpty(cate3))
				categories.add(cate3);
			if (StringUtils.isNotEmpty(cate4))
				categories.add(cate4);

			String[] values = new String[] { stdCate1, stdCate2, stdCate3, stdCate4 };
			addMap(result, categories, values);
		}
		return result;
	}

	/**
	 * 맵핑 카테고리 정보를 반환한다. (기타 카테고리만)
	 * 
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public Map<String, Object> getEtcMappingCategories() {
		List<Map<String, Object>> list = getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectEtcMappingCategories");

		Map<String, Object> result = new HashMap<>();

		for (Map<String, Object> map : list) {
			List<String> categories = new ArrayList<>();

			String stdCate1 = (String) map.get("stdCate1");
			String stdCate2 = (String) map.get("stdCate2");
			String stdCate3 = (String) map.get("stdCate3");
			String stdCate4 = (String) map.get("stdCate4");
			int type = (Integer) map.get("type");

			categories.add(String.valueOf(type));

			switch (type) {
			case 1:
				if (StringUtils.isNotEmpty(stdCate1))
					categories.add(stdCate1);
				break;
			case 2:
				if (StringUtils.isNotEmpty(stdCate1))
					categories.add(stdCate1);
				if (StringUtils.isNotEmpty(stdCate2))
					categories.add(stdCate2);
				break;
			default:
				if (StringUtils.isNotEmpty(stdCate1))
					categories.add(stdCate1);
				if (StringUtils.isNotEmpty(stdCate2))
					categories.add(stdCate2);
				if (StringUtils.isNotEmpty(stdCate3))
					categories.add(stdCate3);
			}

			String[] values = new String[] { stdCate1, stdCate2, stdCate3, stdCate4 };
			addMap(result, categories, values);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void addMap(Map<String, Object> result, List<String> categories, String[] values) {
		Map<String, Object> parent = result;
		Map<String, Object> child = null;

		for (int i = 0; i < categories.size(); i++) {
			String category = categories.get(i);
			Object object = parent.get("_child=" + category);

			parent.put("_name=" + category, values[i]);

			if (i == categories.size() - 1) {
				parent.put("_value=" + category, values);
				break;
			}

			if (object == null) {
				child = new HashMap<>();
				child.put("_parent", parent);
				parent.put("_child=" + category, child);
			} else {
				child = (Map<String, Object>) object;
			}
			parent = child;
		}
	}

	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public List<String> getIgnoreMappingCategories(String site) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);
		return getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectIgnoreMappingCategories", param);
	}

	/**
	 * 온라인 아이템 로그 정보를 반환한다.
	 * 
	 * @param site
	 * @param onlineGoodsNum
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public LogDetail getGoodsLog(String site, String onlineGoodsNum) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);
		param.put("onlineGoodsNum", onlineGoodsNum);

		return getSqlSession().selectOne("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectGoodsLog", param);
	}

	/**
	 * 마스터 테이블에 반영이 안된 온라인 아이템 로그 목록을 가져온다.
	 * 
	 * @param jobExecutionId
	 * @param site
	 * @param collectDay
	 * @param id
	 * @param pageSize
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public List<LogDetail> getNotAppliedGoodsLogs(Long jobExecutionId, String site, String collectDay, long id, int pageSize) {
		Map<String, Object> param = new HashMap<>();

		param.put("jobExecutionId", jobExecutionId);
		param.put("site", site);
		param.put("collectDay", collectDay);
		param.put("id", id);
		param.put("pageSize", pageSize);

		return getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectNotAppliedGoodsLogs", param);
	}

	/**
	 * 실패한 온라인 아이템 로그 건수를 반환한다.
	 * 
	 * @param site
	 * @param collectDay
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public Long getGoodsLogFailCount(String site, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);
		param.put("collectDay", collectDay);

		return getSqlSession().selectOne("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectGoodsLogFailCount", param);
	}

	/**
	 * GOODS_ID를 반환한다.
	 * 
	 * @param site
	 * @param brandCode
	 * @param goodsNum
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public Long getGoodsId(String site, String brandCode, String goodsNum) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);
		param.put("brandCode", brandCode);
		param.put("goodsNum", goodsNum);

		return getSqlSession().selectOne("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectGoodsId", param);
	}

	/**
	 * 판매종료를 업데이트를 위한 상품데이터 목록정보를 반환한다.
	 * 
	 * @param site
	 * @param collectDay
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public List<Map<String, Object>> getClosedGoods(String site, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("site", site);
		param.put("collectDay", collectDay);

		return getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectClosedGoods", param);
	}

	/**
	 * SKU_ID를 반환한다.
	 * 
	 * @param goodsId
	 * @param skuNum
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public Long getSkuId(Long goodsId, String skuNum) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsId", goodsId);
		param.put("skuNum", skuNum);

		return getSqlSession().selectOne("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectSkuId", param);
	}

	/**
	 * 재고 COLLECT_DAY 목록정보를 반환한다.
	 * 
	 * @param skuId
	 * @param collectDay
	 * @return
	 */
	@Transactional(value = "dataSourceTransactionManager", readOnly = true)
	public List<Map<String, String>> getStockHisCollectDays(Long skuId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);

		return getSqlSession().selectList("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.selectStockHisCollectDays", param);
	}

	/**
	 * 온라인 아이템 로그 정보를 변경한다.
	 * 
	 * @param jobExecutionId
	 * @param site
	 * @param onlineGoodsNum
	 * @param collectDay
	 * @param backupFile
	 */
	public void updateGoodsLogBackup(Long jobExecutionId, String site, String onlineGoodsNum, String collectDay, File backupFile) {
		LogDetail logDetail = getGoodsLog(site, onlineGoodsNum);

		if (logDetail == null)
			logDetail = new LogDetail();

		logDetail.setSite(site);
		logDetail.setOnlineGoodsNum(onlineGoodsNum);
		logDetail.setVal9(backupFile.getAbsolutePath());
		logDetail.setVal10(null);
		logDetail.setLastCollectDay(collectDay);
		logDetail.setErrorStep(OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL);
		logDetail.setErrorMessage(null);
		logDetail.setAppliedYn(false);
		logDetail.setJobExecutionId(jobExecutionId);

		Long id = logDetail.getId();

		if (id == null)
			getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoodsLog", logDetail);
		else
			getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateGoodsLog", logDetail);
	}

	/**
	 * 온라인 아이템 정보를 등록 및 변경 한다.
	 * 
	 * @param collectDay
	 * @param goods
	 */
	public void mergeGoods(String collectDay, Goods goods) {
		String site = goods.getSite();
		String brandCode = goods.getBrandCode();
		String goodsNum = goods.getGoodsNum();

		Long goodsId = null;
		Long skuId = null;

		goodsId = getGoodsId(site, brandCode, goodsNum);
		goods.setId(goodsId);

		// 상품정보(마스터)
		if (goodsId == null)
			insertGoods(goods);
		else
			updateGoods(goods);

		goodsId = goods.getId();

		// 상품평정보
		List<Comment> comments = goods.getComments();

		deleteGoodComment(goodsId);
		for (Comment comment : comments) {
			comment.setGoodsId(goodsId);
			insertGoodComment(comment);
		}

		// 상품가격변경이력
		deleteGoodsPriceHis(goodsId, collectDay);

		Integer price = goods.getPrice();
		insertGoodsPriceHis(goodsId, collectDay, price);

		// SKU정보
		for (Sku sku : goods.getSku()) {

			String skuNum = sku.getSkuNum();
			boolean collected = sku.isCollected();

			sku.setGoodsId(goodsId);

			skuId = getSkuId(goodsId, skuNum);
			
			sku.setId(skuId);

			if (collected) {
				if (skuId == null)
					insertSkuInfo(sku);
				else
					updateSkuInfo(sku);
				skuId = sku.getId();
			}

			if (collected) {
				// 재고정보 이력
				List<Stock> stocks = sku.getStocks();

				// deleteStockHis(goodsId, collectDay);
				for (Stock stock : stocks) {
					stock.setGoodsId(goodsId);
					stock.setSkuId(skuId);
					stock.setCollectDay(collectDay);
					mergeStockHis(stock);
				}

				// 재고 판매정보 업데이트
				List<Map<String, String>> list = getStockHisCollectDays(skuId, collectDay);
				for (Map<String, String> map : list) {
					String color = map.get("color");
					String size = map.get("size");
					String currentDay = map.get("collectDay");
					String lastDay = map.get("lastCollectDay");

					if (StringUtils.isNotEmpty(lastDay))
						updateStockHisSellAmount(skuId, color, size, currentDay, lastDay);
				}

				// 할인정보이력
				if (sku.isDiscounted()) {
					deleteSkuDiscountHis(skuId, collectDay);

					Integer discountPrice = sku.getDiscountPrice();
					Float discountRate = sku.getDiscountRate();

					insertSkuDiscountHis(skuId, collectDay, discountPrice, discountRate);
				}
				// 베스트아이템이력
				deleteBestItemHis(skuId, collectDay);

				Boolean bestItem = sku.isBestItem();
				insertBestItemHis(skuId, collectDay, bestItem);
			}

			// 소재정보
			if (skuId != null) {
				List<Materials> materials = sku.getMaterials();
				deleteSkuMaterials(skuId);

				for (Materials m : materials) {
					m.setGoodsId(goodsId);
					m.setSkuId(skuId);

					insertSkuMaterials(m);
				}
			}
		}
	}
	public void mergeGoods_new(String collectDay, Goods goods,String localJsonPath) {
/*		
		Map<String,Object> map = goods.getColtItemMap();
		
		WriteJsonObjectLocal wjol = new WriteJsonObjectLocal(goods.getBrandCode(),goods.getSite(),localJsonPath);
		wjol.writeJsonObject(map);*/
	}
//	public void mergeData(Goods goods)
//	{
//		Map<String,Object> dataStandardList = new HashMap();
//		
//		DataStandard dataStandard = new DataStandard();
//		
//		dataStandard.setBestYn("");
//		dataStandard.setBrandName("");
//		dataStandard.setCategoryCode("");
//		dataStandard.setCrawlDate("");
//		dataStandard.setCategoryUrl("");
//		dataStandard.setDependentData("");
//		dataStandard.setDesc("");
//		dataStandard.setGoodEval("");
//		dataStandard.setGrade("");
//		dataStandard.setImageUrl("");
//		dataStandard.setLayer1("");
//		dataStandard.setLayer2("");
//		dataStandard.setLayer3("");
//		dataStandard.setLayer4("");
//		dataStandard.setLayer5("");
//		dataStandard.setLayer6("");
//		dataStandard.setMaterial("");
//		dataStandard.setNewYn("");
//		dataStandard.setNormalPrice("");
//		dataStandard.setOrigin("");
//		dataStandard.setProductCode("");
//		dataStandard.setProductColor("");
//		dataStandard.setProductSalePrice("");
//		dataStandard.setProductSize("");
//		dataStandard.setPruductName("");
//		dataStandard.setS3ImageUrl("");
//		dataStandard.setSaleYn("");
//		dataStandard.setStockInfo("");		
//		
//		dataStandardList.put("dataStandard",dataStandard);
//		
//	}
	public void insertGoodsLog(LogDetail logDetail) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoodsLog", logDetail);
	}

	public void updateGoodsLog(LogDetail logDetail) {
		getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateGoodsLog", logDetail);
	}

	public void updateGoodsCloseDay(Long id, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("id", id);
		param.put("collectDay", collectDay);

		getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateGoodsCloseDay", param);
	}

	public void mergeGoodsLogMapping(Long goodsLogId, Long goodsId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsLogId", goodsLogId);
		param.put("goodsId", goodsId);
		param.put("lastCollectDay", collectDay);

		int count = getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateGoodsLogMapping", param);
		if (count == 0)
			getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoodsLogMapping", param);
	}

	public void insertGoods(Goods goods) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoods", goods);
	}

	public void updateGoods(Goods goods) {
		getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateGoods", goods);
	}

	public void insertSkuInfo(Sku sku) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertSkuInfo", sku);
	}

	public void updateSkuInfo(Sku sku) {
		getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateSkuInfo", sku);
	}

	public void insertGoodsPriceHis(Long goodsId, String collectDay, Integer price) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsId", goodsId);
		param.put("collectDay", collectDay);
		param.put("price", price);

		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoodsPriceHis", param);
	}

	public void deleteGoodsPriceHis(Long goodsId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsId", goodsId);
		param.put("collectDay", collectDay);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteGoodsPriceHis", param);
	}

	public void insertBestItemHis(Long skuId, String collectDay, boolean bestItemYn) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);
		param.put("bestItemYn", bestItemYn);

		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertBestItemHis", param);
	}

	public void deleteBestItemHis(Long skuId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteBestItemHis", param);
	}

	public void insertGoodComment(Comment comment) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertGoodComment", comment);
	}

	public void deleteGoodComment(Long goodsId) {
		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteGoodComment", goodsId);
	}

	public void insertSkuDiscountHis(Long skuId, String collectDay, Integer discountPrice, Float discountRate) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);
		param.put("discountPrice", discountPrice);
		param.put("discountRate", discountRate);

		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertSkuDiscountHis", param);
	}

	public void deleteSkuDiscountHis(Long skuId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteSkuDiscountHis", param);
	}

	public void insertSkuMaterials(Materials materials) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertSkuMaterials", materials);
	}

	public void deleteSkuMaterials(Long skuId) {
		Map<String, Object> param = new HashMap<>();
		param.put("skuId", skuId);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteSkuMaterials", param);
	}

	public void mergeStockHis(Stock stock) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", stock.getSkuId());
		param.put("color", stock.getColor());
		param.put("goodsSize", stock.getSize());

		int count = getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateStockHis", stock);
		if (count == 0)
			getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertStockHis", stock);
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertColorSize", param);
	}

	public void deleteStockHis(Long skuId, String collectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("collectDay", collectDay);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteStockHis", param);
	}

	public void updateStockHisSellAmount(Long skuId, String color, String size, String collectDay, String lastCollectDay) {
		Map<String, Object> param = new HashMap<>();

		param.put("skuId", skuId);
		param.put("color", color);
		param.put("goodsSize", size);
		param.put("collectDay", collectDay);
		param.put("lastCollectDay", lastCollectDay);

		getSqlSession().update("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.updateStockHisSellAmount", param);
	}

	public void mergeCateMappingFail(Long goodsId, String cate1, String cate2, String cate3, String cate4) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsId", goodsId);
		param.put("cate1", cate1);
		param.put("cate2", cate2);
		param.put("cate3", cate3);
		param.put("cate4", cate4);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteCateMappingFail", param);
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertCateMappingFail", param);
	}

	public void deleteCateMappingFail(Long goodsId) {
		Map<String, Object> param = new HashMap<>();

		param.put("goodsId", goodsId);

		getSqlSession().delete("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.deleteCateMappingFail", param);
	}

	public void insertSearchKeyword(NaverKeyword naverkeyword) {
		getSqlSession().insert("com.eopcon.crawler.samsungcnt.service.dao.ProductDao.insertSearchKeyword", naverkeyword);
	}
}
