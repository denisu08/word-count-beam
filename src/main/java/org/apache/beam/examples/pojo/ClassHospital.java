package org.apache.beam.examples.pojo;

public class ClassHospital {
    private String DRGDefinition;
    private String providerId;
    private String providerStreetAddress;
    private String hospitalReferralRegionDescription;
    private Double averageTotalPayments;
    private Double averageMedicarePayments;
    private Double averageCoveredCharges;
    private String providerState;
    private String providerCity;
    private Integer providerName;
    private Integer providerZipCode;
    private Integer totalDischarges;

    public void setAverageTotalPayments(Double averageTotalPayments) {
        this.averageTotalPayments = averageTotalPayments;
    }

    public void setAverageMedicarePayments(Double averageMedicarePayments) {
        this.averageMedicarePayments = averageMedicarePayments;
    }

    public void setAverageCoveredCharges(Double averageCoveredCharges) {
        this.averageCoveredCharges = averageCoveredCharges;
    }

    public Integer getProviderName() {
        return providerName;
    }

    public void setProviderName(Integer providerName) {
        this.providerName = providerName;
    }

    public Integer getProviderZipCode() {
        return providerZipCode;
    }

    public void setProviderZipCode(Integer providerZipCode) {
        this.providerZipCode = providerZipCode;
    }

    public Integer getTotalDischarges() {
        return totalDischarges;
    }

    public void setTotalDischarges(Integer totalDischarges) {
        this.totalDischarges = totalDischarges;
    }

    public void setDRGDefinition(String drgDefinition) {
        this.DRGDefinition = drgDefinition;
    }

    public String getDRGDefinition() {
        return DRGDefinition;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderStreetAddress(String providerStreetAddress) {
        this.providerStreetAddress = providerStreetAddress;
    }

    public String getProviderStreetAddress() {
        return providerStreetAddress;
    }

    public void setHospitalReferralRegionDescription(String hospitalReferralRegionDescription) {
        this.hospitalReferralRegionDescription = hospitalReferralRegionDescription;
    }

    public String getHospitalReferralRegionDescription() {
        return hospitalReferralRegionDescription;
    }

    public void setAverageTotalPayments(double averageTotalPayments) {
        this.averageTotalPayments = averageTotalPayments;
    }

    public double getAverageTotalPayments() {
        return averageTotalPayments;
    }

    public void setAverageMedicarePayments(double averageMedicarePayments) {
        this.averageMedicarePayments = averageMedicarePayments;
    }

    public double getAverageMedicarePayments() {
        return averageMedicarePayments;
    }

    public void setAverageCoveredCharges(double averageCoveredCharges) {
        this.averageCoveredCharges = averageCoveredCharges;
    }

    public double getAverageCoveredCharges() {
        return averageCoveredCharges;
    }

    public void setProviderState(String providerState) {
        this.providerState = providerState;
    }

    public String getProviderState() {
        return providerState;
    }

    public void setProviderCity(String providerCity) {
        this.providerCity = providerCity;
    }

    public String getProviderCity() {
        return providerCity;
    }
}
