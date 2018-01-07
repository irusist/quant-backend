package com.irusist.quant.quantbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by zhulx on 07/01/2018.
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/estimate")
public class EstimateController {

    @Autowired
    private EstimateService estimateService;

    @RequestMapping("/{index}/{change:.+}")
    public Map<String, Object> estimate(@PathVariable String index, @PathVariable("change") String change) {
        return estimateService.estimate(index, Double.parseDouble(change));
    }

}
