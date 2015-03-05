/*
   Copyright 2011 Jose Maria Arranz Santamaria

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package example.loadmanually;

import example.DataSourceLoaderImpl;
import java.util.Properties;

/**
 *
 * @author jmarranz
 */
public abstract class DataSourceLoaderManualLoad extends DataSourceLoaderImpl
{
	public static final boolean android = System.getProperty("java.specification.name").contains("Dalvik");
    protected Properties props;
    protected String jdbcDriver;
    protected String url;
    protected String userName;
    protected String password;

    public DataSourceLoaderManualLoad(String configFile)
    {
        this.props = loadProperties(configFile);
        try
        {
            this.jdbcDriver = props.getProperty("jdbc.driver");
            this.url = props.getProperty("jdbc.url");
            this.userName = props.getProperty("jdbc.username");
            this.password = props.getProperty("jdbc.password");
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static Properties loadProperties(String fileName)
    {
    	// http://developer.android.com/reference/java/lang/System.html#getProperties%28%29
    	if (android)
    	{
    		// Android 
    		Properties props = new Properties();
            props.setProperty("jdbc.driver","org.sqldroid.SQLDroidDriver");
            props.setProperty("jdbc.url","jdbc:sqlite://data/data/com.innowhere.jepldroidtest/test.db");
            props.setProperty("jdbc.username","myLogin");
            props.setProperty("jdbc.password","myPW"); 
            props.setProperty("simpleDS.poolSize","2");            
            return props;
    	}
    	else
    	{
	        Properties props = new Properties();
	        try
	        {
	            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
	            return props;
	        }
	        catch (Exception ex)
	        {
	            throw new RuntimeException(ex);
	        }
    	}
    }
}
