package com.irusist.quant.quantbackend;

import com.google.common.collect.Lists;
import com.irusist.quant.quantbackend.domain.ValuationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhulx on 06/01/2018.
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/getValuation")
public class ValuationController {

    @Autowired
    private ValuationService valuationService;

    @RequestMapping("/{index}")
    public Map<String, Object> get(@PathVariable String index) {
        List<ValuationResult> valuationList = valuationService.getValuationList(index);
        List<String> dateList = Lists.newArrayList();
        List<Double> peList = Lists.newArrayList();
        List<Double> peRatioList = Lists.newArrayList();
        valuationList.forEach(valuation ->
        {
            dateList.add(valuation.getBizDate());
            peList.add(valuation.getPe());
            peRatioList.add(valuation.getPeRatio());
        });
        Map<String, Object> result = new HashMap<>();
        result.put("date", dateList);
        result.put("pe", peList);
        result.put("pe_ratio", peRatioList);

        // 30%高度线
//        List<Double> p30 = Lists.newArrayListWithCapacity(peRatioList.size());
//        Collections.fill(p30, 0.3D);
//        for (int i = 0; i < peRatioList.size(); i++) {
//            p30.add(0.3D);
//        }
//
//        result.put("p30", p30);
        return result;
    }
}
