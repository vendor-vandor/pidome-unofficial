
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.pidome.server.connector.tools.PlatformException;
import org.pidome.server.connector.tools.Platforms;
import org.pidome.server.services.ServicesSuite;
import org.pidome.server.system.SystemSuite;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.db.DB;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@RunWith(Suite.class)
@SuiteClasses({ ServicesSuite.class, SystemSuite.class })
public class MasterSuite {

    @BeforeClass 
    public static void setUpClass() throws PlatformException, ConfigException, ConfigPropertiesException {      
        Platforms.setPlatform();
        SystemConfig.initialize();
        DB.init();
    }

    @AfterClass public static void tearDownClass() { 
        //DB.releaseAll();
        //DB.removeDB("system");
    }

}