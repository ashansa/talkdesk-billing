package org.talkdesk.billing.test;


import org.talkdesk.billing.BillingAPI;
import org.testng.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by ashansa on 2/28/15.
 */


public class BillingTest {

    BillingAPI billingAPI;

    public static void main(String[] args) throws Exception {
        BillingTest billingTest = new BillingTest();
        billingTest.testCallHistory();
        billingTest.testCharge();
    }

    private BillingTest() throws IOException {
        billingAPI = new BillingAPI();
    }

    private void testCharge() throws Exception {
        Random random = new Random();
        String[] phoneNumbers = {"+94893902843", "+35522348201", "+1264582925"};
        Map<String, BigDecimal> rates = new HashMap();
        rates.put(phoneNumbers[0], new BigDecimal("0.17500"));
        rates.put(phoneNumbers[1], new BigDecimal("0.25500"));
        rates.put(phoneNumbers[2], new BigDecimal("0.27500"));

        for (int i = 0; i < rates.size(); i++) {
            int duration = random.nextInt(500);
            float charge = billingAPI.charge(duration, UUID.randomUUID().toString(), generatePhoneNumber(), generatePhoneNumber(), phoneNumbers[i]);
            BigDecimal expected = new BigDecimal("0.1").add(new BigDecimal("0.5")).add(rates.get(phoneNumbers[i]).multiply(new BigDecimal(duration)));
            Assert.assertEquals(charge, expected.floatValue());
        }
    }

    private void testCallHistory() throws Exception {
        Random random = new Random();
        String accountID = UUID.randomUUID().toString();
        int noOfEntries = random.nextInt(10);

        for (int i = 0; i < noOfEntries; i++) {
            billingAPI.charge(random.nextInt(500), accountID, generatePhoneNumber(), generatePhoneNumber(), generatePhoneNumber());
        }
        Assert.assertEquals(noOfEntries, billingAPI.getCallHistory(accountID).size());


    }

    private String generatePhoneNumber() {
        Random random = new Random();
        return "+" + (random.nextInt(999999999) + 1000000000 );
    }
}
