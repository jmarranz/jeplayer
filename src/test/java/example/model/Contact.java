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
package example.model;

public class Contact
{
    protected int id;
    protected String name;
    protected String phone;
    protected String email;
    protected String notPersisAttr;
    
    public Contact(int id, String name, String phone, String email)
    {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Contact()
    {
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getNotPersisAttr() 
    {
        return notPersisAttr;
    }

    public void setNotPersisAttr(String notPersisAttr) 
    {
        this.notPersisAttr = notPersisAttr;
    }    
}
