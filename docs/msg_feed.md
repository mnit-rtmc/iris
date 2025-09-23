## Msg-Feed

The `msgfeed` protocol can be used to interface with an external system that
generates [DMS] messages.

The external system should respond with an ASCII text file, with one line per
message to be deployed.  Each line contains 3 fields: `dms`, `message` and
`expire`, separated by tab characters `\t` (ASCII 0x09), and terminated with a
single newline character `\n` (ASCII 0x0A).

```
V66E37\tSNOW PLOW[nl]AHEAD[nl]USE CAUTION\t2022-10-02 11:37:00-05:00
```

`dms`: Name of the sign to deploy, which must have the [hashtag] referenced
by a [device action].  Additionally, that action must be associated with the
current phase of an active [action plan].  The [message pattern] of the
_device action_ must be a `feed` [action tag].  For example, if the `msgfeed`
_Comm Link_ name is `XYZ`, then the pattern must be `[feedXYZ]`.

`multi`: Message to deploy, using the [MULTI] markup language.  Each line of
the message must exist in the pattern's library.  This check allows only
"administrator-approved" messages, but it can be disabled by changing the
`msg_feed_verify` [system attribute] to `false`.  **WARNING**: only disable
this check if the message feed host is fully trusted, and there is no
possibility of man-in-the-middle attacks.

`expire`: Date/time when the message will expire, using [RFC 3339]
`full-date` / `full-time` separated by a space.  The message will not be
displayed after this time.  Leave `expire` blank to cancel a previous message.


[action plan]: action_plans.html
[action tag]: action_plans.html#action-tags
[comm link]: comm_links.html
[device action]: action_plans.html#device-actions
[DMS]: dms.html
[hashtag]: hashtags.html
[message pattern]: message_patterns.html
[MULTI]: multi.html
[RFC 3339]: https://tools.ietf.org/html/rfc3339#section-5.6
[system attribute]: system_attributes.html
