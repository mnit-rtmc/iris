/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import junit.framework.TestCase;

/** 
 * CRC test cases.
 * Checks from http://reveng.sourceforge.net/crc-catalogue/all.htm
 *
 * @author Doug Lau
 */
public class CRCTest extends TestCase {

	static private final byte[] CHECK = "123456789".getBytes();

	public CRCTest(String name) {
		super(name);
	}

	public void testCRC8() {
		CRC crc = new CRC(8, 0x07, 0x00, false);
		assertTrue(crc.calculate(CHECK) == 0xF4);
	}

	public void testSS125() {
		CRC crc = new CRC(8, 0x1C, 0x00, false);
		assertTrue(crc.calculate(CHECK) == 0xBC);
	}

	public void testCDMA2000() {
		CRC crc = new CRC(8, 0x9B, 0xFF, false);
		assertTrue(crc.calculate(CHECK) == 0xDA);
	}

	public void testDARC() {
		CRC crc = new CRC(8, 0x39, 0x00, true);
		assertTrue(crc.calculate(CHECK) == 0x15);
	}

	public void testDVB_S2() {
		CRC crc = new CRC(8, 0xD5, 0x00, false);
		assertTrue(crc.calculate(CHECK) == 0xBC);
	}

	public void testEBU() {
		CRC crc = new CRC(8, 0x1D, 0xFF, true);
		assertTrue(crc.calculate(CHECK) == 0x97);
	}

	public void testICODE() {
		CRC crc = new CRC(8, 0x1D, 0xFD, false);
		assertTrue(crc.calculate(CHECK) == 0x7E);
	}

	public void testITU() {
		CRC crc = new CRC(8, 0x07, 0x00, false, 0x55);
		assertTrue(crc.calculate(CHECK) == 0xA1);
	}

	public void testMAXIM() {
		CRC crc = new CRC(8, 0x31, 0x00, true);
		assertTrue(crc.calculate(CHECK) == 0xA1);
	}

	public void testROHC() {
		CRC crc = new CRC(8, 0x07, 0xFF, true);
		assertTrue(crc.calculate(CHECK) == 0xD0);
	}

	public void testWCDMA() {
		CRC crc = new CRC(8, 0x9B, 0x00, true);
		assertTrue(crc.calculate(CHECK) == 0x25);
	}

	public void testCRC10() {
		CRC crc = new CRC(10, 0x233, 0x000, false);
		assertTrue(crc.calculate(CHECK) == 0x199);
	}

	public void testCRC10CDMA2000() {
		CRC crc = new CRC(10, 0x3D9, 0x3FF, false);
		assertTrue(crc.calculate(CHECK) == 0x233);
	}

	public void testCRC11() {
		CRC crc = new CRC(11, 0x385, 0x01A, false);
		assertTrue(crc.calculate(CHECK) == 0x5A3);
	}

	public void testCRC12_3GPP() {
		CRC crc = new CRC(12, 0x80F, 0x000, false, true, 0x00);
		assertTrue(crc.calculate(CHECK) == 0xDAF);
	}

	public void testCRC12CDMA2000() {
		CRC crc = new CRC(12, 0xF13, 0xFFF, false);
		assertTrue(crc.calculate(CHECK) == 0xD4D);
	}

	public void testCRC12DECT() {
		CRC crc = new CRC(12, 0x80F, 0x000, false);
		assertTrue(crc.calculate(CHECK) == 0xF5B);
	}

	public void testCRC13BBC() {
		CRC crc = new CRC(13, 0x1CF5, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0x04FA);
	}

	public void testCRC14DARC() {
		CRC crc = new CRC(14, 0x0805, 0x0000, true);
		assertTrue(crc.calculate(CHECK) == 0x082D);
	}

	public void testCRC15() {
		CRC crc = new CRC(15, 0x4599, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0x059E);
	}

	public void testCRC15MPT() {
		CRC crc = new CRC(15, 0x6815, 0x0000, false, 0x0001);
		assertTrue(crc.calculate(CHECK) == 0x2566);
	}

	public void testARC() {
		CRC crc = new CRC(16, 0x8005, 0x0000, true);
		assertTrue(crc.calculate(CHECK) == 0xBB3D);
	}

	public void testAUGCCITT() {
		CRC crc = new CRC(16, 0x1021, 0x1D0F, false);
		assertTrue(crc.calculate(CHECK) == 0xE5CC);
	}

	public void testBuypass() {
		CRC crc = new CRC(16, 0x8005, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0xFEE8);
	}

	public void testCCITT_False() {
		CRC crc = new CRC(16, 0x1021, 0xFFFF, false);
		assertTrue(crc.calculate(CHECK) == 0x29B1);
	}

	public void testCRC16CDMA2000() {
		CRC crc = new CRC(16, 0xC867, 0xFFFF, false);
		assertTrue(crc.calculate(CHECK) == 0x4C06);
	}

	public void testDds110() {
		CRC crc = new CRC(16, 0x8005, 0x800D, false);
		assertTrue(crc.calculate(CHECK) == 0x9ecf);
	}

	public void testDECTR() {
		CRC crc = new CRC(16, 0x0589, 0x0000, false, 0x0001);
		assertTrue(crc.calculate(CHECK) == 0x007E);
	}

	public void testDECTX() {
		CRC crc = new CRC(16, 0x0589, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0x007F);
	}

	public void testDNP() {
		CRC crc = new CRC(16, 0x3D65, 0x0000, true, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0xEA82);
	}

	public void testEN13757() {
		CRC crc = new CRC(16, 0x3D65, 0x0000, false, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0xC2B7);
	}

	public void testGENIBUS() {
		CRC crc = new CRC(16, 0x1021, 0xFFFF, false, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0xD64E);
	}

	public void testCRC16MAXIM() {
		CRC crc = new CRC(16, 0x8005, 0x0000, true, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0x44C2);
	}

	public void testMCRF4XX() {
		CRC crc = new CRC(16, 0x1021, 0xFFFF, true);
		assertTrue(crc.calculate(CHECK) == 0x6F91);
	}

	public void testT10DIF() {
		CRC crc = new CRC(16, 0x8BB7, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0xD0DB);
	}

	public void testTELEDISK() {
		CRC crc = new CRC(16, 0xA097, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0x0FB3);
	}

	public void testUSB() {
		CRC crc = new CRC(16, 0x8005, 0xFFFF, true, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0xB4C8);
	}

	public void testKERMIT() {
		CRC crc = new CRC(16, 0x1021, 0x0000, true);
		assertTrue(crc.calculate(CHECK) == 0x2189);
	}

	public void testMODBUS() {
		CRC crc = new CRC(16, 0x8005, 0xFFFF, true);
		assertTrue(crc.calculate(CHECK) == 0x4B37);
	}

	public void testX25() {
		CRC crc = new CRC(16, 0x1021, 0xFFFF, true, 0xFFFF);
		assertTrue(crc.calculate(CHECK) == 0x906E);
	}

	public void testXMODEM() {
		CRC crc = new CRC(16, 0x1021, 0x0000, false);
		assertTrue(crc.calculate(CHECK) == 0x31C3);
	}

	public void testCRC24() {
		CRC crc = new CRC(24, 0x864CFB, 0xB704CE, false);
		assertTrue(crc.calculate(CHECK) == 0x21CF02);
	}

	public void testFLEXRAY_A() {
		CRC crc = new CRC(24, 0x5D6DCB, 0xFEDCBA, false);
		assertTrue(crc.calculate(CHECK) == 0x7979BD);
	}

	public void testFLEXRAY_B() {
		CRC crc = new CRC(24, 0x5D6DCB, 0xABCDEF, false);
		assertTrue(crc.calculate(CHECK) == 0x1F23B8);
	}
}
