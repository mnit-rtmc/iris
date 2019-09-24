## Alarms

Select `View ➔ Maintenance ➔ Alarms` menu item

An alarm is a simple device triggered by an event.  The event might be an
equipment failure, high temperature, or low voltage in a field controller.
Alarms can be monitored to help maintain field devices.  Device drivers trigger
alarms based on field controller states, information read from controllers
(_e.g._ voltages), _etc_.

Users can view the status of alarms on the alarm form.  The `Status` column
indicates the state of each alarm, which can be **Clear** or **Triggered**.

Alarms are created using the Alarm Form and can only be created for
controllers using a protocol that generates alarms, such as [MnDOT-170].


[MnDOT-170]: admin_guide.html#mndot170
