# Ramp Meters

Select `View ➔ Ramp Meters` menu item

A ramp meter is a traffic signal at an on-ramp which controls the rate of
vehicles entering a freeway.  Typically, one vehicle is permitted to enter for
each green indication displayed on the signal.

The following operations can be performed on a ramp meter:

* Activating and deactivating meter
* Adjusting meter release rate
* Querying meter status
* Querying green counts
* Synchronizing clock
* Configuring time-of-day operation

## Metering Strategies

Each ramp meter can be assigned a _metering strategy_, or **algorithm**.  There
are currently two metering strategies available — **simple** and [density
adaptive].

Simple metering runs the ramp meter at a fixed release rate.  This rate is the
target rate for the period (AM or PM).


[density adaptive]: density_adaptive.html
