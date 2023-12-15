# Geo Location

A geo location is a record describing the location of a device or other
geographical feature.

`geo_loc` resources are created and deleted automatically with an associated
`resource_n`.  This means there are only two valid API endpoints:

- `GET iris/api/geo_loc/{name}`: Get a single object as JSON, with *minimal*
  and *full* attributes
- `PATCH iris/api/geo_loc/{name}`: Update attributes of one object, with JSON

| Access       | Minimal          | Full        |
|--------------|------------------|-------------|
| 👁️  View      | name             | resource\_n |
| 🔧 Configure | roadway, road\_dir, cross\_street, cross\_dir, cross\_mod, landmark | lat, lon |
