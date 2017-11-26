package com.irusist.quant.quantbackend;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhulx on 24/11/2017.
 */
//@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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

        // 获取所有交易日
        List<String> bizDateList = jdbcTemplate.query("SELECT biz_date from stock_hs where code = '000001.SZ' order by biz_date", new Object[0],
                (rs, rowNum) -> rs.getString("biz_date"));
        System.out.println(bizDateList);
        int bizDateSize = bizDateList.size();
        // 获取所有有基金的指数
        List<String> indexCodeList = jdbcTemplate.query("SELECT index_code from index_fund where index_code > 'H50069' group by index_code order by index_code", new Object[0],
                (rs, rowNum) -> rs.getString("index_code"));
        for (String indexCode : indexCodeList) {
            log.info("preparing to execute: " + indexCode);
            String startDate = jdbcTemplate.queryForObject("SELECT biz_date from index_constituent_history where index_code = ? order by biz_date limit 1", new Object[]{indexCode}, String.class);
            log.info("start date: " + startDate);
            int index = bizDateList.indexOf(startDate);
            // 如果指数成立时间小于股票的最早日期， 则从股票最早日期开始
            if (index == -1) {
                // TODO 暂时先把这部分数据过滤
                log.info("index: " + indexCode + " pass");
                continue;
            }
            List<String> indexDateList = bizDateList.subList(index + 1, bizDateSize);
            // 获取指数第一天的成分股
            List<IndexConstituent> constituentList = jdbcTemplate.query("SELECT * from index_constituent_history where index_code = ? and biz_date = ?", new Object[]{indexCode, startDate},
                    (rs, rowNum) -> new IndexConstituent(rs.getString("biz_date"), rs.getString("stock_code"), rs.getInt("status")));
            // TODO 判断所有成分的status是否都是1
            log.info("constituent size is: " + constituentList.size());
            List<Valuation> valuationList = new ArrayList<>();
            Valuation startValuation = getValuationOneDay(indexCode, startDate, constituentList);
            valuationList.add(startValuation);
            // 获取成分记录第一天之后的数据
            List<IndexConstituent> constituentHistoryList = jdbcTemplate.query("SELECT * from index_constituent_history where index_code = ? and biz_date > ? order by biz_date", new Object[]{indexCode, startDate},
                    (rs, rowNum) -> new IndexConstituent(rs.getString("biz_date"), rs.getString("stock_code"), rs.getInt("status")));
            // 遍历开始日期的所有交易日
            for (String bizDate : indexDateList) {
                // 获取成分变更记录的下一条数据
                if (constituentHistoryList != null && constituentHistoryList.size() > 0 && bizDate.equals(constituentHistoryList.get(0).getBizDate())) {
                    getConstituentList(bizDate, constituentHistoryList, constituentList);
                }
                Valuation valuation = getValuationOneDay(indexCode, bizDate, constituentList);
                valuationList.add(valuation);
            }
            List<Object[]> params = valuationList.stream()
                    .map(valuation -> new Object[]{valuation.getIndexCode(), valuation.getBizDate(), valuation.getPe(), valuation.getPb()})
                    .collect(Collectors.toList());

            // Uses JdbcTemplate's batchUpdate operation to bulk load data
            jdbcTemplate.batchUpdate("INSERT INTO index_valuation(index_code, biz_date, pe, pb) VALUES (?, ?, ?, ?)", params);
        }
    }

    private void getConstituentList(String bizDate, List<IndexConstituent> constituentHistoryList, List<IndexConstituent> constituentList) {
        Iterator<IndexConstituent> iterator = constituentHistoryList.iterator();
        while (iterator.hasNext()) {
            IndexConstituent indexConstituent = iterator.next();
            if (!bizDate.equals(indexConstituent.getBizDate())) {
                break;
            }

            int status = indexConstituent.getStatus();
            if (status == 1) {
                constituentList.add(indexConstituent);
            } else if (status == 0) {
                constituentList.remove(indexConstituent);
            } else {
                log.error("index constituent is not correct, value is: " + status);
            }
            iterator.remove();
        }
    }

    private Valuation getValuationOneDay(String indexCode, String bizDate, List<IndexConstituent> constituentList) {
        StringBuilder stockList = new StringBuilder("(");
        Joiner.on(",").appendTo(stockList, Lists.transform(constituentList, new Function<IndexConstituent, String>() {
            @Override
            public String apply(IndexConstituent indexConstituent) {
                return "'" + indexConstituent.getStockCode() + "'";
            }
        }));
        stockList.append(")");

        // 查询pe， pb
        String sql = "SELECT biz_date, code, pe_ttm, pb from stock_hs where biz_date = ? and code in " + stockList.toString();
        List<Valuation> valuations = jdbcTemplate.query(sql, new Object[]{bizDate},
                (rs, rowNum) -> new Valuation(rs.getString("biz_date"), null, rs.getString("code"), rs.getDouble("pe_ttm"), rs.getDouble("pb")));
        Double peCal = valuations.stream().filter(valuation -> valuation.getPe() > 0).mapToDouble(valuation -> 1 / valuation.getPe()).sum();
        Double pbCal = valuations.stream().filter(valuation -> valuation.getPb() > 0).mapToDouble(valuation -> 1 / valuation.getPb()).sum();
        return new Valuation(bizDate, indexCode, null, valuations.size() / peCal, valuations.size() / pbCal);
    }
}
