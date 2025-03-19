# Hashtags

Many resources include a **notes** field, which may contain one or more
**hashtags**.  This is the `#` (hash) character, followed by a string of
letters (A-Z, a-z) and/or numbers.  They can be used for grouping resources
into categories or districts.

- [Message patterns] can have a **compose** hashtag
- [Message lines] can have a **restrict** hashtag
- [Device actions] use hashtags to associate devices
- [Alert configurations] and [alert messages]
- [Permissions] can restrict access by hashtag

## Dedicated Purpose

Devices can be marked for a dedicated purpose by using _reserved hashtags_.

### Camera

Hashtag     | Purpose
------------|----------------------
#LiveStream | Live-stream available
#Recorded   | Recorded stream

### DMS

Hashtag     | Purpose
------------|---------------------------
#Hidden     | Hidden from user interface
#LaneUse    | [Lane-use] indication
#Parking    | [Parking area] information
#Safety     | Automated safety warning
#Tolling    | [Tolling] price display
#TravelTime | [Travel time] estimation
#VSL        | [Variable speed] advisory / limit
#Wayfinding | Traveller route information


[alert configurations]: alerts.html#dms-hashtags
[alert messages]: alerts.html#alert-messages
[device actions]: action_plans.html#device-actions
[lane-use]: lcs.html
[message lines]: message_patterns.html#message-lines
[message patterns]: message_patterns.html
[parking area]: parking_areas.html
[permissions]: permissions.html
[tolling]: tolling.html
[travel time]: travel_time.html
[variable speed]: vsa.html
