package org.talkdesk.billing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Console;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ashansa on 2/27/15.
 */
public class CommandLineUI {

    private static Log log = LogFactory.getLog(CommandLineUI.class);
    private static Console console = System.console();
    private static BillingAPI api;

    public static void main(String[] args) throws IOException, SQLException {
        api = new BillingAPI();
        if(args.length > 0 && Constants.SETUP_TABLES.equals(args[0])) {
            try {
                new DBManager().populateDBs();
            } catch (SQLException e) {
                String msg = "Error while populating Tables";
                log.debug(msg, e);
                log.error(msg);
                throw e;
            } catch (IOException e) {
                String msg = "Error while populating Tables";
                log.debug(msg, e);
                log.error(msg);
                throw e;
            }
        } else {
            new CommandLineUI().launchMainUI();
        }
    }

    private void launchMainUI() {
        int choice = 0;

        System.out.println("\n1 Charge Phone");
        System.out.println("2 Account Details");
        System.out.println("3 Exit");
        try {
            choice = Integer.valueOf(console.readLine("\nPlease Enter your choice:\n"));
        } catch (NumberFormatException e) {
            System.out.println("\nPlease enter valid option number\n");
        }

        switch (choice) {
            case 1:
                chargePhone();
                break;

            case 2:
                getCallHistory();
                break;

            case 3:
                System.exit(0);

            default:
                launchMainUI();
        }
    }

    private void chargePhone() {
        try {
            String inputString = console.readLine("\nPlease enter callDuration, accountId, talkdeskNo, customerNo, forwardedNo(optional) " +
                    "seperated by comma(,):\n");
            String[] inputs = inputString.replace(" ", "").split(",");
            int noOfInputs = inputs.length;

            if (noOfInputs == 5) {
                System.out.println("Call cost: " + api.charge(Double.valueOf(inputs[0]), inputs[1], inputs[2], inputs[3], inputs[4]));
            }
            else if (noOfInputs == 4) {
                System.out.println("Call cost: " + api.charge(Double.valueOf(inputs[0]), inputs[1], inputs[2], inputs[3], null));
            } else {
                System.out.println("\nPlease enter required details. You can have only 4 or 5 (with forwardNo) values");
            }
            launchMainUI();
        } catch (Exception e) {
            System.out.println("Error occured. " + e.getMessage());
            launchMainUI();
        }
    }

    private void getCallHistory() {
        try {
            String accountId = console.readLine("\nPlease enter the account ID:\n");
            ArrayList<String[]> callHistory = api.getCallHistory(accountId.trim());
            if(callHistory.size() == 0) {
                System.out.println("\n No call history for the given account number");
                launchMainUI();
            } else {
                System.out.println("\n######### " + accountId + " call history #########\n");
                for (int i = 0; i < callHistory.size() ; i++) {
                    String[] entry = callHistory.get(i);
                    System.out.println("call from " + entry[0] + " to " + entry[1] + " forwarded to " + entry[2] +
                            ". Duration: " + entry[3] + " and cost: " + entry[4]);
                }
                launchMainUI();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
