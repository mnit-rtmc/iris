For some unknown reason, CBW devices are all designed to be incompatible with
each other.  There are at least __four__ variations of XML elements to report
relay states.  Additionally, some responses (for `state.xml`) are not valid
HTTP, because why not?

## X-301, X-310, X-WR-10R12, XRDI-WRQ-LS

- Relay: `<relay1state>1</relay1state>`
- Input: `<input2state>0</input2state>`
- Control: `state.xml?relay1State=1`

## X-WR-1R12

- Invalid HTTP with `state.xml`; must use `stateFull.xml`
- Relay: `<relaystate>0</relaystate>`
- Input: `<inputstate>1</inputstate>`
- Control: `stateFull.xml?relayState=1`

## X-401

- Relay: `<relay2>0</relay2>`
- Input: `<digitalInput1>0</digitalInput1>`
- Control: `state.xml?digitalIO2=0`

## X-410

- Relay: `<relay2>0</relay2>`
- Input: `<digitalInput1>0</digitalInput1>`
- Control: `state.xml?relay1=0`

## X-332

- Relay: `<relaystates>0000000000000000</relaystates>` (relays 16,15,...,1)
- Input: `<inputstates>000000000000000000</inputstates>` (inputs 18,17,...,1)
- Control: `state.xml?relay2State=0`
