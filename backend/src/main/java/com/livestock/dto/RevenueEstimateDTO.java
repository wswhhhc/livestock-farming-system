package com.livestock.dto;

import java.math.BigDecimal;

public class RevenueEstimateDTO {
    private Long batchId;
    private String batchNo;
    private Long categoryId;
    private String categoryName;
    private Integer currentStage;
    private Integer currentQuantity;
    private String entryDate;
    private String expectedSlaughterDate;

    private BigDecimal basePrice;
    private BigDecimal slaughterWeight;
    private BigDecimal estimatedSlaughterRate;

    private BigDecimal adjustedPrice;
    private String priceAdjustDesc;

    private BigDecimal estimatedWeight;
    private BigDecimal estimatedRevenue;
    private BigDecimal projectedFeedCost;
    private BigDecimal projectedLaborWaterEquip;
    private BigDecimal medicalOtherCost;
    private BigDecimal totalCost;
    private BigDecimal estimatedProfit;
    private String profitMargin;

    private Integer applicableRuleCount;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getCurrentStage() { return currentStage; }
    public void setCurrentStage(Integer currentStage) { this.currentStage = currentStage; }
    public Integer getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public String getExpectedSlaughterDate() { return expectedSlaughterDate; }
    public void setExpectedSlaughterDate(String expectedSlaughterDate) { this.expectedSlaughterDate = expectedSlaughterDate; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public BigDecimal getSlaughterWeight() { return slaughterWeight; }
    public void setSlaughterWeight(BigDecimal slaughterWeight) { this.slaughterWeight = slaughterWeight; }
    public BigDecimal getEstimatedSlaughterRate() { return estimatedSlaughterRate; }
    public void setEstimatedSlaughterRate(BigDecimal estimatedSlaughterRate) { this.estimatedSlaughterRate = estimatedSlaughterRate; }
    public BigDecimal getAdjustedPrice() { return adjustedPrice; }
    public void setAdjustedPrice(BigDecimal adjustedPrice) { this.adjustedPrice = adjustedPrice; }
    public String getPriceAdjustDesc() { return priceAdjustDesc; }
    public void setPriceAdjustDesc(String priceAdjustDesc) { this.priceAdjustDesc = priceAdjustDesc; }
    public BigDecimal getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(BigDecimal estimatedWeight) { this.estimatedWeight = estimatedWeight; }
    public BigDecimal getEstimatedRevenue() { return estimatedRevenue; }
    public void setEstimatedRevenue(BigDecimal estimatedRevenue) { this.estimatedRevenue = estimatedRevenue; }
    public BigDecimal getProjectedFeedCost() { return projectedFeedCost; }
    public void setProjectedFeedCost(BigDecimal projectedFeedCost) { this.projectedFeedCost = projectedFeedCost; }
    public BigDecimal getProjectedLaborWaterEquip() { return projectedLaborWaterEquip; }
    public void setProjectedLaborWaterEquip(BigDecimal projectedLaborWaterEquip) { this.projectedLaborWaterEquip = projectedLaborWaterEquip; }
    public BigDecimal getMedicalOtherCost() { return medicalOtherCost; }
    public void setMedicalOtherCost(BigDecimal medicalOtherCost) { this.medicalOtherCost = medicalOtherCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getEstimatedProfit() { return estimatedProfit; }
    public void setEstimatedProfit(BigDecimal estimatedProfit) { this.estimatedProfit = estimatedProfit; }
    public String getProfitMargin() { return profitMargin; }
    public void setProfitMargin(String profitMargin) { this.profitMargin = profitMargin; }
    public Integer getApplicableRuleCount() { return applicableRuleCount; }
    public void setApplicableRuleCount(Integer applicableRuleCount) { this.applicableRuleCount = applicableRuleCount; }
}
