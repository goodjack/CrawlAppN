package com.ssfashion.bigdata.crawler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/*import com.epopcon.wspider.common.data.Item;
import com.epopcon.wspider.common.logger.Logger;
import com.epopcon.wspider.common.logger.WSLogger;
import com.epopcon.wspider.common.logger.WSLogger.WSTYPE;
import com.epopcon.wspider.context.WspiderContext;
import com.epopcon.wspider.db.dflt.dto.fashion.ColtItem;
import com.epopcon.wspider.db.sscnt.dao.ProductDao;
import com.epopcon.wspider.db.sscnt.dto.Goods;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.CategoryMapper;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.AinItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.DarkVictoryItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.ImvelyItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.ItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.MixxMixItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.MusinsaItemConvertor;
import com.epopcon.wspider.taskJob.dbtransfer.sscnt.convertor.StyleNandaItemConvertor;*/

//@Component(value="cateUtil")
public class CategoryUtil {

	/*private static Logger logger = WSLogger.getLogger(WSTYPE.CORE);
	
	private ProductDao productDao;
	private CategoryMapper categoryMapper;

	private Map<String, Object> siteMappingCategories;
	private Map<String, Map<String, Object>> mappingCategories;
//	private WspiderContext context;

	private List<ItemConvertor> itemConvertors = new ArrayList<>();
	{
		itemConvertors.add(new StyleNandaItemConvertor());
		itemConvertors.add(new AinItemConvertor());
		itemConvertors.add(new ImvelyItemConvertor());
		itemConvertors.add(new MixxMixItemConvertor());
		itemConvertors.add(new DarkVictoryItemConvertor());
		itemConvertors.add(new MusinsaItemConvertor());
	}

	
	public CategoryUtil(WspiderContext context, String site) throws Exception {
		this.productDao = context.getBean(ProductDao.class);
		this.categoryMapper = new CategoryMapper(productDao);
		this.siteMappingCategories = categoryMapper.mapperInit(site);
//		this.mappingCategories = categoryMapper.getMappingCategories();
	}

	public Map<String, Object> getSiteMappingCategories() {
		return siteMappingCategories;
	}

	public Item mapCategories(Map<String, Object> param, Item item) {
		Goods goods = null;
		ColtItem coltItem = (ColtItem) item;

		for (ItemConvertor itemConvertor : itemConvertors) {
			if (itemConvertor.isSupported(coltItem)) {
				goods = itemConvertor.convertObject(coltItem);
				if (goods == null)
					break;

				String siteName = goods.getSite();
				String brandCode = goods.getBrandCode();

				categoryMapper.mappingCategory(siteName, brandCode, getCategoryNames(coltItem.getGoodsCate()), goods );

				break;
			}
		}

		if (goods == null) {
			return item;
		}

		System.out.println("================================!!!!1");
		System.out.println("goods=" + goods);
		System.out.println("================================!!!!2");
		
		
		return goods;
	}

	public List<List<String>> getCategoryNames(String goodsCate) {
		List<List<String>> list = new ArrayList<>();

		if (StringUtils.isNotBlank(goodsCate)) {
			String[] temp = goodsCate.split(";");
			for (String str : temp) {
				list.add(Arrays.asList(str.split(">")));
			}
		}
		return list;
	}*/
}
