/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import junit.framework.TestCase;

/**
 * Hashtags unit tests.
 *
 * @author Doug Lau
 */
public class HashtagsTest extends TestCase {

	public HashtagsTest(String name) {
		super(name);
	}

	public void testNormalize() {
		assertTrue("#1".equals(Hashtags.normalize("#1")));
		assertTrue("#Abc123".equals(Hashtags.normalize("#Abc123")));
		assertTrue("#Xyz456".equals(Hashtags.normalize("#Xyz456")));
		assertTrue("#MyTag".equals(Hashtags.normalize("#MyTag ")));
		assertTrue(null == Hashtags.normalize("#"));
		assertTrue(null == Hashtags.normalize("##"));
		assertTrue(null == Hashtags.normalize("#123-456"));
		assertTrue(null == Hashtags.normalize("#A.b"));
		assertTrue(null == Hashtags.normalize("#X_y"));
	}

	public void testContains() {
		Hashtags tags = new Hashtags("These are #special #notes, ugly!");
		assertTrue(tags.contains("#Special"));
		assertTrue(tags.contains("#NOTES"));
		assertFalse(tags.contains("#ugly!"));
		tags = new Hashtags("#Hi,#IsThis\t#AGood\n#Test?");
		assertTrue(tags.contains("#hi"));
		assertTrue(tags.contains("#isthis"));
		assertTrue(tags.contains("#agood"));
		assertTrue(tags.contains("#test"));
		assertFalse(tags.contains("#test?"));
		tags = new Hashtags("(#Nesting[#brace{#test}#this]#should)#work");
		assertTrue(tags.contains("#NESTING"));
		assertTrue(tags.contains("#BRACE"));
		assertTrue(tags.contains("#TEST"));
		assertTrue(tags.contains("#THIS"));
		assertTrue(tags.contains("#SHOULD"));
		assertTrue(tags.contains("#WORK"));
	}

	public void testAdd() {
		assertTrue("\n#tag".equals(Hashtags.add("", "#tag")));
		assertTrue("Random note\n#tag".equals(
			Hashtags.add("Random note", "#tag")
		));
		assertTrue("Note with #existing #tags\n#Plus".equals(
			Hashtags.add("Note with #existing #tags", "#Plus")
		));
		assertTrue("#Same tag\n#Same".equals(
			Hashtags.add("#Same tag", "#Same")
		));
		assertTrue("Notes".equals(Hashtags.add("Notes", "Invalid")));
	}

	public void testRemove() {
		assertTrue("".equals(Hashtags.remove("\n#tag", "#TAG")));
		assertTrue("".equals(Hashtags.remove("\n#trim   ", "#Trim")));
		assertTrue("Random note".equals(
			Hashtags.remove("Random note\n#TAG", "#tag")
		));
		assertTrue("Note with #existing #tags".equals(
			Hashtags.remove("Note with #existing #tags\n#Plus", "#plus")
		));
		assertTrue("tag\n#Same".equals(
			Hashtags.remove("#Same tag\n#Same", "#SaMe")
		));
		assertTrue("#First  #third".equals(
			Hashtags.remove("#First #SECOND #third", "#second")
		));
		assertTrue("#PartialTag".equals(
			Hashtags.remove("#PartialTag", "#Partial")
		));
		assertTrue("#PartialTag with more!".equals(
			Hashtags.remove("#PartialTag with more!", "#Partial")
		));
	}
}
