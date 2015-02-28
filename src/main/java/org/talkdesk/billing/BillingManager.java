package org.talkdesk.billing;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ashansa on 2/27/15.
 */
public class BillingManager {

    private static Log log = LogFactory.getLog(BillingManager.class);
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private DBManager dbManager;

    public BillingManager() throws IOException {
        dbManager = new DBManager();
    }

    /**
     *
     * @return
     */
    public BigDecimal charge(double callDuration, String accountId, String talkdeskNo, String customerNo, String forwardedNo) throws Exception {

        BigDecimal charge = getTalkdeskNumberCost(phoneUtil.parse(talkdeskNo, null)).add(getProfitMarginCost());

        if(forwardedNo != null) {
            charge = charge.add(getExternalNumberCost(phoneUtil.parse(forwardedNo,null), callDuration));
        }

        try {
            addInfoToAccount(accountId, customerNo, talkdeskNo, forwardedNo, callDuration, charge.doubleValue());
        } catch (SQLException e) {
            log.error("Error in adding the call details to user account");
        }
        return charge;
    }

    /**
     * call history details are store in the below order in columns
     * accountId, customerNo, talkdeskNo, forwardedNo, callDuration, charge
     * @return
     */
    public ArrayList<String[]> listCallHistory(String accountId) throws SQLException {
        ArrayList<String[]> callHistory = new ArrayList<String[]>();
        ResultSet results = dbManager.getCallHistory(accountId);
        while (results.next()) {
            //store call entry in order of customerNo, talkdeskNo, forwardedNo, callDuration, charge
            String[] callEntry = new String[5];
            callEntry[0] = results.getString(2); //starting from 2nd column which is customer no
            callEntry[1] = results.getString(3);
            callEntry[2] = results.getString(4);
            callEntry[3] = results.getString(5);
            callEntry[4] = results.getString(6);

            callHistory.add(callEntry);
        }
        return callHistory;
    }

    /**
     * calculated with talkdeskNumber
     * @param talkdeskNo
     * @return cost for talkdesk number
     */
    private BigDecimal getTalkdeskNumberCost(Phonenumber.PhoneNumber talkdeskNo) {

        String numberType = phoneUtil.getNumberType(talkdeskNo).name();
        if(PhoneNumberUtil.PhoneNumberType.TOLL_FREE.equals(numberType)) {
            if(Constants.US_REGION_CODE.equals(phoneUtil.getRegionCodeForNumber(talkdeskNo))) {
                return new BigDecimal(String.valueOf(Constants.US_TOLL_FREE_COST));
            } else if(Constants.UK_REGION_CODE.equals( phoneUtil.getRegionCodeForNumber(talkdeskNo))) {
                return new BigDecimal(String.valueOf(Constants.UK_TOLL_FREE_COST));
            }
        }
        return new BigDecimal(String.valueOf(Constants.DEFAULT_TALKDESK_COST));
    }

    /**
     * If call if not answered through browser and forwarded to external number externalNumberCost applies
     * @param forwardedNo
     * @param duration
     * @return external number cost
     */
    private BigDecimal getExternalNumberCost(Phonenumber.PhoneNumber forwardedNo, double duration) throws Exception {
        String region = phoneUtil.getRegionCodeForNumber(forwardedNo);
        String phoneNumber = phoneUtil.getCountryCodeForRegion(region) + phoneUtil.getNationalSignificantNumber(forwardedNo);

        ResultSet results = dbManager.getChargeDetails(region);
        String relatedCharge = null;
        String selectedPrefix = "";
        while (results.next()) {
            //ArrayList prefix = new ArrayList();
            //Collections.addAll(prefix, results.getString(3).replace(" ", "").split(","));
            for(String prefix : results.getString(3).replace(" ","").split(",")) {
                if(phoneNumber.toString().startsWith(prefix) && selectedPrefix.length() < prefix.length()) {
                    relatedCharge = results.getString(2);
                    selectedPrefix = prefix;
                }
            }
        }
        if(relatedCharge == null) {
            System.out.println("Could not find the charge for the external number. Adding default charge.");
            relatedCharge = String.valueOf(Constants.DEFAULT_CHARGE);
        }

        BigDecimal charge = new BigDecimal(relatedCharge);
        return charge.multiply(new BigDecimal(String.valueOf(duration)));
    }

    /**
     *
     * @return profit margin
     */
    private BigDecimal getProfitMarginCost() {
        //Returned a constant value shown in the problem as advice
        return new BigDecimal(String.valueOf(Constants.PROFIT_MARGIN));
    }

    private void addInfoToAccount(String accountId, String customerNo, String talkdeskNo, String forwardedNo,
                                  double callDuration, double charge) throws SQLException {
        if(forwardedNo == null)
            forwardedNo = "NONE";

        dbManager.addCallEntriesToUser(accountId, customerNo, talkdeskNo, forwardedNo, callDuration, charge);
    }



    public static void main(String args[]) {
        String number = "+94845348611";
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, null);
            String region = phoneUtil.getRegionCodeForNumber(phoneNumber);
            phoneUtil.getNumberType(phoneNumber);
            Locale.getISOCountries();

            //callDuration, accountId, talkdeskNo, customerNo, forwardedNo
           //System.out.println(new BillingManager().charge(40, "4f4a37a201c642014200000c", "+14845348611", "+35196191819",null));
          System.out.println(new BillingManager().charge(4,"123456789","+125456677","+418573925","+53859335"));

            ArrayList res = new BillingManager().listCallHistory("123456789");
            for (int i = 0; i < res.size() ; i++) {
                String[] entry = (String[]) res.get(i);
                System.out.println("call from " + entry[0] + " to " + entry[1] + " forwarded to " + entry[2] +
                        ". Duration: " + entry[3] + " and cost: " + entry[4]);
            }

        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
