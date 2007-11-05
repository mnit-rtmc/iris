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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.PixFontImpl;
import us.mn.state.dot.tms.IndexedListImpl;
import us.mn.state.dot.tms.PixCharacter;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to send a font to a DMS controller
 *
 * @author Douglas Lau
 */
public class DMSFontDownload extends DMSOperation {

	/** Version ID returned by Skyline controllers for NORMAL font */
	static protected final int SKYLINE_VERSION_ID_NORMAL = 288;

	/** Version ID returned by Skyline controllers for HINTED font */
	static protected final int SKYLINE_VERSION_ID_HINTED = 64723;

	/** Font to download to the sign */
	protected final PixFontImpl font;

	/** Font index */
	protected final int index;

	/** Character list */
	protected final IndexedListImpl charList;

	/** Create a new DMS font download operation */
	public DMSFontDownload(DMSImpl d, PixFontImpl f) {
		super(DOWNLOAD, d);
		font = f;
		// On ADDCO signs, font 1 is read-only (apparently)
		if(d.getMake().contains("ADDCO"))
			index = 2;
		else
			index = 1;
		charList = (IndexedListImpl)f.getCharacterList();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new CheckVersionID();
	}

	/** Check version ID */
	protected class CheckVersionID extends Phase {

		/** Check the font version ID */
		protected Phase poll(AddressedMessage mess) throws IOException {
			FontVersionID version = new FontVersionID(index);
			mess.add(version);
			try { mess.getRequest(); }
			catch(SNMP.Message.NoSuchName e) {
				return new CreateFont();
			}
			int v = version.getInteger();
			DMS_LOG.log(dms.getId() + " Font #" + index +
				" versionID:" + v);
			if(v == font.getVersionID())
				return null;
			if(index == 1 && v == SKYLINE_VERSION_ID_NORMAL)
				return null;
			if(index == 2 && v == SKYLINE_VERSION_ID_HINTED)
				return null;
			return new InvalidateFont();
		}
	}

	/** Invalidate the font */
	protected class InvalidateFont extends Phase {

		/** Invalidate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontHeight(index, 0));
			mess.setRequest();
			return new CreateFont();
		}
	}

	/** Create the font */
	protected class CreateFont extends Phase {

		/** Create a new font in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontNumber(index, index));
			mess.add(new FontName(index, font.getName()));
			mess.add(new FontHeight(index, font.getHeight()));
			mess.add(new FontCharSpacing(index,
				font.getCharacterSpacing()));
			mess.add(new FontLineSpacing(index,
				font.getLineSpacing()));
			mess.setRequest();
			if(charList.size() > 0)
				return new AddCharacter(1);
			else
				return new ValidateFont();
		}
	}

	/** Add a character to the font table */
	protected class AddCharacter extends Phase {

		/** Index of character in font */
		protected final int c_num;

		/** Create a new add character phase */
		public AddCharacter(int c) {
			c_num = c;
		}

		/** Add a character to the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			PixCharacter c = (PixCharacter)charList.getElement(
				c_num);
			mess.add(new CharacterWidth(index, c_num,
				c.getWidth()));
			mess.add(new CharacterBitmap(index, c_num,
				c.getBitmap()));
			mess.setRequest();
			if(c_num < charList.size()) {
				if(c_num % 20 == 0 && !controller.isFailed())
					controller.resetErrorCounter(id);
				return new AddCharacter(c_num + 1);
			} else
				return new ValidateFont();
		}
	}

	/** Validate the font. This forces a LedStar fontVersionID update. */
	protected class ValidateFont extends Phase {

		/** Validate a font entry in the font table */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new FontHeight(index, font.getHeight()));
			mess.setRequest();
			return new SetDefaultFont();
		}
	}

	/** Set the default font number for message text */
	protected class SetDefaultFont extends Phase {

		/** Set the default font numbmer */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DefaultFont(index));
			mess.setRequest();
			return null;
		}
	}
}
