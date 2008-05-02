--  IRIS -- Intelligent Roadway Information System
--  Copyright (C) 2008  Minnesota Department of Transportation
-- 
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
-- 
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.

-- Basic DMS message history report.

select 
    sign_status_event.event_id, 
    sign_status_event.event_date, 
    event_description.description, 
    sign_status_event.device_id, 
    sign_status_event.message, 
    tms_user.description 
from sign_status_event, event_description, tms_user 
where 
(event_description.event_desc_id = sign_status_event.event_desc_id) AND 
(tms_user.id = sign_status_event.user_id)
ORDER BY sign_status_event.event_date
;
