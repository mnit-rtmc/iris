# Beacons

A beacon is a light or set of lights that flashes toward oncoming traffic.
Sometimes called _flashers_ or _wig-wags_, their purpose is to draw attention to
a static sign or [DMS].

## Setup

Select `View ➔ Message Signs ➔ Beacons` menu item

A beacon must be connected to a controller using one of these [protocol]s:

| Protocol      | Model      | [IO Pin]s |
|---------------|------------|-----------|
| CBW           |            | _varies_  |
|               | X-WR-1R12  | 1         |
|               | X-301      | 1-2       |
|               | X-401      | 1-2       |
|               | X-310      | 1-4       |
|               | X-410      | 1-4       |
|               | X-WR-10R12 | 1-10      |
|               | X-332      | 1-16      |
| DLI DIN Relay |            | 1-8       |
| MnDOT-170     |            | 2         |
| Natch         |            | 2         |
| NDOT Beacon   |            | 1         |

<details>
<summary>API Resources 🕵️ </summary>

* `iris/beacon_state` (lookup table)
* `iris/api/beacon` (primary)
* `iris/api/beacon/{name}`

| Access       | Primary                   | Secondary                   |
|--------------|---------------------------|-----------------------------|
| 👁️  View      | name, location            | geo\_loc                    |
| 👉 Operate   | state                     |                             |
| 💡 Manage    | message                   | preset                      |
| 🔧 Configure | controller, device, notes | pin, verify\_pin, ext\_mode |

</details>

**Message** is a static text message on the sign.

**Notes** can be any extra information about the beacon.

**Device**, if specified, is a deivce to which the beacon is attached:
_alarm_, _dms_, _gate arm_, _lcs_, _ramp meter_, or _tag reader_.

**Pin** is the controller output pin to activate beacon.

**Verify Pin** is a digital input to sense whether the lights are activated,
for beacons which have verify circuitry.  For the CBW [protocol], it is also
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
- With [action plans] containing [device actions]
- Deploying the associated **device** (for DMS, the message must be configured
  with "flash beacon" enabled).

## Events

Whenever a beacon's state changes, a time-stamped [event] record can be stored
in the `beacon_event` table.


[action plans]: action_plans.html
[device actions]: action_plans.html#device-actions
[DMS]: dms.html
[event]: events.html
[IO pin]: controllers.html#io-pins
[protocol]: protocols.html
[remote]: dms.html#setup
