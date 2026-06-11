# Lane Control Signs (LCS)

Select `View ➔ Lane Use ➔ LCS` menu item

Lane Control Signs (LCS) are traffic-control devices for displaying downstream
lane conditions to motorists.  Typically, one sign is mounted over each lane
as an array.  They can display standard **indications**:

| `#` | Indication                | Symbol
|-----|---------------------------|--------------
|   1 | `Dark`                    | <span style="background:black;border:0.1rem solid gray">    </span>
|   2 | `Lane open`               | <span style="background:black;color:#0f0;border:0.1rem solid gray"> ↓ </span>
|   3 | `Use caution`             | <span style="background:black;color:#ff0;border:0.1rem solid gray"> ⇣ </span>
|   4 | `Lane closed ahead`       | <span style="background:black;color:#ff0;border:0.1rem solid gray"> ✕ </span>
|   5 | `Lane closed`             | <span style="background:black;color:#f00;border:0.1rem solid gray"> ✖ </span>
|   6 | `Merge right`             | <span style="background:black;color:#ff0;border:0.1rem solid gray"> 》</span>
|   7 | `Merge left`              | <span style="background:black;color:#ff0;border:0.1rem solid gray">《 </span>
|   8 | `Must exit right`         | <span style="background:black;color:#fff;border:0.1rem solid gray"> ⤷ </span>
|   9 | `Must exit left`          | <span style="background:black;color:#fff;border:0.1rem solid gray"> ⤶ </span>
|  10 | `HOV / HOT`               | <span style="background:black;color:#fff;border:0.1rem solid gray"> ◊ </span>
|  11 | `Variable speed advisory` | <span style="background:black;color:#ff0;border:0.1rem solid gray"> A </span>
|  12 | `Variable speed limit`    | <span style="background:white;color:black;border:0.1rem solid gray"> L </span>

<details>
<summary>API Resources 🕵️ </summary>

* `iris/lcs_indication` (lookup table)
* `iris/lcs_type` (lookup table)
* `iris/api/lcs` (primary)
* `iris/api/lcs/{name}`

| Access       | Primary                | Secondary              |
|--------------|------------------------|------------------------|
| 👁️  View      | name, location, status | geo\_loc               |
| 👉 Operate   | lock                   |                        |
| 💡 Manage    |                        | shift                  |
| 🔧 Configure | controller, notes      | pin, lcs\_type, preset |

</details>

There are a few different types of LCS:

- Dedicated signs over each lane
- General purpose [DMS] over each lane
- In-pavement LED striping / marking

## Setup

The LCS properties form has setup information.

Field    | Description
---------|---------------------------------------------------
Notes    | administrator notes, possibly including [hashtag]s
LCS Type | dedicated sign, [DMS], in-pavement
Preset   | verification camera [preset]
Shift    | roadway corridor lane shift

An LCS array must also be associated with a [controller], on [IO pin] 2.  For
NTCIP [protocol], any controller for a DMS in the array is allowed.

## LCS States

Each _indication_ that can be displayed on each lane of an LCS array is
represented by an LCS state.

<details>
<summary>API Resources 🕵️ </summary>

* `iris/api/lcs_state` (primary)
* `iris/api/lcs_state/{name}`

| Access       | Primary          | Secondary              |
|--------------|------------------|------------------------|
| 👁️  View      | name, lcs        |                        |
| 💡 Manage    | lane, indication | msg\_pattern, msg\_num |
| 🔧 Configure | controller       | pin                    |

</details>

Each state must be assigned to a separate [IO pin] on a [controller].  The
pin specifies a digital output to activate that state.

For DMS type signs using NTCIP, the pin is not relevant; any number 3+ will
work.  Instead, [message pattern] is used to specify the [MULTI] for the
indication.  _Message number_ (2-65535) can be used to speed up activation,
by caching the message in the controller's message table.


[controller]: controllers.html
[DMS]: dms.html
[hashtag]: hashtags.html
[IO pin]: controllers.html#io-pins
[message pattern]: message_patterns.html
[MULTI]: multi.html
[preset]: cameras.html#presets
[protocol]: protocols.html
