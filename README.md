# talkdesk-billing
Update resources/db.properties file with db connection details
file for rates_file property is in resources directory. update the path for rates_file

You should create the database with the specified name in db.properties

To setup the system:  build the application and navigate to target directory and run
java - jar call-billing-1.0-SNAPSHOT-jar-with-dependencies.jar setup
This will clean up the billing information and create new tables for the system.

To run the application
java -jar call-billing-1.0-SNAPSHOT-jar-with-dependencies.jar
