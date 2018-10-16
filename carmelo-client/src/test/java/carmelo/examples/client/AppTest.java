package carmelo.examples.client;

import carmelo.common.UserConfiguration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
        System.out.println("AppTest");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        System.out.println("suite");
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        System.out.println("testApp");
        assertTrue( true );
        
        //测试
        String username;
        if((username = UserConfiguration.getProp("username")) == null) {
        	System.out.println("username is null");
        	UserConfiguration.setProp("username", "testname");
        }else {
        	System.out.println("username is " + username);
        }
    }
}
