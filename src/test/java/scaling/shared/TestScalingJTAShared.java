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

package scaling.shared;

import example.DataSourceFactoryOfLoaderJTA;
import example.DataSourceLoaderJTA;
import example.model.Person;
import java.util.Random;
import javax.sql.DataSource;
import jepl.JEPLBootJTA;
import jepl.JEPLJTADataSource;
import jepl.JEPLJTAMultipleDataSource;
import jepl.JEPLTransactionPropagation;

/**
 *
 * @author jmarranz
 */
public class TestScalingJTAShared
{
    public static String getProvider(TestScalingConf conf,DataSourceFactoryOfLoaderJTA dsLoader)
    {
        String[] providerList = conf.getProviderList();
        for(int i = 0; i < providerList.length; i++)
        {
            String provider = providerList[i];
            if (dsLoader.isJTAProviderSupported(provider))
            {
                return provider;
            }
        }
        throw new RuntimeException("CANNOT LOAD A PROVIDER");
    }
    
    public static DataSourceLoaderJTA[] createDataSourceLoaderJTAs(TestScalingConf conf,DataSourceFactoryOfLoaderJTA dsLoader,String jtaProvider)
    {
        int numberOfDataSources = conf.getNumberOfDataSources();
        if (numberOfDataSources == 1)
        {
            // Cogemos el DataSource remoto que nos interesa más para testear
            // la escalabilidad respecto a un DataSource accesible por red
            return new DataSourceLoaderJTA[]
                    { dsLoader.getDataSourceLoaderJTA( conf.getOnOneDataSourceUse() ,jtaProvider )  };
        }
        else
        {
            return dsLoader.getDataSourceLoaderListJTA( numberOfDataSources ,jtaProvider);
        }
    }

    public static JEPLJTADataSource[] getJEPLJTADataSourceList(DataSource[] dsArr,JEPLJTAMultipleDataSource jdsMgr)
    {
        JEPLBootJTA boot = jdsMgr.getJEPLBootJTA();
        JEPLJTADataSource[] jdsArr = new JEPLJTADataSource[dsArr.length];
        for(int i = 0; i < dsArr.length ; i++)
        {
            jdsArr[i] = boot.createJEPLJTADataSource(dsArr[i]);
            jdsArr[i].setDefaultJEPLTransactionPropagation(JEPLTransactionPropagation.NOT_SUPPORTED);
        }
        return jdsArr;
    }
    
    public static int getMasterDataSourceIndex(TestScalingConf conf,PersonDAOScaling[] personDaoArr)
    {
        if (personDaoArr.length == 1) return 0; // Ignoring provided value
        return conf.getMasterDataSource() - 1;
    }

    public static int getCloserDataSourceIndex(TestScalingConf conf,PersonDAOScaling[] personDaoArr)
    {
        if (personDaoArr.length == 1) return 0; // Ignoring provided value
        return conf.getCloserDataSource() - 1;
    }
    
    public static Person insertPerson(int masterDSIndex,PersonDAOScaling[] personDaoArr,boolean testRollback,Random rand)
    {
        Person person = new Person();
        person.setName("A Person object");
        person.setPhone("1111111");
        person.setEmail("hello@world.com");
        person.setAge(20);

        PersonDAOScaling dao = personDaoArr[masterDSIndex];
        dao.insertKeyGenerated(person);

        for(int i = 0; i < personDaoArr.length ; i++)
        {
            if (i == masterDSIndex) continue;

            if (testRollback && rand.nextInt(3) == 0)
                throw new RuntimeException("FALSE ERROR INSERT");
            PersonDAOScaling currDao = personDaoArr[i];
            currDao.insertKeyNotGenerated(person);
        }

        return person;
    }

    public static void updatePerson(int masterDSIndex,Person person,PersonDAOScaling[] personDaoArr,boolean testRollback,Random rand)
    {
        int age = person.getAge() + 1;
        if (age > Short.MAX_VALUE) age = 0;
        person.setAge(age);

        PersonDAOScaling dao = personDaoArr[masterDSIndex];
        dao.update(person);

        for(int i = 0; i < personDaoArr.length ; i++)
        {
            if (i == masterDSIndex) continue;
        
            if (testRollback && rand.nextInt(3) == 0)
                throw new RuntimeException("FALSE ERROR UPDATE");
            PersonDAOScaling currDao = personDaoArr[i];
            currDao.update(person);
        }
    }

    public static boolean deletePerson(int masterDSIndex,Person person,PersonDAOScaling[] personDaoArr,boolean testRollback,Random rand)
    {
        PersonDAOScaling dao = personDaoArr[masterDSIndex];
        boolean res = dao.deleteById(person.getId());
        if (res)
        {
            for(int i = 0; i < personDaoArr.length ; i++)
            {
                if (i == masterDSIndex) continue;

                if (testRollback && rand.nextInt(3) == 0)
                    throw new RuntimeException("FALSE ERROR DELETE");
                PersonDAOScaling currDao = personDaoArr[i];
                currDao.deleteById(person.getId());
            }
        }
        return res;
    }
}
