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
package example.dao;

import example.model.Company;
import example.model.Person;
import jepl.JEPLDataSource;

/**
 *
 * @author jmarranz
 */
public class CompanyDAOSelector extends ContactDAOSelectorBase
{
    protected CompanyDAO companyDAO;

    public CompanyDAOSelector(Company obj,JEPLDataSource ds)
    {
        super(obj);
        this.companyDAO = new CompanyDAO(ds);
    }

    public Company getCompany()
    {
        return (Company)obj;
    }    
    
    @Override
    public void insert()
    {
        companyDAO.insert(getCompany());
    }

    @Override
    public void update()
    {
        companyDAO.update(getCompany());
    }

    @Override
    public boolean delete()
    {
        return companyDAO.delete(getCompany());
    }
}
