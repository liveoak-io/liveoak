Server Starter Module
=====================


Vert.x module that can be used to start the standalone server via vertx.


Building, and running
---------------------

This module was created using:

    mvn archetype:generate -Dfilter=io.vertx:

and simplified by removing unnecessary things, and tailored by modifying tests.

To rebuild, and run the server from this directory do the following ...
First, change to this module's directory:

    cd modules/starter

Then rebuild, and run server:

    mvn clean install
    mvn vertx:runMod


Or to enable remote debugging use:

    MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y" mvn vertx:runMod



Alternatively, without using maven ...

    vertx runZip target/restafari-starter-1.0.0-SNAPSHOT-mod.zip


To enable remote debugging use:

    VERTX_OPTS="-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y" vertx runZip target/restafari-starter-1.0.0-SNAPSHOT-mod.zip


Another way to run it is as a vertx module:

    VERTX_OPTS="-Dvertx.mods=target/mods" vertx runMod org.projectodd.restafari~restafari-starter~1.0.0-SNAPSHOT



Configuration
-------------

There is an example configuration file *mboss.json* that can be used to configure the server.

To take effect the file has to be passed to vertx as a configuration file e.g.:

    vertx runZip target/restafari-starter-1.0.0-SNAPSHOT-mod.zip -conf mboss.json


Server can also be configured via system properties which override configuration set through *mboss.json*.

