# Geo Location

A geo location is a record describing the location of a device or other
geographical feature.

`geo_loc` resources are created and deleted automatically with an associated
`resource_n`.  This means there are only two valid API endpoints:

- `GET iris/api/geo_loc/{name}?res={res}`: Get a single object as JSON, with
  the given associated resource type.
- `PATCH iris/api/geo_loc/{name}?res={res}`: Update attributes of one object,
  with JSON

| Access       | Primary           |
|--------------|-------------------|
| 👁️  View      | name, resource\_n |
| 🔧 Configure | roadway, road\_dir, cross\_street, cross\_dir, cross\_mod, landmark, lat, lon |

The `res={res}` query parameter is used for permission checks, and must match
the value of `resource_n` in the record.  It can be one of these values:

| Resource        | [Base Resource] |
|-----------------|-----------------|
| alarm           | controller      |
| beacon          |                 |
| camera          |                 |
| controller      |                 |
| dms             |                 |
| gate\_arm       |                 |
| gps             | controller      |
| parking\_area   |                 |
| r\_node         | detector        |
| ramp\_meter     |                 |
| tag\_reader     | toll\_zone      |
| weather\_sensor |                 |


[base resource]: permissions.html#base-resources
