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

package jepl;

/**
 * This interface is the base of "boot" interfaces, factories of data sources.
 * 
 * @author jmarranz
 */
public interface JEPLBoot extends JEPLUserData
{
    /**
     * Returns the boot root object used to create this data source factory.
     *
     * @return the parent boot root
     */
    public JEPLBootRoot getJEPLBootRoot();

    /**
     * Returns the number of the parsed queries cached by thread.
     *
     * <p>Parsing a SQL statement for ?, ?num or :name parameters taks time, if the SQL
     * query has been already parsed JEPLayer caches it to avoid parsing again.
     * </p>
     * <p>Caching is done per thread and there is no blocking between threads, the only caveat
     * is it needs memory, the more threads used for database actions the more thread
     * is used caching SQL statements.</p>
     *
     * <p>If you are going to use many threads and JEPLayer seems to take very much memory reduce
     * the number of cached queries or in the extreme set to 0 calling {@link #setMaxParsedQueriesCachedPerThread(int)}
     * </p>
     * 
     * @return the number of cached queries. By default is 500.
     * @see #setMaxParsedQueriesCachedPerThread(int)
     */
    public int getMaxParsedQueriesCachedPerThread();

    /**
     * Sets the number of parsed queries cached by thread.
     *
     * @param value the number of cached queries. A value of 0 disables caching.
     * @see #getMaxParsedQueriesCachedPerThread()
     */
    public void setMaxParsedQueriesCachedPerThread(int value);
}
