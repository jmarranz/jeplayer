/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manual;

import example.loadmanually.SimpleDataSource;
import javax.sql.DataSource;

/**
 *
 * @author jmarranz
 */
public class ReferenceManualCodeJEPLDroid
{
    public static void PROVIDE_A_DATASOURCE_FOR_SQLITE()
    {
        String jdbcDriver = "org.sqldroid.SQLDroidDriver";
        String url = "jdbc:sqlite://data/data/com.innowhere.jepldroidtest/test.db";
        String userName = "myLogin";
        String password = "myPW";        
        int poolSize = 1;
        
        DataSource ds = new SimpleDataSource(jdbcDriver,url,userName,password,poolSize);
    }
}
