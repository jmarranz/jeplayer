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
import example.model.Contact;
import example.model.Person;
import jepl.JEPLDataSource;

public abstract class ContactDAOSelectorBase
{
    protected Contact obj;

    public ContactDAOSelectorBase(Contact obj)
    {
        this.obj = obj;
    }

    public static ContactDAOSelectorBase createContactDAOSelectorBase(Contact obj,JEPLDataSource ds)
    {
        if (obj instanceof Person) return new PersonDAOSelector((Person)obj,ds);
        else if (obj instanceof Company) return new CompanyDAOSelector((Company)obj,ds);
        else return new ContactDAOSelector(obj,ds);
    }

    public abstract void insert();

    public abstract void update();

    public abstract boolean delete();
}
