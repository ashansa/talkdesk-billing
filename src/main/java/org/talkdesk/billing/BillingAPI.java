package org.talkdesk.billing;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ashansa on 2/27/15.
 */
public class BillingAPI {

    private static BillingManager billingManager;

    public BillingAPI() throws IOException {
        billingManager = new BillingManager();
    }

    public float charge(double callDuration, String accountId, String talkdeskNo, String customerNo, String forwardedNo) throws Exception {
        return billingManager.charge(callDuration, accountId, talkdeskNo, customerNo, forwardedNo).floatValue();
    }

    /**
     *
     * @return
     */
    public ArrayList<String[]> getCallHistory(String accountId) throws SQLException {
        return billingManager.listCallHistory(accountId);
    }
}
