package io.spring.billrun.job;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

public class BillingDataProcessor implements ItemProcessor<BillingData, ReportingData> {

    @Value("${spring.cellular.pricing.data:0.01}")
    private Float dataPrincing;

    @Value("${spring.cellular.princing.call: 0.5}")
    private Float callPrincing;

    @Value("${spring.cellular.pricing.sms:0.1}")
    private Float smsPrincing;

    @Value("${spring.cellular.spending.threshold:150}")
    private Float spendingThresholding;



    @Override
    public ReportingData process(BillingData item) throws Exception {
        Double billingTotal = (double) (item.dataUsage() * dataPrincing
                        + item.callDuration() * callPrincing
                        + item.smsCount() * smsPrincing);
        if(billingTotal < spendingThresholding) {
            return null;
        }

        return new ReportingData(item, billingTotal);

    }
}
