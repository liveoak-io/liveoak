
PgSql REST API - Developer's Guide
==================================

PgSql module provides REST API to underlying PostgreSQL database.


Features
========

PgSql module provides a REST endpoint that exposes the data (rows in tables), and the meta data (table definitions) in a JSON format.

The data is not represented as a simple flat JSON object per table row, rather the endpoint takes into account foreign key constraints between tables, and automatically creates hierarchical JSON objects containing nested objects for many-to-one relationships, and lists of objects for one-to-many relationships.

Using 'fields' url query parameter client can [control which fields to return](GUIDE.md#controlling-which-fields-to-return), and how deep to expand object hierarchies.

Client can use 'sort' query parameter for [sorting results](GUIDE.md#sorting), and 'offset', and 'limit' query parameters for [pagination](GUIDE.md#pagination).

PgSql endpoint provides full CRUD support, also supports creating, and updating graphs of objects, and automatically performs cascading deletes as necessary.

Using 'q' url query parameter a query can be passed in a [Mongo DB JSON query syntax](http://docs.mongodb.org/manual/reference/operator/query) to perform [querying](GUIDE.md#querying).

There is also a special '_batch' endpoint for [bulk operations](GUIDE.md#bulk-operations). One can get table definitions of all the tables, and export all data from all tables, then submit responses to '_batch' endpoint configured against another database to restore same table definitions, and fill the tables with the same data.


Configuration
=============

PgSql service is automatically made available to applications through extensions mechanism.

There is a file pgsql.json in $LIVEOAK/conf/extensions directory which triggers installation of PgSqlExtension.


To make use of the extension, an application has to configure a resource of type 'pgsql' in its application.json file e.g.:

    {
      resources: {
        sqldata: {
          type: 'pgsql',
          config: {
            server: 'localhost',
            port: 5432,
            db: 'test',
            user: 'test',
            password: 'test',
            schemas: 'test'
          }
        }
      } 
    }

In this case we bind a new 'pgsql' resource to /APP_NAME/sqldata endpoint, where APP_NAME is the name of our application. 


The available configuration options are:

    {
      'db': 'database name',
      'server': 'hostname or ip',
      'port': 5432,
      'user': 'username',
      'password': 'password',
      'max-connections': 10,
      'initial-connections': 1,
      'schemas': [],
      'blocked-schemas': [],
      'default-schema': 'test',
      'allow-create-schema': false
    }

Options are mostly self explanatory.

* schemas
>can be set to a list of schema names that are to be accessible via pgsql endpoint. If left empty, all schemas will be accessible

* blocked-schemas
>can be set to a list of schemas to be explicitly inaccessible via pgsql endpoint. These override any value in 'schemas'

* default-schema 
>is a schema name to be used whenever only a table name is specified without schema part

* allow-create-schema 
>provides ability to control if when encountering an unknown schema name an attempt should automatically be made to create a new schema with that name. Database user needs to have the necessary permission to create a new schema.


REST API
========

In LiveOak resources are structured in a very simple way. Each application gets its namespace under root. It further partitions this namespace to subcontexts where each subcontext is handled by a resource registered to that subcontext.

Following the configuration example above, PgSql root endpoint is available at /APP_NAME/sqldata.

Let's assume our application name is demo-app, and is deployed to a LiveOak server running on localhost. We can then access it at:

http://localhost:8080/demo-app/sqldata

We'll refer to this as 'PgSql endpoint'.

We use 'curl' in the examples below. For brewity some non-essential parameters are left out, but may under some circumstances be required - depending on application configuration.

Specifically, it may be necessary to use

    -H 'Content-Type: application/json'

when performing POST / PUT operations.

And it may sometimes be necessary to use

    -H 'Accept: application/json'



Listing tables (collections)
============================

GET http://localhost:8080/demo-app/sqldata
 
    $ curl http://localhost:8080/demo-app/sqldata 
    
    {
      "id" : "sqldata",
      "self" : {
        "href" : "/demo-app/sqldata"
      },
      "links" : [ {
        "rel" : "batch",
        "href" : "/demo-app/sqldata/_batch"
      } ],
      "count" : 0,
      "type" : "database"
    }

All resources contain at least an 'id' field, and a 'self' field containing a 'href'. The first one is a convenience, since the self/href already uniquely identifies a resource.

Also commonly returned field is 'links' which helps with the discovery of related endpoints in the spirit of HATEOAS.

Collection resources contain a 'count' field, which returns a number of children. In this case there are no children, as there are no tables yet.

The 'type' field helps tools determine the contract to use when communicating with this REST endpoint.


If some tables were already present in the database, then we would also receive a 'members' field listing the child items.

For example we might receive:

    {
      "id" : "sqldata",
      "self" : {
        "href" : "/demo-app/sqldata"
      },
      "links" : [ {
        "rel" : "batch",
        "href" : "/demo-app/sqldata/_batch"
      } ],
      "count" : 2,
      "type" : "database",
      "members" : [ {
        "id" : "rooms",
        "self" : {
          "href" : "/demo-app/sqldata/rooms"
        }
      }, {
        "id" : "users",
        "self" : {
          "href" : "/demo-app/sqldata/users"
        }
      } ]
    }

For children we only receive object stubs with identity information.



Creating a table
================

To create a new table we POST a JSON message describing the table to PgSql endpoint.

POST http://localhost:8080/demo-app/sqldata

    $ curl -X POST 'http://localhost:8080/demo-app/sqldata' -T - << EOF
    
    {
      "id" : "users",
      "columns" : [ {
        "name" : "user_id",
        "type" : "varchar",
        "size" : 40
      }, {
        "name" : "nick",
        "type" : "varchar",
        "size" : 60,
        "nullable" : false,
        "unique" : true
      }, {
        "name" : "last_login",
        "type" : "timestamp",
        "nullable" : false
      } ],
      "primary-key" : [ "user_id" ]
    }
    
    EOF


When Content-Type header is not present, LiveOak assumes it to be 'application/json'.

We get back a JSON describing the created table schema:

    {
      "id" : "users;schema",
      "self" : {
        "href" : "/demo-app/sqldata/users;schema"
      },
      "columns" : [ {
        "name" : "user_id",
        "type" : "varchar",
        "size" : 40,
        "nullable" : false,
        "unique" : true
      }, {
        "name" : "nick",
        "type" : "varchar",
        "size" : 60,
        "nullable" : false,
        "unique" : true
      }, {
        "name" : "last_login",
        "type" : "timestamp",
        "size" : 29,
        "nullable" : false,
        "unique" : false
      } ],
      "primary-key" : [ "user_id" ],
      "ddl" : "CREATE TABLE \"test\".\"users\" (\"user_id\" varchar (40), \"nick\" varchar (60) UNIQUE NOT NULL, \"last_login\" timestamp NOT NULL, PRIMARY KEY (\"user_id\"))"
    }


Note how we get back more information than what we sent to the server. We get all the values of all the fields. Where we didn't specify a value in submitted JSON the default was used.

There is also a read-only field called 'ddl' that contains a 

Table schema is described by four fields:

  * id

    >contains a table name, which can also be prefixed by a schema name followed by a dot e.g.: id: 'test.users'.
    >If schema doesn't exist the endpoint will attempt to create it. 

  * columns

    >contains a list of column specifications.
    >A column specification has the following fields:
       
    * name
    
    >column name
    
    * type
    
    >a PostgreSQL column type
    
    * size
    
    >column size information. It depends on the type if this value is editable at all
    
    * nullable
    
    >specifies if this column's value can be null.
    >Default value is true.
    
    * unique
    
    >specifies if this column's value is unique among all the rows in the table. 
    >Default value is false.
           
  * primary-key

    >contains a single column name or a list of columns names that together form a primary key
    
  * foreign-keys

    >contains a list or a single foreign-key specifications, each of which has the following format:
      
    * table
    
    >id of the table this foreign key references
    
    * columns
    
    >a single column name, or list of column names that together form a primary key in the referenced table

  * ddl

    >a read-only field containing schema specification in convenient format ready for copy paste into other tools



Retrieving a table schema
=========================

Table schemas can be retrieved by appending a ;schema suffix to a collection URL.


GET http://localhost:8080/demo-app/sqldata/TABLEID;schema

    
    $ curl -v 'http://localhost:8080/demo-app/sqldata/rooms;schema'
    
    {
      "id" : "rooms;schema",
      "self" : {
        "href" : "/demo-app/sqldata/rooms;schema"
      },
      "columns" : [ {
        "name" : "room_id",
        "type" : "varchar",
        "size" : 40,
        "nullable" : false,
        "unique" : true
      }, {
        "name" : "owner_id",
        "type" : "varchar",
        "size" : 40,
        "nullable" : false,
        "unique" : false
      }, {
        "name" : "name",
        "type" : "varchar",
        "size" : 60,
        "nullable" : false,
        "unique" : true
      } ],
      "primary-key" : [ "room_id" ],
      "foreign-keys" : [ {
        "table" : "test.users",
        "columns" : [ "owner_id" ]
      } ],
      "ddl" : "CREATE TABLE \"test\".\"rooms\" (\"room_id\" varchar (40), \"owner_id\" varchar (40) NOT NULL, \"name\" varchar (60) UNIQUE NOT NULL, \"public\" bool NOT NULL, \"create_time\" timestamp NOT NULL, PRIMARY KEY (\"room_id\"), FOREIGN KEY (\"owner_id\") REFERENCES \"test\".\"users\" (\"user_id\"))"
    }




Dropping a table
================

DELETE http://localhost:8080/demo-app/sqldata/TABLEID

    curl -v -X DELETE 'http://localhost:8080/demo-app/sqldata/rooms'

    {
      "id" : "rooms",
      "self" : {
        "href" : "/demo-app/sqldata/rooms"
    }


The returned status has no error-type section in it, meaning that the operation was successful - the table, and all the data it contained was removed.

If another table had a foreign key constraint reference to this table, the operation would fail.
  



Column fields Vs. synthetic fields
==================================

PgSql endpoint maps tables to collections, and table rows to collection items. Table rows contain data in columns, and this data is exported as JSON document containing fields. 

In PostgreSQL tables are fully identified by specifying a schema, and a table name. Schema is a namespace that contains tables. There can be multiple tables called 'room' each existing in a different schema.

To keep the collection ids as simple as possible, a full set of visible schemas and tables is taken into account, and where a table name is only present in one schema, the collection id only uses table name, without a schema prefix.

Where duplicates are detected, collection id is equal to schema name + dot + table name.

Row data is mapped to JSON by using column names as field names, except for foreign key columns.

For foreign key columns a synthentic field is created (field that has a different name than a column, and that represents a resource reference, or entire embedded object).

If a column name ends with '_id' then this ending is chopped off and the first part is used as a field name (e.g. if column is called 'owner_id', then field name is 'owner'.

If column name doesn't end with '_id' then the referred table id is used as a field name (e.g. if foreign key column 'userid' references a table called 'users', then the field name will be called 'users' rather than 'userid'.

In addition to row columns, the JSON item also contains synthetic fields for one-to-many relationships. These are references to items in other collections, representing tables with foreign keys referring to our table. There are corresponding columns in our table in this case. For these fields the name is equal to the other collection's id.

For example, if there is a table 'messages' with column 'user_id' pointing to a table 'users', then each item in 'users' collection will contain a synthetic field named 'messages' containing a list of items corresponding to messages linked to that user.




Creating records (items)
========================

POST http://localhost:8080/demo-app/sqldata/TABLEID

    $ curl -v -X POST 'http://localhost:8080/demo-app/sqldata/users' -T - << EOF
    
    {                                                             
      'id': '0000001',
      'nick': 'rabbit',
      'last_login': 0
    }
    
    EOF


If successful this returns the fields corresponding to all the columns of the targeted table.

Client always has to specify an 'id' field when creating any item. Currently an 'id' can't be autogenerated. In the future it will be possible to configure a server side id generator.

A response to successful call returns the full state of newly created item - all values for all field, and might look something like:

    {
      "id" : "0000001",
      "self" : {
        "href" : "/demo-app/sqldata/users/0000001"
      },
      "user_id" : "0000001",
      "nick" : "rabbit",
      "last_login": 0,
      "messages" : [ ],
      "rooms" : [ ]
    }

In this case we assume there are two additional tables present in our database with foreign key constraint referencing the 'users' table. The ids of those tables are used to name a 'synthetic' collection fields 'messages', and 'rooms'. This is how many-to-one relationships are automatically resolved.

Since this item was only just created it can't possibly have any references pointing to it yet, therefore these two fields are empty.


We can also create the related one-to-many items with one single JSON message, by nesting them:


    $ curl -v -X POST 'http://localhost:8080/demo-app/sqldata/users' -T - << EOF
    
    {                                                             
      'id': '0000003',
      'nick': 'TheQueen',
      'last_login': 0,
      'messages': [ {
        'id': 'msg00000001',
        'room': {
          'self': {
            'href': '/demo-app/sqldata/rooms/00001'
          }
        },
        'content': 'Hello ...'
      }, {
        'id': 'msg00000002',
        'room': {
          'self': {
            'href': '/demo-app/sqldata/rooms/00001'
          }
        },
        'content': 'Hello again ...'
      } ]
    }
    
    EOF

When creating related items using nesting the foreign key columns connecting master-child tables will be filled automatically, so in this case there is no need to specify in message instance a back link to the wrapping user instance via 'user' field on the message.

Also notice how we 'room' within a message we only specify a 'self' / 'href' field. This is called a _resource reference_. Table 'messages' has a many-to-one relationship with 'rooms', which means that 'messages' table contains a foreign key column 'room_id' that links to table 'rooms'.

Currently creating instances related via many-to-one relationship is not supported. Support for this may be added in the future.



Updating records (items)
========================

PUT http://localhost:8080/demo-app/sqldata/TABLEID/ITEMID

    curl -v -X PUT 'http://localhost:8080/demo-app/sqldata/users/0000001' -T - << EOF
    {
      "id" : "0000001",
      "nick" : "MadHatter",
      "last_login": 0
    }


All column fields of the item are always updated, if a field for a column is not present it's assumed the new value is null.

That goes for foreign key columns as well, which are represented as synthetic fields for many-to-one relationships. 

The exception is 'self' / 'href' which is not a column field, or primary key column field ('user_id' in our case) which is always calculated from 'id'.

Synthetic fields for one-to-many relationships with other tables may be omitted in which case they will not be updated. That allows performing an update of a single (master) table only.

If one-to-many synthetic fields are present, they will be updated.

For example:

    curl -v -X PUT 'http://localhost:8080/demo-app/sqldata/users/0000001' -T - << EOF
    {
      "id" : "0000001",
      "nick" : "MadHatter",
      "last_login": 0,
      "messages" : [ ]
    }

That would result in deletion of all the rows in 'messages' table where foreign key column references the user with id '0000001'. 


Response of a successful PUT call is a full state instance, as if performing a GET afterwards.



Deleting records (items)
========================

DELETE http://localhost:8080/demo-app/sqldata/TABLEID/ITEMID

    curl -v -X DELETE 'http://localhost:8080/demo-app/sqldata/users/0000001'


Delete operation will automatically cascade delete any items that link to the item being deleted.

For the future an option could be added to instead of deleting the dependent records the referring foreign key columns would be set to null on referring records.



Retrieving data
===============

Getting individual item
-----------------------

GET http://localhost:8080/demo-app/sqldata/TABLEID/ITEMID

    curl -v 'http://localhost:8080/demo-app/sqldata/users/0000001'


This is the most basic REST operation. It returns a full state instance, including column fields and synthetic fields. By default only resource references are returned for synthetic fields. To expand them use 'fields' parameter:

    curl -v 'http://localhost:8080/demo-app/sqldata/users/0000001?fields=*(*)'



Listing items in the collection
-------------------------------

GET http://localhost:8080/demo-app/sqldata/TABLEID

    curl -v 'http://localhost:8080/demo-app/sqldata/users'

When listing items, only identity information for children is returned by default:

    {
      "id" : "users",
      "self" : {
        "href" : "/demo-app/sqldata/users"
      },
      "links" : [ {
        "rel" : "schema",
        "href" : "/demo-app/sqldata/users;schema"
      } ],
      "count" : 1,
      "type" : "collection",
      "members" : [ {
        "id" : "0000001",
        "self" : {
          "href" : "/demo-app/sqldata/users/0000001"
        }
      } ]
    }

To expand them use 'fields' parameter:

    curl -v 'http://localhost:8080/demo-app/sqldata/users?fields=*(*)'


By default the number of members returned is automatically limited. To take control of that use 'limit' parameter in combination with 'offset' parameter as explained later in [Pagination](GUIDE.md#pagination) chapter.

You can control sorting by using 'sort' parameter as explained in [Sorting](GUIDE.md#sorting) chapter.

And you can control which fields to return as we will explain now.



Controlling which fields to return
----------------------------------

Individual fields can be filtered out by using 'fields' query parameter. For example, we can return all fields except a synthetic field 'messages':

    curl -v 'http://localhost:8080/demo-app/sqldata/users/0000001?fields=*,-messages'

You can target fields in nested items:

    curl -v 'http://localhost:8080/demo-app/sqldata/users/0000001?fields=*,messages(*,-user)'
    
This example will return 'messages' list field but for each instance returned it would suppress a 'user' field which links back to the wrapping user instance. 

'fields' parameter is also used for controlling expansion of resource references. Symbol '*' at any level means - return all column fields, and only return resource references for synthetic fields. To expand those in full another '(*)' has to be appended, and that can be nested ad-infinitum.

There are two fields that currently can't be filterer out using this method - these are 'id', and 'self'.
That may be addressed in the future.


   
Querying
--------

GET http://localhost:8080/demo-app/sqldata/TABLEID?q=QUERY


PgSql endpoint supports a basic subset of [Mongo DB query syntax](http://docs.mongodb.org/manual/reference/operator/query), which uses JSON to express a query.

Currently the supported operators are: $gt, $gte, $lt, $lte, $ne, $or, $and, and $not.

Some examples of queries (note the need to url-encode the value of 'q' parameter):

    curl -v -G 'http://localhost:8080/demo-app/sqldata/messages' --data-urlencode 'q={"user.nick": "MadHatter"}'

That would return all messages from user 'MadHatter'. Note that 'user' field on 'message' is a synthetic field derived from a foreign key column 'user_id' that points to another table. Column 'nick' is in 'users' table, not in 'messages' table. A join select is performed in the background to execute this query.

Another example using multiple condition:

    curl -v -G 'http://localhost:8080/demo-app/sqldata/messages' --data-urlencode 'q={"user.nick": "MadHatter", create_time: {$gt: "2014-08-30"}}'

That would further limit the result to those messages that are also fresher than the specified date.

We can use $or, and $not, and make a more complex query:

    curl -v -G 'http://localhost:8080/demo-app/sqldata/messages' --data-urlencode 'q={$not: {$or: [{"user.nick": "MadHatter"}, {create_time: {$lt: "2014-08-30"}}]}}'

That would return all messages not from 'MadHatter', and not older than a specified date.



Sorting
-------
  
Query parameter 'sort' can be used to control the ordering of items in the result. It has a very simple syntax - a comma separated list of field names. If order is to be descending the field name has to be prefixed with a '-'.

For example:

    curl -v 'http://localhost:8080/demo-app/sqldata/messages?sort=-create_time'

That would return messages starting with latest.


Currently only targeted collection column fields can be specified for sorting. The following would work:

    ?sort=user.nick


  
Pagination
----------
  
There are two query parameters that control pagination.

Use 'limit' parameter to set the maximum number of members to be returned. 

Use 'offset' parameter to skip first N items in the result.

Combining both allows paging e.g.:

    curl -v 'http://localhost:8080/demo-app/sqldata/messages?sort=create_time&offset=100&limit=100'




Bulk operations
===============

PgSql provides a special '_batch' endpoint which allows directly using previous responses as inputs to create, update, and delete operations.

For example, you can list all messages:

    curl -v 'http://localhost:8080/demo-app/sqldata/messages'

    {
      "id" : "messages",
      "self" : {
        "href" : "/demo-app/sqldata/messagess"
      },
      "links" : [ {
        "rel" : "schema",
        "href" : "/demo-app/sqldata/messages;schema"
      } ],
      "count" : 2,
      "type" : "collection",
      "members" : [ {
        "id" : "msg00000001",
        "self" : {
          "href" : "/demo-app/sqldata/messages/msg00000001"
        },
        "user" : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
        "create_time" : 1407770105000,
        "content" : "Hello",
        "room"  : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
      }, {
        "id" : "msg00000002",
        "self" : {
          "href" : "/demo-app/sqldata/messages/msg00000002"
        },
        "user" : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
        "create_time" : 1407770115000,
        "content" : "What\'s up",
        "room"  : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
      }]
    }


Then drop 'messages' table, recreate it, and POST the above response as body to '_batch' endpoint:

    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=create' -T - << EOF
    
    ... body ...
    
    EOF

That would re-create all the messages.

Any top level field other than 'members' is ignored by '_batch' endpoint.

Four different actions are supported: _create_, _update_, _merge_, and _delete_.


Checking for errors
-------------------

Batch endpoint processes items one at a time, and generates success report for each one. When creating or dropping tables, the items may be reordered to avoid constraint violation errors.

Statuses are returned in response as members in the same order they were executed.

Each item in the response contains a 'self' / 'href' field, and an 'id'.

In case of an error, a field called 'error-type' is present on the item, with optional presence of 'message', and 'cause' fields.

HTTP response itself will always return a status 200 OK, it is then up to the client to iterate over response members and make sure none has 'error-type' set on it.



Creating tables
---------------

Multiple table schema definitions in a JSON format can be concatenated into a 'members' JSON array, and posted to '_batch' endpoint. 

    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=create' -T - << EOF
    
    {
      "members": [{
        "id" : "rooms;schema",
        "self" : {
          "href" : "/demo-app/sqldata/rooms;schema"
        },

        ... as returned by GET /demo-app/sqldata/rooms;schema ...

      }, {
        "id" : "users;schema",
        "self" : {
          "href" : "/demo-app/sqldata/users;schema"
        },

        ... as returned by GET /demo-app/sqldata/users;schema ...

      }, {
        "id" : "messages;schema",
        "self" : {
          "href" : "/demo-app/sqldata/messages;schema"
        },

        ... as returned by GET /demo-app/sqldata/messages;schema ...

      }]
    }
    EOF

The order in which tables are created may be modified to avoid unnecessary dependency errors.



Dropping tables
---------------

Same as for create - multiple table schema definitions can be concatenated into a JSON array and passed as a 'members' field to '_batch' endpoint.

Each item can also contain only a resource reference to the table:

    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=delete' -T - << EOF
    
    {
      "members": [{
        "self" : {
          "href" : "/demo-app/sqldata/rooms"
        }
      },{
        "self" : {
          "href" : "/demo-app/sqldata/users"
        }
      },{
        "self" : {
          "href" : "/demo-app/sqldata/messages"
        }
      }]

    }
    EOF


The order in which tables are dropped may be modified to avoid unnecessary dependency errors.


Creating items
--------------

Multiple items from multiple collections can be concatenated into a JSON array and passed as a 'members' field to '_batch' endpoint.

They will be processed one by one. If some item fails to be created the result status for this item will contain 'error-type' field to communicate an error. If processing of some item fails, that does not cause an abort of the whole batch - the processing will continue with the next item.

If an item with the same id exists already that will result in error for that item.

Every item has to have 'self' / 'href' present as it is this uri() that uniquely identifies an item.

    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=create' -T - << EOF
    
    {
      "members": [{
        "id" : "0000001",
        "self" : {
          "href" : "/demo-app/sqldata/users/0000001"
        },
        "user_id" : "0000001",
        "nick" : "MadHatter",
        "last_login": 0,
        "messages" : [ ],
        "rooms" : [ ]
      }, {
        "id" : "room00001",
        "self" : {
          "href" : "/demo-app/sqldata/rooms/room00001"
        },
        "name" : "My Room",
        "owner": {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        }
      }, {
        "id" : "msg000001",
        "self" : {
          "href" : "/demo-app/sqldata/messages/msg000001"
        },
        "user" : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
        "create_time" : 1407770105000,
        "content" : "Hello",
        "room" : {
          "self" : {
            "href" : "/demo-app/sqldata/rooms/room00001"
          }
        }
      }]
    }
    EOF


One-to-many child items can be included as full state child items into the wrapping item, and they will be created.

It is up to a caller to ensure that any dependencies of each item are fulfilled by the time it's that item's turn to be processed.

In the above example we first create a new user, then create a new room that refers to just created user, and finally we create a message that refers to both user, and room that were just created.

If ordering of these three items was any different there would be dependency errors during processing.



Updating items
--------------

Items can be bulk updated as well. Like with bulk create they are processed one by one, and if record does not exist for specific item id, the processing for that item will fail.

Update rules are the same as when updating individual items with PUT against an item's uri.

Items are updated in full, and if one-to-many dependent items are embedded with their full state, they will be updated in full as well - no longer referenced items will be deleted, previously unreferenced items will be created anew, alredy existing items will be updated.

Other rules of processing are the same as for creating items.

    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=update' -T - << EOF
    
    {
      "members": [{
        "id" : "room00001",
        "self" : {
          "href" : "/demo-app/sqldata/rooms/room00001"
        },
        "name" : "Queen\'s Room",
        "owner": {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000003"
          }
        }
      }, {
        "id" : "msg000001",
        "self" : {
          "href" : "/demo-app/sqldata/messages/msg000001"
        },
        "user" : {
          "self" : {
            "href" : "/demo-app/sqldata/users/0000001"
          }
        },
        "create_time" : 1407770105000,
        "content" : "Hello (edited)",
        "room" : {
          "self" : {
            "href" : "/demo-app/sqldata/rooms/room00001"
          }
        }
      }]
    }

In the above example we rename My Room, and edit one message. Since only full updates are supported we have to send full item state even if we only want to update a single field.

In the future we may introduce support for partial updates.



Merging items (Upsert)
----------------------

With batch create items that exist already will generate an error status, similarly with batch update the items that don't yet exist will fail. Often times we simply want to establish a new data state regardless of the current state. If an item exists it should be updated, if doesn't exist it should be created.

To get this kind of behavior use 'merge' action. Posted body should be the same as for batch create.


    curl -v -X POST 'http://localhost:8080/demo-app/sqldata/_batch?action=merge' -T - << EOF
    
    {
      "members": [{
      
         ... full state item ...
      
      }, {
      
         ... full state item ...
      
      }]
    }



Miscellaneous
=============


Type support
------------

Basic types that have been tested to work are:

 * char, varchar
 * integer, int4, int8
 * bool
 * text
 * timestamp

Other types have not been tested.

There is no support for blobs, there is no special usage of json or jsonb types.



Transactions
------------

Currently there is no transaction management support in PgSql endpoint. All transactions are in autocommit mode. 

This prevents a possibility of any transactional deadlocks on the database - since we can't control the order in which users access the data those would otherwise be a real danger.

We can still perform transactionally safe single table updates - all the columns in a single table are updated together in one transaction.

What we don't have is transactions spanning multiple tables.

In a real world there are many usecases where some data inconsistency can be tolerated. To some extent it can also be addressed by client code itself, or by using server side business logic callbacks.

For example, imagine that we have an order containing order items. We have at least 'orders', and 'items' tables. But must likely also 'addresses', and 'products' tables.

Basic data constraints are enforced at the level of primary keys / foreign keys / unique constraints.

But what can be done if for an order with two items a record is created in 'orders', and one in 'items', while another insert into 'items' fails?

One option would be to introduce to 'orders' table a boolean column called 'confirmed' or something similar with default value false, and after initial order and items creation perform an order update of 'confirmed' field - setting it to true.

All queries that need a consistent view of orders would then have to add a condition:

    GET http://localhost:8080/demo-app/sqldata/orders?q={confirmed:true}

If we refrain from ever changing created orders, then 'confirmed' flag will always reflect transactionally correct state of order and its items.

The need to transactionally edit multiple tables might be circumvented by introducing another table containing the columns that have to be updated in a single step and linking it with the other tables.

At the end of the day approaches like that result in a database that's better structured for high volume access, and we should have less problems scaling the application.

