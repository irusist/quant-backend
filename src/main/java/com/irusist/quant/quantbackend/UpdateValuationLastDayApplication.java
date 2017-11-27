package com.irusist.quant.quantbackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by zhulx on 24/11/2017.
 */
//@SpringBootApplication
public class UpdateValuationLastDayApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UpdateValuationLastDayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UpdateValuationLastDayApplication.class, args);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {
        // 获取最近一天的交易日
        String lastBizDate = jdbcTemplate.queryForObject("select biz_date from index_valuation order by biz_date desc limit 1", String.class);
        log.info(String.format("last biz_date: %s", lastBizDate));

        // 查询所有指数的估值数据
        List<Valuation> valuationList = jdbcTemplate.query("SELECT * from index_valuation where biz_date = ? order by index_code", new Object[]{lastBizDate},
                (rs, rowNum) -> new Valuation(rs.getString("biz_date"), rs.getString("index_code"), null, rs.getDouble("pe"), rs.getDouble("pb")));
        for (Valuation valuation : valuationList) {
            String indexCode = valuation.getIndexCode();
            String bizDate = valuation.getBizDate();
            log.info(String.format("preparing updating index: %s, biz_date: %s", indexCode, bizDate));
            // 查询小于当前pe的数量
            int subPeSize = jdbcTemplate.queryForObject("select count(1) from index_valuation where index_code = ? and biz_date < ? and pe < ?",
                    new Object[]{indexCode, bizDate, valuation.getPe()}, int.class);
            // 查询小于当前pb的数量
            int subPbSize = jdbcTemplate.queryForObject("select count(1) from index_valuation where index_code = ? and biz_date < ? and pb < ?",
                    new Object[]{indexCode, bizDate, valuation.getPb()}, int.class);
            // 查询总数量
            int allSize = jdbcTemplate.queryForObject("select count(1) from index_valuation where index_code = ? and biz_date < ?",
                    new Object[]{indexCode, bizDate}, int.class);

            // 更新数据
            jdbcTemplate.update("update index_valuation set sub_pe_size = ?, sub_pb_size = ?, all_size = ? where index_code = ? and biz_date = ?",
                    subPeSize, subPbSize, allSize, indexCode, bizDate);
            // 更新比例
            if (allSize != 0) {
                jdbcTemplate.update("update index_valuation set pe_ratio = ?, pb_ratio = ? where index_code = ? and biz_date = ?",
                        subPeSize / allSize, subPbSize / allSize, indexCode, bizDate);
            }
        }

        // 查询所有指数的估值数据
        valuationList.clear();
        valuationList = jdbcTemplate.query("SELECT * from index_valuation_uqer where biz_date = ? order by index_code", new Object[]{lastBizDate},
                (rs, rowNum) -> new Valuation(rs.getString("biz_date"), rs.getString("index_code"), null, rs.getDouble("pe"), rs.getDouble("pb")));
        for (Valuation valuation : valuationList) {
            String indexCode = valuation.getIndexCode();
            String bizDate = valuation.getBizDate();
            log.info(String.format("preparing updating index: %s, biz_date: %s", indexCode, bizDate));
            // 查询小于当前pe的数量
            int subPeSize = jdbcTemplate.queryForObject("select count(1) from index_valuation_uqer where index_code = ? and biz_date < ? and pe < ?",
                    new Object[]{indexCode, bizDate, valuation.getPe()}, int.class);
            // 查询小于当前pb的数量
            int subPbSize = jdbcTemplate.queryForObject("select count(1) from index_valuation_uqer where index_code = ? and biz_date < ? and pb < ?",
                    new Object[]{indexCode, bizDate, valuation.getPb()}, int.class);
            // 查询总数量
            int allSize = jdbcTemplate.queryForObject("select count(1) from index_valuation_uqer where index_code = ? and biz_date < ?",
                    new Object[]{indexCode, bizDate}, int.class);

            // 更新数据
            jdbcTemplate.update("update index_valuation_uqer set sub_pe_size = ?, sub_pb_size = ?, all_size = ? where index_code = ? and biz_date = ?",
                    subPeSize, subPbSize, allSize, indexCode, bizDate);
            // 更新比例
            if (allSize != 0) {
                jdbcTemplate.update("update index_valuation_uqer set pe_ratio = ?, pb_ratio = ? where index_code = ? and biz_date = ?",
                        subPeSize / allSize, subPbSize / allSize, indexCode, bizDate);
            }
        }
    }
}
