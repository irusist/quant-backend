package com.irusist.quant.quantbackend;

import com.irusist.quant.quantbackend.domain.ValuationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhulx on 06/01/2018.
 */
@Service
public class ValuationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ValuationResult> getValuationList(String index) {
        List<ValuationResult> result = jdbcTemplate.query("select biz_date, pe, pe_ratio from index_valuation_uqer where index_code = ? order by biz_date", new Object[]{index},
                (rs, rowNum) -> new ValuationResult(rs.getString("biz_date"), rs.getDouble("pe"), rs.getDouble("pe_ratio")));
        return result;
    }
}
