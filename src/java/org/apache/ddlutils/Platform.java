package org.apache.ddlutils;

/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.builder.SqlBuilder;
import org.apache.ddlutils.model.Database;

/**
 * A platform encapsulates the database-related functionality such as performing queries
 * and manipulations. It also contains an sql builder that is specific to this platform.
 * 
 * @author <a href="mailto:tomdz@apache.org">Thomas Dudziak</a>
 * @version $Revision: 231110 $
 */
public interface Platform
{
    /**
     * Returns the name of the database that this platform is for.
     * 
     * @return The database name
     */
    public String getDatabaseName();

    /**
     * Returns the info object for this platform.
     * 
     * @return The info object
     */
    public PlatformInfo getPlatformInfo();
    
    /**
     * Returns the sql builder for the this database.
     * 
     * @return The sql builder
     */
    public SqlBuilder getSqlBuilder();

    /**
     * Returns the data source that this platform uses to access the database.
     * 
     * @return The data source
     */
    public DataSource getDataSource();
    
    /**
     * Sets the data source that this platform shall use to access the database.
     * 
     * @param dataSource The data source
     */
    public void setDataSource(DataSource dataSource);

    /**
     * Returns a (new) JDBC connection from the data source.
     * 
     * @return The connection
     */
    public Connection borrowConnection() throws DynaSqlException;

    /**
     * Closes the given JDBC connection (returns it back to the pool if the datasource is poolable).
     * 
     * @param connection The connection
     */
    public void returnConnection(Connection connection);

    /**
     * Executes a series of sql statements which must be seperated by the delimiter
     * configured as {@link PlatformInfo#getSqlCommandDelimiter()} of the info object
     * of this platform.
     * 
     * @param connection      The connection to the database
     * @param sql             The sql statements to execute
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The number of errors
     */
    public int evaluateBatch(String sql, boolean continueOnError) throws DynaSqlException;

    /**
     * Executes a series of sql statements which must be seperated by the delimiter
     * configured as {@link PlatformInfo#getSqlCommandDelimiter()} of the info object
     * of this platform.
     *
     * TODO: consider outputting a collection of String or some kind of statement
     * object from the SqlBuilder instead of having to parse strings here
     *
     * @param connection      The connection to the database
     * @param sql             The sql statements to execute
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The number of errors
     */
    public int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DynaSqlException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    public void createTables(Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DynaSqlException;

    /**
     * Alters the database schema so that it match the given model. Drops and table modifications will
     * not be made.
     *
     * @param desiredDb The desired database schema
     */
    public void alterTables(Database desiredDb, boolean continueOnError) throws DynaSqlException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param desiredDb     The desired database schema
     * @param doDrops       Whether columns, tables and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     */
    public void alterTables(Database desiredDb, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException;

    /**
     * Alters the database schema so that it match the given model. Drops and table modifications will
     * not be made.
     *
     * @param connection A connection to the existing database that shall be modified
     * @param desiredDb  The desired database schema
     */
    public void alterTables(Connection connection, Database desiredDb, boolean continueOnError) throws DynaSqlException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param connection    A connection to the existing database that shall be modified
     * @param desiredDb     The desired database schema
     * @param doDrops       Whether columns, tables and indexes should be dropped if not in the
     *                      new schema
     * @param modifyColumns Whether columns should be altered for datatype, size as required
     */
    public void alterTables(Connection connection, Database desiredDb, boolean doDrops, boolean modifyColumns, boolean continueOnError) throws DynaSqlException;

    /**
     * Drops the tables defined in the given database.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    public void dropTables(Database model, boolean continueOnError) throws DynaSqlException;

    /**
     * Drops the tables defined in the given database.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    public void dropTables(Connection connection, Database model, boolean continueOnError) throws DynaSqlException; 

    /**
     * Performs the given parameterized SQL query returning an iterator over the results.
     *
     * @param model      The database model to use
     * @param sql        The sql query to perform
     * @param parameters The query parameter values
     * @return An iterator for the dyna beans resulting from the query
     */
    public Iterator query(Database model, String sql, Collection parameters) throws DynaSqlException;

    /**
     * Performs the given SQL query returning an iterator over the results.
     *
     * @param model The database model to use
     * @param sql   The sql query to perform
     * @return An iterator for the dyna beans resulting from the query
     */
    public Iterator query(Database model, String sql) throws DynaSqlException;

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String)} method all beans will be
     * materialized and the connection will be closed before returning the beans. 
     * 
     * @param model The database model to use
     * @param sql   The sql query
     * @return The dyna beans resulting from the query
     */
    public List fetch(Database model, String sql) throws DynaSqlException;

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String, Collection)} method all beans will be
     * materialized and the connection will be closed before returning the beans. 
     * 
     * @param sql        The parameterized query
     * @param parameters The parameter values
     * @return The dyna beans resulting from the query
     */
    public List fetch(Database model, String sql, Collection parameters) throws DynaSqlException;

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String)} method all beans will be
     * materialized and the connection will be closed before returning the beans.
     * Also, the two int parameters specify which rows of the result set to use.
     * If there are more rows than desired, they will be ignored (and not read
     * from the database).
     * 
     * @param model The database model to use
     * @param sql   The sql query
     * @param start Row number to start from (0 for first row)
     * @param end   Row number to stop at (inclusively; -1 for last row)
     * @return The dyna beans resulting from the query
     */
    public List fetch(Database model, String sql, int start, int end) throws DynaSqlException;

    /**
     * Queries for a list of dyna beans representing rows of the given query.
     * In contrast to the {@link #query(String, Collection)} method all beans will be
     * materialized and the connection will be closed before returning the beans.
     * Also, the two int parameters specify which rows of the result set to use.
     * If there are more rows than desired, they will be ignored (and not read
     * from the database).
     * 
     * @param model      The database model to use
     * @param sql        The parameterized sql query
     * @param parameters The parameter values
     * @param start      Row number to start from (0 for first row)
     * @param end        Row number to stop at (inclusively; -1 for last row)
     * @return The dyna beans resulting from the query
     */
    public List fetch(Database model, String sql, Collection parameters, int start, int end) throws DynaSqlException;

    /**
     * Stores the given bean in the database, inserting it if there is no primary key
     * otherwise the bean is updated in the database.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean to store
     */
    public void store(Database model, DynaBean dynaBean) throws DynaSqlException;

    /**
     * Returns the sql for inserting the given bean.
     * 
     * @param dynaBean The bean
     * @return The insert sql
     */
    public String getInsertSql(Database model, DynaBean dynaBean);

    /**
     * Inserts the given DynaBean in the database, assuming the primary key values are specified.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean to insert
     */
    public void insert(Database model, DynaBean dynaBean) throws DynaSqlException;

    /**
     * Inserts the bean. If one of the columns is an auto-incremented column, then the
     * bean will also be updated with the column value generated by the database.
     * Note that the connection will not be closed by this method.
     * 
     * @param model      The database model to use
     * @param dynaBean   The bean
     * @param connection The database connection
     */
    public void insert(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException;

    /**
     * Returns the sql for updating the given bean in the database.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean
     * @return The update sql
     */
    public String getUpdateSql(Database model, DynaBean dynaBean);

    /**
     * Updates the given bean in the database, assuming the primary key values are specified.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean
     */
    public void update(Database model, DynaBean dynaBean) throws DynaSqlException;

    /**
     * Updates the row which maps to the given bean.
     * 
     * @param model      The database model to use
     * @param dynaBean   The bean
     * @param connection The database connection
     */
    public void update(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException;

    /**
     * Returns the sql for deleting the given bean from the database.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean
     * @return The sql
     */
    public String getDeleteSql(Database model, DynaBean dynaBean);

    /**
     * Deletes the given bean from the database, assuming the primary key values are specified.
     * 
     * @param model    The database model to use
     * @param dynaBean The bean to delete
     */
    public void delete(Database model, DynaBean dynaBean) throws DynaSqlException;

    /**
     * Deletes the row which maps to the given bean from the database.
     * 
     * @param model      The database model to use
     * @param dynaBean   The bean
     * @param connection The database connection
     */
    public void delete(Database model, DynaBean dynaBean, Connection connection) throws DynaSqlException;
}