# Incident LCS Deployment

_Lane-use Control Signs_ ([LCS]) indications can be suggested from active
roadway [incidents].  The [lane impact] and distance to the incident are used to
determine which [indications] to put on the signs.

There are three distance thresholds for making suggestions:

Threshold | Distance
----------|------------
short     | 0 to 0.5 mi
medium    | 0.5 to 1.0 mi
long      | 1.0 to 1.5 mi

## Short Distance

For incidents that are a _short_ distance from the LCS, the [indications] are
based only on impact in the same lane as the LCS:

Condition         | Indication
------------------|--------------
lane blocked      | `lane closed`
lane affected     | `use caution`
lane free flowing | `lane open`

## Medium Distance

For _medium_ distance incidents, the conditions are a bit more complicated:

Condition                                  | Indication
-------------------------------------------|--------------
lane not blocked                           | `lane open`
lane blocked; nearest open lane to left    | `merge left`
lane blocked; nearest open lane to right   | `merge right`
lane blocked; open lanes to left and right | `merge both`
all lanes blocked                          | `lane closed`

## Long Distance

It is simpler for _long_ distance:

Condition        | Indication
-----------------|--------------------
lane not blocked | `lane open`
lane blocked     | `lane closed ahead`

## Changeable LCS Indications

For [changeable LCS], the `use caution` indication is used instead of `merge
left`, `merge right`, `merge both` or `lane closed ahead`.


[changeable LCS]: lcs.html#changeable-lcs
[incidents]: incidents.html
[indications]: lcs.html#indications
[lane impact]: incidents.html#lane-impact
[LCS]: lcs.html
