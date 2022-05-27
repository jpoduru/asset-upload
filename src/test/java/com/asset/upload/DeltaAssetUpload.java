package com.asset.upload;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class DeltaAssetUpload
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DeltaAssetUpload( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DeltaAssetUpload.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
