Mongo persistence module

To rebuild from project root and run tests against local MongoDB instance use:

    mvn -pl modules/mongo clean install "-DargLine=-Dmongo.db=mydb -Dmongo.host=localhost"
