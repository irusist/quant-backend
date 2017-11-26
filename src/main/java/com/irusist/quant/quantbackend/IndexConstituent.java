package com.irusist.quant.quantbackend;

/**
 * Created by zhulx on 25/11/2017.
 */
public class IndexConstituent {

    private String bizDate;

    private String stockCode;

    private int status;

    public IndexConstituent(String bizDate, String stockCode, int status) {
        this.bizDate = bizDate;
        this.stockCode = stockCode;
        this.status = status;
    }

    public String getBizDate() {
        return bizDate;
    }

    public String getStockCode() {
        return stockCode;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "IndexConstituent{" +
                "bizDate='" + bizDate + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public int hashCode() {
        return stockCode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        IndexConstituent other = (IndexConstituent) obj;
        return other.stockCode.equals(stockCode);
    }
}
