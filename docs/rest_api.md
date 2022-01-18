# REST API

These are rough notes about the IRIS web API.

## Requests

Data requests are split into *public* and *restricted* paths:

- `iris/`: Public data
- `iris/api/`: Restricted data (needs session authentication)
- `iris/api/login`: Authentication endpoint

## Public Data

This data is available publicly, with no authentication required.  These
resources are JSON arrays, fetched using http `GET` requests.

- `iris/camera_pub`: Camera locations and configuration
- `iris/detector`: Vehicle detectors
- `iris/dms_message`: Current DMS messages and status
- `iris/dms_pub`: DMS locations and configuration
- `iris/font`: Bitmapped fonts for DMS
- `iris/graphic`: Graphics for DMS
- `iris/incident`: Currently active incidents
- `iris/sign_config`: DMS sign configurations
- `iris/sign_detail`: DMS sign detail information
- `iris/sign_message`: Active DMS sign messages
- `iris/station_sample`: Vehicle detection station data
- `iris/system_attribute`: System-wide attributes (public only)
- `iris/TPIMS_archive`: Truck parking archive data
- `iris/TPIMS_dynamic`: Truck parking dynamic data
- `iris/TPIMS_static`: Truck parking static data
- `iris/img/`: Images (gif) of active sign messages in `sign_message`

## Restricted Data

There are many restricted resource types, which can be accessed using standard
http methods.  These `{type}` values include: `comm_config`, `comm_link`,
`cabinet_style`, `cabinet`, `controller` and `modem`.

A `Content-Type: application/json` header is included where appropriate.

- `GET iris/api/{type}`: Get a list of all objects of one type, as a JSON array
- `POST iris/api/{type}`: Create a new object of the given type.  Body contains
                          required attributes as JSON
- `GET iris/api/{type}/{name}`: Get one object as JSON
- `PATCH iris/api/{type}/{name}`: Update attributes of one object, with JSON
- `DELETE iris/api/{type}/{name}`: Delete one object

## ETags and Caching

Consider using Etags to avoid mid-air collisions
