# Stream Switching Protocol (needs a snappy name)

## UDP command

Must contain *exactly* 5 fields, separated by ASCII unit separator (31)

1. Command: "play", "record", or "stop"
2. Camera ID
3. Stream request URI
4. Stream type: "MJPEG", "MPEG4", "H264"
5. Title: ASCII text description

## UDP response

Must contain 1 field

1. Error description, or empty string for ACK
