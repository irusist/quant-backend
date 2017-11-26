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
@SpringBootApplication
public class UpdateValuationApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UpdateValuationApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UpdateValuationApplication.class, args);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {


        // 查询所有指数的估值数据
        List<Valuation> valuationList = jdbcTemplate.query("SELECT * from index_valuation_uqer order by index_code, biz_date",
                (rs, rowNum) -> new Valuation(rs.getString("biz_date"), rs.getString("index_code"), null, rs.getDouble("pe"), rs.getDouble("pb")));
        for (Valuation valuation : valuationList) {
            String indexCode = valuation.getIndexCode();
            String bizDate = valuation.getBizDate();
            log.info("preparing updating index: " + indexCode + ", biz_date: " + bizDate);
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
            jdbcTemplate.update("update index_valuation_uqer set sub_pe_size = ?, sub_pb_size = ?, all_size = ? where index_code = ? and biz_date = ?",
                    subPeSize, subPbSize, allSize, indexCode, bizDate);

        }
    }
}
