package com.livestock.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {
    private Summary summary;
    private Pending pending;
    private Revenue revenue;

    public Summary getSummary() { return summary; }
    public void setSummary(Summary summary) { this.summary = summary; }
    public Pending getPending() { return pending; }
    public void setPending(Pending pending) { this.pending = pending; }
    public Revenue getRevenue() { return revenue; }
    public void setRevenue(Revenue revenue) { this.revenue = revenue; }

    public static class Summary {
        private int totalQuantity;
        private int activeBatchCount;
        private int categoryCount;
        private int siteCount;

        public int getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
        public int getActiveBatchCount() { return activeBatchCount; }
        public void setActiveBatchCount(int activeBatchCount) { this.activeBatchCount = activeBatchCount; }
        public int getCategoryCount() { return categoryCount; }
        public void setCategoryCount(int categoryCount) { this.categoryCount = categoryCount; }
        public int getSiteCount() { return siteCount; }
        public void setSiteCount(int siteCount) { this.siteCount = siteCount; }
    }

    public static class Pending {
        private long unreadAdvice;
        private long slaughterReady;
        private long overdueBatches;

        public long getUnreadAdvice() { return unreadAdvice; }
        public void setUnreadAdvice(long unreadAdvice) { this.unreadAdvice = unreadAdvice; }
        public long getSlaughterReady() { return slaughterReady; }
        public void setSlaughterReady(long slaughterReady) { this.slaughterReady = slaughterReady; }
        public long getOverdueBatches() { return overdueBatches; }
        public void setOverdueBatches(long overdueBatches) { this.overdueBatches = overdueBatches; }
    }

    public static class Revenue {
        private BigDecimal estimatedRevenue;
        private BigDecimal estimatedProfit;
        private BigDecimal totalCost;
        private String profitMargin;

        public BigDecimal getEstimatedRevenue() { return estimatedRevenue; }
        public void setEstimatedRevenue(BigDecimal estimatedRevenue) { this.estimatedRevenue = estimatedRevenue; }
        public BigDecimal getEstimatedProfit() { return estimatedProfit; }
        public void setEstimatedProfit(BigDecimal estimatedProfit) { this.estimatedProfit = estimatedProfit; }
        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
        public String getProfitMargin() { return profitMargin; }
        public void setProfitMargin(String profitMargin) { this.profitMargin = profitMargin; }
    }

    // ========== Chart Data ==========

    public static class CategoryDistItem {
        private String name;
        private int batchCount;
        private int quantity;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getBatchCount() { return batchCount; }
        public void setBatchCount(int batchCount) { this.batchCount = batchCount; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public static class CostTrendItem {
        private String month;
        private BigDecimal totalAmount;
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class SiteUtilItem {
        private String siteName;
        private int capacity;
        private int currentStock;
        private double usageRate;
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public int getCurrentStock() { return currentStock; }
        public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
        public double getUsageRate() { return usageRate; }
        public void setUsageRate(double usageRate) { this.usageRate = usageRate; }
    }

    public static class StageDistItem {
        private String stage;
        private int count;
        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}
