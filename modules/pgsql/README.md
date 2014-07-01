PostgreSQL persistence module
-----------------------------

To rebuild from project root and run tests against local PostgreSQL instance use:

    mvn -pl modules/pgsql clean install "-DargLine=-Dpgsql.db=tests -Dpgsql.host=localhost"

