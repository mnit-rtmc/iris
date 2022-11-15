## Beacons

A beacon is a light or set of lights that flashes toward oncoming traffic.
Sometimes called _flashers_ or _wig-wags_, their purpose is to draw attention to
a [DMS] or static sign.

### Setup

Select `View ➔ Message Signs ➔ Beacons` menu item

Beacons can be connected to controllers using one of these protocols:
- [CBW]
- [Din-Relay]
- [MnDOT-170]
- [Natch]

These fields are available for configuring a beacon:

| Field      | Description                              |
|------------|------------------------------------------|
| Message    | Static text message on sign              |
| Notes      | Administrative notes                     |
| Pin        | Controller output pin to activate beacon |
| Verify Pin | Verify circuit I/O pin                   |
| Ext Mode   | External detect verify mode              |

**Verify Pin** is a digital input to sense whether the lights are activated, for
beacons which have verify circuitry.  For the [CBW] protocol, it is also an
output to energize the verify circuit.  **Pin** and **Verify Pin** can be the
same.

**Ext Mode** determines the state reported when a verify is detected without
being commanded:
- false: `Fault: Stuck On`
- true: `Flashing: Ext` (use when an external system can control the beacon)

### State

Beacons can be controlled in a few ways:
- Changing state manually through the user interface
- Deploying or blanking DMS with a [remote] beacon
- Activating a ramp meter with an advance warning beacon
- With [action plans] containing [beacon actions]

A beacon can be in one of these states:

| State            | Description                                   |
|------------------|-----------------------------------------------|
| Unknown          | State not known due to communication error    |
| Dark             | Not flashing                                  |
| Flashing         | Lights flashing, commanded by IRIS            |
| Flashing: Ext    | Lights flashing, commanded by external system |
| Fault: Stuck On  | Lights flashing, but not commanded            |
| Fault: No Verify | Flashing commanded, but not verified          |


### Events

Whenever a beacon's state changes, a time-stamped record is added to the
`beacon_event` table.  These records are purged automatically when older than
the value of the `beacon_event_purge_days` [system attribute].


[action plans]: action_plans.html
[beacon actions]: action_plans.html#beacon-actions
[CBW]: comm_links.html#cbw
[Din-Relay]: comm_links.html#din-relay
[DMS]: dms.html
[MnDOT-170]: comm_links.html#mndot-170
[Natch]: comm_links.html#natch
[NTCIP]: comm_links.html#ntcip
[remote]: dms.html#setup
[system attribute]: system_attributes.html
