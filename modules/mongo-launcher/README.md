Mongo Launcher
==============

This module contains classes that cover several features:

 - Finding existing MongoDB installation on the system, and installing a new one if none is found (see MongoInstaller.java)
 - Launching a mongod process to listen on specific port if none is found on that port (see MongoLauncher.java)
 - Ensuring MongoDB is started before applications depending on it are deployed (see MongoLauncherExtension.java)
 - Performin autosetup before any LiveOak extensions are started that properly configures and installs MongoLauncherExtension by
   generating and placing mongo-launcher.json file in $LIVEOAK_HOME/conf/extensions directory (see MongoLauncherAutoSetup.java)


Automatic configuration
-----------------------

When LiveOak is started it automatically scans for presence of MongoDB. If none is found it tries to install one
using an internet connection. MongoDB instance installed that way is placed in $HOME/.liveoak/mongo directory.

This process is fully automatic, and requires zero interaction.


Manual configuration
--------------------

On first run of LiveOak launcher, $LIVEOAK_HOME/conf/extensions/mongo-launcher.json file is created. This file can be created
manually beforehand with custom configuration.

Its content looks like:

    {
      module-id : "io.liveoak.mongo-launcher",
      config : {
        enabled : "auto",
        log-path : "/Users/john/LiveOak/launcher/data/mongod.log",
        pid-file-path : "/Users/john/LiveOak/launcher/data/mongod.pid",
        db-path : "/Users/john/LiveOak/launcher/data",
        use-small-files: true,
        mongod-path : "/Users/john/.liveoak/mongo/mongodb-osx-x86_64-2.4.9/bin/mongod"
      }
    }

The following config option can be used:

### enabled: [auto | true | false]

 This option controls if mongod should be started. The default value is 'auto'.

 If value is 'auto', we first check if existing mongod instance is listening on mongod port. If port accepts connections, then
 a new mongod instance will not be started.

 Values 'true', and 'false' will be followed blindly - mongod will be started, or there will be no attempt to start it.

### log-path: Path to log file

 This value is passed to mongod as *--logpath* argument.

### pid-file-path: Path to pid file

 This value is passed to mongod as *--pidfilepath* argument.

### db-path: Path to directory containing mongodb data files

 This value is passed to mongod as *--dbpath* argument.

### mongod-path: Path to mongod executable

 If there is existing mongod installation that should be used, this is how to point to it.

### port: Mongod port

 This value is passed to mongod as *--port* argument.

### use-small-files: [true | false]

 This value controls whether mongod is started with *--smallfiles* argument. The default value is true.
 It is used to reduce disk space usage.

### extra-args: arguments directly passed to mongod

 This value is appended to mongod command line composed from all the previous options.

 Example:

    extra-args : "-vv --maxConns 5"

