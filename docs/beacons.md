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

<details>
<summary>API Resources 🕵️ </summary>

* `iris/beacon_state` (lookup table)
* `iris/api/beacon` (primary)
* `iris/api/beacon/{name}`

| Access       | Primary        | Secondary                   |
|--------------|----------------|-----------------------------|
| 👁️  View      | name, location | geo\_loc                    |
| 👉 Operate   | state          |                             |
| 💡 Manage    | message, notes | preset                      |
| 🔧 Configure | controller     | pin, verify\_pin, ext\_mode |

</details>

## Setup

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
- With [action plans] containing [device actions]


## Events

Whenever a beacon's state changes, a time-stamped [event] record can be stored
in the `beacon_event` table.


[action plans]: action_plans.html
[CBW]: protocols.html#cbw
[device actions]: action_plans.html#device-actions
[Din-Relay]: protocols.html#din-relay
[DMS]: dms.html
[event]: events.html
[MnDOT-170]: protocols.html#mndot-170
[Natch]: protocols.html#natch
[remote]: dms.html#setup
