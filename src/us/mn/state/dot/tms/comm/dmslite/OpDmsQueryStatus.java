/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */



package us.mn.state.dot.tms.comm.dmslite;

//~--- non-JDK imports --------------------------------------------------------

import us.mn.state.dot.tms.DMSImpl;

//import us.mn.state.dot.tms.StatusTable;
import us.mn.state.dot.tms.comm.AddressedMessage;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * This operation queries the status of a DMS. This includes temperature and
 * failure information.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpDmsQueryStatus extends OpDms {

    /** Skyline power supply status table columns */
    static protected final String[] SKYLINE_POWER_COLUMNS = { "Power Supply", "Status" };

    /** Short Error status */

    // protected final ShortErrorStatus shortError = new ShortErrorStatus();

    /** Create a new DMS query status object */
    public OpDmsQueryStatus(DMSImpl d) {
        super(DEVICE_DATA, d);
    }

    /** Create the first real phase of the operation */
    protected Phase phaseOne() {
        return new QueryBrightness();
    }

    /** Cleanup the operation */
    public void cleanup() {
        if (success) {
            m_dms.notifyUpdate();
        }

        super.cleanup();
    }

    /** Phase to query the DMS ambient temperature status */
    protected class AmbientTemperature extends Phase {

        /** Query the DMS ambient temperature */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           TempMinAmbient min_amb = new TempMinAmbient();
             *           mess.add(min_amb);
             *           TempMaxAmbient max_amb = new TempMaxAmbient();
             *           mess.add(max_amb);
             */
            try {

                // mess.getRequest();
                m_dms.setMinAmbientTemp(-100);        // min_amb.getInteger());
                m_dms.setMaxAmbientTemp(200);         // max_amb.getInteger());
            } catch (IllegalStateException ex) {    // FIXME: made this exception up

                // Ledstar has no ambient temp objects
            }

            return new HousingTemperature();
        }
    }


    /** Phase to query the DMS controller temperature status */
    protected class ControllerTemperature extends Phase {

        /** Query the DMS controller temperature */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           TempMinCtrlCabinet min_cab = new TempMinCtrlCabinet();
             *           mess.add(min_cab);
             *           TempMaxCtrlCabinet max_cab = new TempMaxCtrlCabinet();
             *           mess.add(max_cab);
             *           mess.getRequest();
             */
            m_dms.setMinCabinetTemp(-10);    // min_cab.getInteger());
            m_dms.setMaxCabinetTemp(200);    // max_cab.getInteger());

            return new AmbientTemperature();
        }
    }


    /** Phase to query the DMS failure status */
    protected class Failures extends Phase {

        /** Query the DMS failure status */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           mess.add(shortError);
             * /                      DmsStatDoorOpen door = new DmsStatDoorOpen();
             * /                      mess.add(door);
             *           PixelFailureTableNumRows rows =
             *                   new PixelFailureTableNumRows();
             *           mess.add(rows);
             *           mess.getRequest();
             */

            // m_dms.setErrorStatus(shortError);  FIXME: call this?
//          DMS_LOG.log(m_dms.getId() + ": " + door);
            // m_dms.setPixelFailureCount(rows.getInteger());    FIXME: call this?
            return new MoreFailures();
        }
    }


    /** Phase to query the DMS housing temperature status */
    protected class HousingTemperature extends Phase {

        /** Query the DMS housing temperature */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           TempMinSignHousing min_hou = new TempMinSignHousing();
             *           mess.add(min_hou);
             *           TempMaxSignHousing max_hou = new TempMaxSignHousing();
             *           mess.add(max_hou);
             *           mess.getRequest();
             */
            m_dms.setMinHousingTemp(-200);    // min_hou.getInteger());  FIXME
            m_dms.setMaxHousingTemp(200);     // max_hou.getInteger());  FIXME

            return new Failures();
        }
    }


    /** Phase to query Ledstar-specific status */
    protected class LedstarStatus extends Phase {

        // protected final LedLdcPotBase potBase = new LedLdcPotBase();
        // protected final LedPixelLow low = new LedPixelLow();
        // protected final LedPixelHigh high = new LedPixelHigh();
        // protected final LedBadPixelLimit bad = new LedBadPixelLimit();

        /** Query Ledstar-specific status */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           mess.add(potBase);
             *           mess.add(low);
             *           mess.add(high);
             *           mess.add(bad);
             *           try { mess.getRequest(); }
             *           catch(SNMP.Message.NoSuchName e) {
             *                   return new SkylineStatus();
             *           }
             */

            // FIXME not sure if these should be set
            // m_dms.setLdcPotBase(potBase.getInteger(), false);
            // m_dms.setPixelCurrentLow(low.getInteger(), false);
            // m_dms.setPixelCurrentHigh(high.getInteger(), false);
            // m_dms.setBadPixelLimit(bad.getInteger(), false);
            return null;
        }
    }


    /** Phase to query more DMS failure status */
    protected class MoreFailures extends Phase {

        /** Query more DMS failure status */
        protected Phase poll(AddressedMessage mess) throws IOException {

            /*
             *           LampFailureStuckOff l_off = new LampFailureStuckOff();
             *           LampFailureStuckOn l_on = new LampFailureStuckOn();
             *           if(shortError.checkError(ShortErrorStatus.LAMP)) {
             *                   mess.add(l_off);
             *                   mess.add(l_on);
             *           }
             *           FanFailures fan = new FanFailures();
             *           if(shortError.checkError(ShortErrorStatus.FAN))
             *                   mess.add(fan);
             *           ControllerErrorStatus con = new ControllerErrorStatus();
             *           if(shortError.checkError(ShortErrorStatus.CONTROLLER))
             *                   mess.add(con);
             *           if(shortError.checkError(ShortErrorStatus.LAMP |
             *                   ShortErrorStatus.FAN |
             *                   ShortErrorStatus.CONTROLLER))
             *           {
             *                   mess.getRequest();
             *           }
             */

/*          FIXME execute these?
                                 String lamp = l_off.getValue();
                                 if(lamp.equals("OK"))
                                         lamp = l_on.getValue();
                                 else if(!l_on.getValue().equals("OK"))
                                         lamp += ", " + l_on.getValue();
                                 m_dms.setLampStatus(lamp);
                                 m_dms.setFanStatus(fan.getValue());
*/

            // DMS_LOG.log(m_dms.getId() + ": " + con);
            return new LedstarStatus();
        }
    }


    /** Phase to query the brightness status */
    protected class QueryBrightness extends Phase {

        /** Query the DMS brightness status */
        protected Phase poll(AddressedMessage mess) throws IOException {
            return null;    // FIXME

            /*
             *           DmsIllumPhotocellLevelStatus p_level =
             *                   new DmsIllumPhotocellLevelStatus();
             *           mess.add(p_level);
             *           DmsIllumBrightLevelStatus b_level =
             *                   new DmsIllumBrightLevelStatus();
             *           mess.add(b_level);
             *           DmsIllumLightOutputStatus light =
             *                   new DmsIllumLightOutputStatus();
             *           mess.add(light);
             *           DmsIllumControl control = new DmsIllumControl();
             *           mess.add(control);
             *           mess.getRequest();
             */

/*          FIXME not sure if these should be called
                                 m_dms.setPhotocellLevel(p_level.getInteger());
                                 m_dms.setBrightnessLevel(b_level.getInteger());
                                 m_dms.setLightOutput(light.getInteger());
                                 if(control.isManual())
                                         m_dms.setManualBrightness(true);
                                 else {
                                         m_dms.setManualBrightness(false);
                                         if(!control.isPhotocell())
                                                 DMS_LOG.log(m_dms.getId() + ": "+control);
                                 }
                                 return new ControllerTemperature();
*/
        }
    }
}
