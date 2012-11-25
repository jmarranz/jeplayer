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
package manual;

import example.DataSourceFactoryOfLoaderJDBC;
import example.DataSourceLoader;
import javax.sql.DataSource;
import jepl.JEPLBootNonJTA;
import jepl.JEPLBootRoot;
import jepl.JEPLDAL;
import jepl.JEPLNonJTADataSource;

/**
 *
 * @author jmarranz
 */
public class CreateDBModel
{
    public static void main(String[] args)
    {
        DataSourceLoader dsFactory = 
                DataSourceFactoryOfLoaderJDBC.getDataSourceFactoryOfLoaderJDBC().createDataSourceLoaderJDBC();

        DataSource ds = dsFactory.getDataSource();
        JEPLBootNonJTA boot = JEPLBootRoot.get().createJEPLBootNonJTA();
        JEPLNonJTADataSource jds = boot.createJEPLNonJTADataSource(ds);

        try
        {
            JEPLDAL dal = jds.createJEPLDAL();

            dal.createJEPLDALQuery("DROP TABLE IF EXISTS PERSON").executeUpdate();
            dal.createJEPLDALQuery("DROP TABLE IF EXISTS COMPANY").executeUpdate();
            dal.createJEPLDALQuery("DROP TABLE IF EXISTS CONTACT").executeUpdate();

            dal.createJEPLDALQuery(
                "CREATE TABLE  CONTACT (" +
                "  ID INT NOT NULL AUTO_INCREMENT," +
                "  EMAIL VARCHAR(255) NOT NULL," +
                "  NAME VARCHAR(255) NOT NULL," +
                "  PHONE VARCHAR(255) NOT NULL," +
                "  PRIMARY KEY (ID)" +
                ")" +
                "ENGINE=InnoDB"
                ).executeUpdate();

            dal.createJEPLDALQuery(
                "CREATE TABLE  PERSON (" +
                "  ID INT NOT NULL," +
                "  AGE SMALLINT," +
                "  CONSTRAINT PERSON_FK_ID FOREIGN KEY (ID) REFERENCES CONTACT(ID) " +
                "     ON DELETE CASCADE" +
                ")" +
                "ENGINE=InnoDB"
                ).executeUpdate();

            dal.createJEPLDALQuery(
                "CREATE TABLE  COMPANY (" +
                "  ID INT NOT NULL," +
                "  ADDRESS VARCHAR(255) NOT NULL," +
                "  CONSTRAINT COMPANY_FK_ID FOREIGN KEY (ID) REFERENCES CONTACT(ID) " +
                "     ON DELETE CASCADE" +
                ")" +
                "ENGINE=InnoDB"
                ).executeUpdate();
        }
        finally
        {
            dsFactory.destroy();
        }
    }
}
