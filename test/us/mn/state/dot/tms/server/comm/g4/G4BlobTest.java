/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.util.LinkedList;
import us.mn.state.dot.tms.server.comm.ParsingException;
import junit.framework.TestCase;

/** 
 * Test cases for G4Blob
 * @author Michael Darter
 */
public class G4BlobTest extends TestCase {

	/** constructor */
	public G4BlobTest(String name) {
		super(name);
	}

	/** test methods */
	static public void test() {

		// sample G4 statistics record, with 8 subrecords
		int[] sample_array = new int[] {
			255, 170, 128, 24, 0, 4, 74, 0, 0, 53, 34, 
			25, 2, 4, 9, 2, 18, 7, 56, 0, 30, 135, 0, 
			0, 0, 0, 0, 0, 1, 197, 
			255, 170, 16, 16, 0, 4, 0, 11, 0, 14, 0, 9, 
			0, 13, 0, 19, 0, 5, 0, 8, 0, 83, 
			255, 170, 17, 16, 0, 4, 0, 69, 0, 101, 0, 70, 
			0, 128, 0, 157, 0, 33, 0, 98, 2, 148,
			255, 170, 18, 16, 0, 4, 0, 66, 0, 59, 0, 63, 
			0, 50, 0, 55, 0, 68, 0, 70, 1, 179, 
			255, 170, 20, 16, 0, 4, 0, 3, 0, 2, 0, 2, 0, 
			3, 0, 3, 0, 2, 0, 1, 0, 20, 
			255, 170, 22, 16, 0, 4, 0, 0, 0, 0, 0, 1, 0, 1, 0, 
			1, 0, 0, 0, 1, 0, 8, 
			255, 170, 24, 16, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 1, 0, 5, 
			255, 170, 129, 3, 0, 4, 74, 0, 78};

		try {
			// validate data request
			int station_id = 6;
			G4Blob dr = G4Blob.buildDataRequest(station_id);
			assertTrue(dr.getQualifier() == G4Blob.QUAL_DATAREQ);
			assertTrue(dr.getDataLength() == 
				G4Blob.DATALEN_DATAREQ);
			assertTrue(dr.getChecksum() == station_id);
			assertTrue(dr.isChecksumValid());

			// validRec
			G4Blob rec = new G4Blob(sample_array);
			assertFalse(rec.validRec(-1) != null);
			assertTrue(rec.validRec(0) != null);
			assertFalse(rec.validRec(1) != null);
			assertTrue(rec.validRec(30) != null);
			assertTrue(rec.validRec(52) != null);
			assertTrue(rec.validRec(74) != null);
			assertTrue(rec.validRec(96) != null);
			assertTrue(rec.validRec(118) != null);
			assertTrue(rec.validRec(140) != null);
			assertTrue(rec.validRec(162) != null);
			assertFalse(rec.validRec(170) != null);
			assertFalse(rec.validRec(999) != null);

			// subdivide
			LinkedList<G4Blob> sr = rec.subdivide();
			assertTrue(sr.size() == 8);
			for(G4Blob b : sr)
				assertTrue(b.singleValidRec());
		} catch(Exception ex) {
			assertTrue(false);
		}

		// readComplete
		assertTrue(new G4Blob(sample_array).readComplete());
	}
}
