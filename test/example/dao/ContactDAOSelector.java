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

import example.model.Contact;
import jepl.JEPLDataSource;

/**
 *
 * @author jmarranz
 */
public class ContactDAOSelector extends ContactDAOSelectorBase
{
    protected ContactDAO contactDAO;

    public ContactDAOSelector(Contact obj,JEPLDataSource ds)
    {
        super(obj);
        this.contactDAO = new ContactDAO(ds);
    }

    @Override
    public void insert()
    {
        contactDAO.insert(obj);
    }

    @Override
    public void update()
    {
        contactDAO.update(obj);
    }

    @Override
    public boolean delete()
    {
        return contactDAO.delete(obj);
    }
}
