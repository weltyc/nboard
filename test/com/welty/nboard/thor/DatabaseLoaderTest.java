package com.welty.nboard.thor;

import junit.framework.TestCase;

public class DatabaseLoaderTest extends TestCase {

    public static void testIsWtbFilename() {
        assertTrue(DatabaseLoader.IsWtbFilename("temp.wtb"));
        assertTrue(!DatabaseLoader.IsWtbFilename("temp"));
        assertTrue(!DatabaseLoader.IsWtbFilename("bla.wtba"));
        assertTrue(DatabaseLoader.IsWtbFilename("foo.WTB"));
        assertTrue(DatabaseLoader.IsWtbFilename("c:/devl/othello/foo.wtB"));
    }
}
