/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A simple class for file IO.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public final class FileIO {

	/** Atomic file-mover method - uses a newer Java file-moving api */
	static public void atomicMove(Path src, Path dst) throws IOException {
		try {
			try {
				Files.move(src, dst,
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.ATOMIC_MOVE);
			}
			catch (AtomicMoveNotSupportedException ex) {
				Files.move(src, dst,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException ex) {
			throw new IOException("Rename failed", ex);
		}
	}
}
