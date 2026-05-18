package com.livestock.dto;

import java.math.BigDecimal;

public class RevenueSummaryDTO {
    private int batchCount;
    private int totalQuantity;
    private BigDecimal totalEstimatedWeight;
    private BigDecimal totalEstimatedRevenue;
    private BigDecimal totalCost;
    private BigDecimal totalEstimatedProfit;
    private String overallProfitMargin;
    private BigDecimal todayCost;
    private BigDecimal historicalTotalCost;

    public int getBatchCount() { return batchCount; }
    public void setBatchCount(int batchCount) { this.batchCount = batchCount; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public BigDecimal getTotalEstimatedWeight() { return totalEstimatedWeight; }
    public void setTotalEstimatedWeight(BigDecimal totalEstimatedWeight) { this.totalEstimatedWeight = totalEstimatedWeight; }
    public BigDecimal getTotalEstimatedRevenue() { return totalEstimatedRevenue; }
    public void setTotalEstimatedRevenue(BigDecimal totalEstimatedRevenue) { this.totalEstimatedRevenue = totalEstimatedRevenue; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getTotalEstimatedProfit() { return totalEstimatedProfit; }
    public void setTotalEstimatedProfit(BigDecimal totalEstimatedProfit) { this.totalEstimatedProfit = totalEstimatedProfit; }
    public String getOverallProfitMargin() { return overallProfitMargin; }
    public void setOverallProfitMargin(String overallProfitMargin) { this.overallProfitMargin = overallProfitMargin; }
    public BigDecimal getTodayCost() { return todayCost; }
    public void setTodayCost(BigDecimal todayCost) { this.todayCost = todayCost; }
    public BigDecimal getHistoricalTotalCost() { return historicalTotalCost; }
    public void setHistoricalTotalCost(BigDecimal historicalTotalCost) { this.historicalTotalCost = historicalTotalCost; }
}
