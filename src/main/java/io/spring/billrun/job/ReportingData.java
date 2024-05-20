package io.spring.billrun.job;

public record ReportingData(BillingData billingData, Double billingTotal) {
}
