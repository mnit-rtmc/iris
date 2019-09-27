# Incident LCS Deployment

_Lane-use Control Signs_ ([LCS]) indications can be suggested from active
roadway incidents.  The lane impact and distance to the incident are used to
determine which indications to put on the signs.

There are three distance thresholds for making suggestions:

Threshold | Distance
----------|------------
short     | 0 to 0.5 mi
medium    | 0.5 to 1.0 mi
long      | 1.0 to 1.5 mi

## Short Distance

For incidents in _short_ distance from the LCS, the indications are based on
impact in the same lane as the LCS only:

Indication    | Condition
--------------|-------------
`lane closed` | lane blocked     
`use caution` | lane affected    
`lane open`   | lane free flowing

## Medium Distance

For _medium_ distance incidents, the conditions are a bit more complicated:

Indication    | Condition
--------------|-----------------
`lane open`   | lane not blocked
`merge left`  | lane blocked; nearest open lane to left
`merge right` | lane blocked; nearest open lane to right
`merge both`  | lane blocked; open lanes to left and right
`lane closed` | all lanes blocked

## Long Distance

It is simpler for _long_ distance:

Indication          | Condition
--------------------|-----------------
`lane open`         | lane not blocked
`lane closed ahead` | lane blocked

## Changeable LCS Indications

For [changeable LCS], the `use caution` indication is used instead of `merge
left`, `merge right`, `merge both` or `lane closed ahead`.


[changeable LCS]: lcs.html#changeable-lcs
[LCS]: lcs.html
