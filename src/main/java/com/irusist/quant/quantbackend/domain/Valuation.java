package com.irusist.quant.quantbackend.domain;

/**
 * Created by zhulx on 25/11/2017.
 */
public class Valuation {

    private String bizDate;

    private String indexCode;

    private String stockCode;

    private Double pe;

    private Double pb;

    public Valuation(String bizDate, String indexCode, String stockCode, Double pe, Double pb) {
        this.bizDate = bizDate;
        this.indexCode = indexCode;
        this.stockCode = stockCode;
        this.pe = pe;
        this.pb = pb;
    }

    public String getBizDate() {
        return bizDate;
    }

    public String getIndexCode() {
        return indexCode;
    }

    public String getStockCode() {
        return stockCode;
    }

    public Double getPe() {
        return pe;
    }

    public Double getPb() {
        return pb;
    }

    @Override
    public String toString() {
        return "Valuation{" +
                "bizDate='" + bizDate + '\'' +
                ", indexCode='" + indexCode + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", pe=" + pe +
                ", pb=" + pb +
                '}';
    }
}
