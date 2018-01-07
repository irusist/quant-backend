package com.irusist.quant.quantbackend;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by zhulx on 07/01/2018.
 */
@Service
public class EstimateService {

    private static final Logger log = LoggerFactory.getLogger(EstimateService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> estimate(String indexCode, Double change) {
        Map<String, Object> result = Maps.newLinkedHashMap();
        jdbcTemplate.query("select pe, pe_ratio from index_valuation_uqer where index_code = ? order by biz_date desc limit 1",
                new Object[]{indexCode}, (rs -> {
                    result.put("current_pe", rs.getDouble("pe"));
                    result.put("current_ratio", rs.getDouble("pe_ratio"));
                }));
        Double currentPe = (Double) result.get("current_pe");
        Double estimatePe = Double.valueOf(currentPe) * (1 + change / 100);

        int subCount = jdbcTemplate.queryForObject("select count(1) from index_valuation where index_code = ? and pe < ?",
                new Object[]{indexCode, estimatePe}, int.class);
        int allCount = jdbcTemplate.queryForObject("select count(1) from index_valuation where index_code = ?",
                new Object[]{indexCode}, int.class);

        log.info(String.format("current pe is : %s, change is %s%%, estimate pe is: %s", currentPe, change, estimatePe));
        result.put("estimate_pe", estimatePe);
        result.put("estimate_ratio", subCount * 1.0 / allCount);
        return result;
    }
}
