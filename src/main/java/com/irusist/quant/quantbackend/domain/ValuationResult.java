package com.irusist.quant.quantbackend.domain;

/**
 * Created by zhulx on 25/11/2017.
 */
public class ValuationResult {

    private String bizDate;

    private Double pe;

    private Double peRatio;

    public ValuationResult(String bizDate, Double pe, Double peRatio) {
        this.bizDate = bizDate;
        this.pe = pe;
        this.peRatio = peRatio;
    }

    public String getBizDate() {
        return bizDate;
    }

    public void setBizDate(String bizDate) {
        this.bizDate = bizDate;
    }

    public Double getPe() {
        return pe;
    }

    public void setPe(Double pe) {
        this.pe = pe;
    }

    public Double getPeRatio() {
        return peRatio;
    }

    public void setPeRatio(Double peRatio) {
        this.peRatio = peRatio;
    }
}
