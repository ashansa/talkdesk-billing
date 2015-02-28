# talkdesk-billing
Talkdesk-billing gives the cost that the talkdesk will charge for a call given
    callDuration, accountId, talkdeskNo, customerNo, forwardedNo(optional)
And you can get the call history for a perticular account by giving the accountId

How to run the project?

Update resources/db.properties file with db connection details
file for rates_file property is in resources directory. update the path for rates_file

You should create the database with the specified name in db.properties

To setup the system:  build the application (mvn clean install) and navigate to target directory and run
  java - jar call-billing-1.0-SNAPSHOT-jar-with-dependencies.jar setup
This will clean up the billing information and create new tables for the system.

To run the application
  java -jar call-billing-1.0-SNAPSHOT-jar-with-dependencies.jar
and follow the instructions in the command line.

Sample input for option1:
25,234ID,+14845348611,+351961918192,+351961918346
