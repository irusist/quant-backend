package com.irusist.quant.quantbackend;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhulx on 24/11/2017.
 */
//@SpringBootApplication
public class UqerLastDayApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UqerLastDayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UqerLastDayApplication.class, args);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 000001 起始时间太早
     * 000854 没有历史记录
     * 399416 没有历史记录
     * 399632 没有历史记录
     * 399634 没有历史记录
     * 399983 没有历史记录
     * 460220 没有历史记录
     * 930746 港股，暂时不处理港股
     * 930839 港股，暂时不处理港股
     * CSPSADRP 没有成分股
     * GDAXI 没有成分股
     * H11136 美股，暂时不处理
     * H30251 没有历史记录
     * H30252 没有历史记录
     * H30255 没有历史记录
     * H30257 没有历史记录
     * H30359 没有历史记录
     * H30373 没有历史记录
     * H30533 美股，暂时不处理
     * H30537 没有历史记录
     * H50069 港股，暂时不处理
     * HSCEI  没有历史记录
     * HSI  没有历史记录
     * HSML25 没有历史记录
     * NBI 没有历史记录
     * NDX 没有历史记录
     * SPGOGUP 没有历史记录
     * SPX 没有历史记录
     *
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {
        // 获取最近一天的交易日
        String lastBizDate = jdbcTemplate.queryForObject("select biz_date from uqer_stock_hs where code = '000001' order by biz_date desc limit 1", String.class);
        log.info(String.format("last biz_date: %s", lastBizDate));
        List<String> excludeIndexList = Lists.newArrayList("000001", "000854", "399416", "399632", "399634", "399983", "460220",
                "930746", "930839", "CSPSADRP", "GDAXI", "H11136", "H30251", "H30252", "H30255", "H30257", "H30359", "H30373",
                "H30533", "H30537", "H50069", "HSCEI", "HSI", "HSML25", "NBI", "NDX", "SPGOGUP", "SPX");
        // 获取所有有基金的指数
        List<String> indexCodeList = jdbcTemplate.query("SELECT index_code from index_fund group by index_code order by index_code",
                (rs, rowNum) -> rs.getString("index_code"));
        // 去掉暂时不处理的指数
        indexCodeList.removeAll(excludeIndexList);

        // 遍历所有指数
        for (String indexCode : indexCodeList) {
            log.info(String.format("preparing to execute: %s", indexCode));
            // 查询当前的成分股
            List<IndexConstituent> constituentList = jdbcTemplate.query("select stock_code, status from index_constituent_current where index_code = ?",
                    new Object[]{indexCode}, (rs, rowNum) -> new IndexConstituent(lastBizDate, rs.getString("stock_code"), 1));
            log.info(String.format("constituent size is: %s", constituentList.size()));
            Valuation valuation = getValuationOneDay(indexCode, lastBizDate, constituentList);
            // 插入数据
            jdbcTemplate.update("INSERT INTO index_valuation_uqer(index_code, biz_date, pe, pb) VALUES (?, ?, ?, ?)", new Object[]{valuation.getIndexCode(), valuation.getBizDate(), valuation.getPe(), valuation.getPb()});
        }
    }

    private Valuation getValuationOneDay(String indexCode, String bizDate, List<IndexConstituent> constituentList) {
        StringBuilder stockList = new StringBuilder("(");
        Joiner.on(",").appendTo(stockList, Lists.transform(constituentList, new Function<IndexConstituent, String>() {
            @Override
            public String apply(IndexConstituent indexConstituent) {
                String code = indexConstituent.getStockCode();
                int index = code.indexOf(".");
                if (index != -1) {
                    return "'" + code.substring(0, index) + "'";
                }
                return "'" + code + "'";            }
        }));
        stockList.append(")");

        // 查询pe， pb
        String sql = "SELECT biz_date, code, pe, pb from uqer_stock_hs where biz_date = ? and code in " + stockList.toString();
        List<Valuation> valuations = jdbcTemplate.query(sql, new Object[]{bizDate},
                (rs, rowNum) -> new Valuation(rs.getString("biz_date"), null, rs.getString("code"), rs.getDouble("pe"), rs.getDouble("pb")));
        Double peCal = valuations.stream().filter(valuation -> valuation.getPe() > 0).mapToDouble(valuation -> 1 / valuation.getPe()).sum();
        Double pbCal = valuations.stream().filter(valuation -> valuation.getPb() > 0).mapToDouble(valuation -> 1 / valuation.getPb()).sum();
        return new Valuation(bizDate, indexCode, null, valuations.size() / peCal, valuations.size() / pbCal);
    }
}
