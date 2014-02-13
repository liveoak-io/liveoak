Keycloak module

To rebuild from project root and run tests against local MongoDB instance use:

    mvn -pl modules/keycloak clean install -Dmongo.db=mydb -Dkeycloak.mongo.clearOnStartup=true

keycloak.mongo.clearOnStartup=true will clear the DB collections to ensure that test has clear environment (Skip this option if you don't want this)