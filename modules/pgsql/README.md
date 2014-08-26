PostgreSQL persistence module
-----------------------------

To rebuild from project root and run tests against local PostgreSQL instance use:

    mvn -pl modules/pgsql clean install

If you want to override default database connection settings you can use:

    mvn -pl modules/pgsql clean install "-DargLine=-Dpgsql.db=tests -Dpgsql.server=localhost -Dpgsql.user=test -Dpgsql.password=test"
    
These the default values, so you can use the short version above.

You may first have to prepare the database 'test', and user 'test'. Specifically, user 'test' needs to have a permission to create 
 schemas.

On Linux / Mac OS X you simply execute the following:

    initdb -D ~/.liveoak/pgsql/data
    pg_ctl -D ~/.liveoak/pgsql/data -l logfile start
    createdb test
    psql test
    CREATE USER test createdb PASSWORD 'test';
    GRANT CREATE ON DATABASE test TO test;    
    \q

