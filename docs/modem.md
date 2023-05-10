# Modems

Select `View ➔ Maintenance ➔ Modems` menu item

Modems can be used to connect [comm link]s to a *plain-old telephone system*
(POTS).  The URI of the comm link must use the `modem://` scheme, with a
phone number instead of IP address.

## Resources

* `iris/api/modem`
* `iris/api/modem/{name}`

Attribute [permissions]:

| Access       | Minimal    | Full        |
|--------------|------------|-------------|
| Read Only    | name       |             |
| 💡 Plan      | enabled    | timeout\_ms |
| 🔧 Configure |            | uri, config |


[comm link]: comm_links.html
[permissions]: permissions.html
