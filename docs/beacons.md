# Beacons

Select `View ➔ Message Signs ➔ Beacons` menu item

A beacon is a light or set of lights that flashes toward oncoming traffic.
Sometimes called _flashers_ or _wig-wags_, their purpose is to draw attention to
a static sign or [DMS].

Beacons can be connected to controllers using one of these protocols:
- [CBW]
- [Din-Relay]
- [MnDOT-170]
- [Natch]

## Resources

* `iris/api/beacon`
* `iris/api/beacon/{name}`

Attribute [permissions]:

| Access       | Minimal        | Full                        |
|--------------|----------------|-----------------------------|
| Read Only    | name, location | geo\_loc                    |
| 👉 Operate   | state          |                             |
| 💡 Plan      | message, notes | preset                      |
| 🔧 Configure | controller     | pin, verify\_pin, ext\_mode |

**Message** is a static text message on the sign.

**Notes** can be any extra information about the beacon.

**Pin** is the controller output pin to activate beacon.

**Verify Pin** is a digital input to sense whether the lights are activated,
for beacons which have verify circuitry.  For the [CBW] protocol, it is also
an output to energize the verify circuit.  **Pin** and **Verify Pin** can be
the same.

**Ext Mode** determines the state reported when a verify is detected without
being commanded:
- false: `Fault: Stuck On`
- true: `Flashing: External` (use when an external system can control the
  beacon)

**State** is one of the following values:

| State              | Description                                   |
|--------------------|-----------------------------------------------|
| Unknown            | State not known due to communication error    |
| Dark               | Not flashing                                  |
| Flashing           | Lights flashing, commanded by IRIS            |
| Flashing: External | Lights flashing, commanded by external system |
| Fault: Stuck On    | Lights flashing, but not commanded            |
| Fault: No Verify   | Flashing commanded, but not verified          |

Beacons can be controlled in a few ways:
- Changing state manually through the user interface
- Deploying or blanking DMS with a [remote] beacon
- Activating a ramp meter with an advance warning beacon
- With [action plans] containing [beacon actions]


## Events

Whenever a beacon's state changes, a time-stamped record is added to the
`beacon_event` table.  These records are purged automatically when older than
the value of the `beacon_event_purge_days` [system attribute].


[action plans]: action_plans.html
[beacon actions]: action_plans.html#beacon-actions
[CBW]: protocols.html#cbw
[Din-Relay]: protocols.html#din-relay
[DMS]: dms.html
[MnDOT-170]: protocols.html#mndot-170
[Natch]: protocols.html#natch
[NTCIP]: protocols.html#ntcip
[permissions]: permissions.html
[remote]: dms.html#setup
[system attribute]: system_attributes.html
