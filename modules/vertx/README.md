# JSON Resource Interchange Format

This document describes the expected message format for REST
messages via asynchronous JSON messages over the event bus.
Got that?

Each request should contain an `action` property, specifying
one of `create`, `read`, `update`, or `delete` as literal string
values. Additional expected parameters are typically required
depending on the `action` requested.

## Read a Resource

To read a single resource, provide the resource `id` and specify
the `read` action.

    {
      'action': 'read',
      'id': <id>
    }

A successful response will contain a shallow copy of the object
(depth 1).

    {
      'status': 200,
      'type':'object',
      'state':
        {
          'id': <object_id>
          'name': <property_value>
          'employees': [
            { id: <object_id> }
            ]
        }
    }

## Read a Resource Collection

To read an entire resource collection, specify the `read` action, but
do not provide an `id` property. You may optionally provide a `filter`
property.

By default a response will contain an array of resources with only an 
`id` property (and maybe URL?). This is depth 0. To include all non-reference
properties (strings, numbers, etc), set to depth 1. Any nested resource
objects should be returned as `{'id':<id>}` objects. To inline those objects,
specify a depth of 2. And so on.

    {
      'action': 'read',
      'filter': {
        <field_name>: {
          'value': <literal_value>,
          'regex': <regex_string>
        }
      },
      'page': <N>, // Default 0
      'depth': <N> // Default 0
    }

A successful response for a collection read will look something like this.
Note, the `resources` property contains an array of objects with only the
object `id`.

    {
      'status': 200,
      'type': 'collection',
      'depth': <N>, // Default 0
      'size': <record_count>,
      'page': <page_number>, // align with the collection paging as defined by mbaas
      'resources': [ { 'id': <object_id> } ]
    }

## Update an Object Resource

To update a resource, provide the resource `id` and a hash of changed properties.

    {
      'action': 'update',
      'id': <id>,
      'changes': {
        <property_name>: <property_value>
      }
    }

A successful update will return an `id` response, which can be used to retrieve the
resource again, if necessary..

    {
      'status': 200,
      'type': 'id',
      'id': <id> // ID of the changed resource
    }

## Delete a Resource

To delete a resource, specify the `delete` action and provide the object's `id` value.
If `verbose` is true, also return the resource's last known state. I'm not sure I like
this for a number of reasons.

    {
      'action': 'delete',
      'id': <id>,
      'verbose': <true|false> // defaults to false
    }

A successful delete response.

    {
      'status': 200,
      'type': 'id',
      'id': <id> // ID of the deleted resource
      'state': {
        <property_name>: <property_value>
      }
    }


