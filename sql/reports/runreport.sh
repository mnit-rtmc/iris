#!/bin/bash
#  IRIS # Intelligent Roadway Information System
#  Copyright (C) 2008  Minnesota Department of Transportation
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
# 
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.

   if [ "$1" = "" ] ; then
      echo " "
      echo "IRIS report generator"
      echo "  Syntax: $0 report-name"
      echo "  e.g. $0 dms_msg_history_basic.sql"
      echo " "
      exit 1
   fi

# this is very basic at present
cat $1 | psql log 

