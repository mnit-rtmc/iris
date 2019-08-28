## Parking Areas

The number of available spaces in a parking area can be published on the web,
or displayed on a DMS.  A parking area could be a rest area or a parking garage.
The honeybee server can publish parking area information compatible with the
MAASTO truck parking information management system (TPIMS).

A special [corridor](admin_guide.html#road_topology) must be associated with
the parking area in order to allow counting available spaces.  Within that
corridor, detectors with the **parking** lane type will be used.
Lanes 1 (front) and 2 (rear) are reserved for a _head_ parking space.
If applicable, lanes 3 (front) and 4 (rear) are for the _tail_.

### Parking Area Action Tag

A `[pa]` [action tag](admin_guide.html#action_tag) is replaced with the number
of available parking spaces to be displayed on a DMS.  The tag has three
parameters, separated by commas.
 - Parking area ID
 - Text to display if available spaces is below the low threshold
 - Text to display if parking area is closed
