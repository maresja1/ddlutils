package org.apache.ddlutils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.JdbcModelReader;
import org.apache.ddlutils.platform.SqlBuilder;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * A platform encapsulates the database-related functionality such as performing queries
 * and manipulations. It also contains an sql builder that is specific to this platform.
 * 
 * @version $Revision: 231110 $
 */
public interface Platform
{
    /**
     * Returns the name of the database that this platform is for.
     * 
     * @return The name
     */
    String getName();

    /**
     * Returns the info object for this platform.
     * 
     * @return The info object
     */
    PlatformInfo getPlatformInfo();
    
    /**
     * Returns the sql builder for the this platform.
     * 
     * @return The sql builder
     */
    SqlBuilder getSqlBuilder();

    /**
     * Returns the model reader (which reads a database model from a live database) for this platform.
     * 
     * @return The model reader
     */
    JdbcModelReader getModelReader();
    
    /**
     * Returns the data source that this platform uses to access the database.
     * 
     * @return The data source
     */
    DataSource getDataSource();
    
    /**
     * Sets the data source that this platform shall use to access the database.
     * 
     * @param dataSource The data source
     */
    void setDataSource(DataSource dataSource);

    /**
     * Returns the username that this platform shall use to access the database.
     * 
     * @return The username
     */
    String getUsername();

    /**
     * Sets the username that this platform shall use to access the database.
     * 
     * @param username The username
     */
    void setUsername(String username);

    /**
     * Returns the password that this platform shall use to access the database.
     * 
     * @return The password
     */
    String getPassword();

    /**
     * Sets the password that this platform shall use to access the database.
     * 
     * @param password The password
     */
    void setPassword(String password);

    // runtime properties

    /**
     * Determines whether script mode is on. This means that the generated SQL is not
     * intended to be sent directly to the database but rather to be saved in a SQL
     * script file. Per default, script mode is off.
     * 
     * @return <code>true</code> if script mode is on
     */
    boolean isScriptModeOn();

    /**
     * Specifies whether script mode is on. This means that the generated SQL is not
     * intended to be sent directly to the database but rather to be saved in a SQL
     * script file.
     * 
     * @param scriptModeOn <code>true</code> if script mode is on
     */
    void setScriptModeOn(boolean scriptModeOn);

    /**
     * Determines whether delimited identifiers are used or normal SQL92 identifiers
     * (which may only contain alphanumerical characters and the underscore, must start
     * with a letter and cannot be a reserved keyword).
     * Per default, delimited identifiers are not used
     *
     * @return <code>true</code> if delimited identifiers are used
     */
    boolean isDelimitedIdentifierModeOn();

    /**
     * Specifies whether delimited identifiers are used or normal SQL92 identifiers.
     *
     * @param delimitedIdentifierModeOn <code>true</code> if delimited identifiers shall be used
     */
    void setDelimitedIdentifierModeOn(boolean delimitedIdentifierModeOn);

    /**
     * Determines whether SQL comments are generated. 
     * 
     * @return <code>true</code> if SQL comments shall be generated
     */
    boolean isSqlCommentsOn();

    /**
     * Specifies whether SQL comments shall be generated.
     * 
     * @param sqlCommentsOn <code>true</code> if SQL comments shall be generated
     */
    void setSqlCommentsOn(boolean sqlCommentsOn);

    /**
     * Determines whether SQL insert statements can specify values for identity columns.
     * This setting is only relevant if the database supports it
     * ({@link PlatformInfo#isIdentityOverrideAllowed()}). If this is off, then the
     * <code>insert</code> methods will ignore values for identity columns. 
     *  
     * @return <code>true</code> if identity override is enabled (the default)
     */
    boolean isIdentityOverrideOn();

    /**
     * Specifies whether SQL insert statements can specify values for identity columns.
     * This setting is only relevant if the database supports it
     * ({@link PlatformInfo#isIdentityOverrideAllowed()}). If this is off, then the
     * <code>insert</code> methods will ignore values for identity columns. 
     *  
     * @param identityOverrideOn <code>true</code> if identity override is enabled (the default)
     */
    void setIdentityOverrideOn(boolean identityOverrideOn);

    /**
     * Determines whether foreign keys of a table read from a live database
     * are alphabetically sorted.
     *
     * @return <code>true</code> if read foreign keys are sorted
     */
    boolean isForeignKeysSorted();

    /**
     * Specifies whether foreign keys read from a live database, shall be
     * alphabetically sorted.
     *
     * @param foreignKeysSorted <code>true</code> if read foreign keys shall be sorted
     */
    void setForeignKeysSorted(boolean foreignKeysSorted);

    /**
     * Determines whether the default action for ON UPDATE is used if the specified one is not supported by the platform.
     * If this is set to <code>false</code>, then an exception will be thrown if the action is not supported. By default, this
     * is set to <code>true</code> meaning that the default action would be used.
     * 
     * @return <code>true</code> if the default action is used
     */
    boolean isDefaultOnUpdateActionUsedIfUnsupported();

    /**
     * Specifies whether the default action for ON UPDATE shall be used if the specified one is not supported by the platform.
     * If this is set to <code>false</code>, then an exception will be thrown if the action is not supported. By default, this
     * is set to <code>true</code> meaning that the default action would be used.
     * 
     * @param useDefault If <code>true</code> then the default action will be used
     */
    void setDefaultOnUpdateActionUsedIfUnsupported(boolean useDefault);

    /**
     * Determines whether the default action for ON DELETE is used if the specified one is not supported by the platform.
     * If this is set to <code>false</code>, then an exception will be thrown if the action is not supported. By default, this
     * is set to <code>true</code> meaning that the default action would be used.
     * 
     * @return <code>true</code> if the default action is used
     */
    boolean isDefaultOnDeleteActionUsedIfUnsupported();

    /**
     * Specifies whether the default action for ON DELETE shall be used if the specified one is not supported by the platform.
     * If this is set to <code>false</code>, then an exception will be thrown if the action is not supported. By default, this
     * is set to <code>true</code> meaning that the default action would be used.
     * 
     * @param useDefault If <code>true</code> then the default action will be used
     */
    void setDefaultOnDeleteActionUsedIfUnsupported(boolean useDefault);

    // functionality
    
    /**
     * Returns a (new) JDBC connection from the data source.
     * 
     * @return The connection
     */
    Connection borrowConnection() throws DatabaseOperationException;

    /**
     * Closes the given JDBC connection (returns it back to the pool if the datasource is poolable).
     * 
     * @param connection The connection
     */
    void returnConnection(Connection connection);

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
    int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Performs a shutdown at the database. This is necessary for some embedded databases which otherwise
     * would be locked and thus would refuse other connections. Note that this does not change the database
     * structure or data in it in any way.
     */
    void shutdownDatabase() throws DatabaseOperationException;

    /**
     * Performs a shutdown at the database. This is necessary for some embedded databases which otherwise
     * would be locked and thus would refuse other connections. Note that this does not change the database
     * structure or data in it in any way.
     * 
     * @param connection The connection to the database
     */
    void shutdownDatabase(Connection connection) throws DatabaseOperationException;

    /**
     * Creates the database specified by the given parameters. Please note that this method does not
     * use a data source set via {@link #setDataSource(DataSource)} because it is not possible to
     * retrieve the connection information from it without establishing a connection.<br/>
     * The given connection url is the url that you'd use to connect to the already-created
     * database.<br/>
     * On some platforms, this method suppurts additional parameters. These are documented in the
     * manual section for the individual platforms. 
     * 
     * @param jdbcDriverClassName The jdbc driver class name
     * @param connectionUrl       The url to connect to the database if it were already created
     * @param username            The username for creating the database
     * @param password            The password for creating the database
     * @param parameters          Additional parameters relevant to database creation (which are platform specific)
     */
    void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map parameters) throws DatabaseOperationException, UnsupportedOperationException;

    /**
     * Drops the database specified by the given parameters. Please note that this method does not
     * use a data source set via {@link #setDataSource(DataSource)} because it is not possible to
     * retrieve the connection information from it without establishing a connection.
     * 
     * @param jdbcDriverClassName The jdbc driver class name
     * @param connectionUrl       The url to connect to the database
     * @param username            The username for creating the database
     * @param password            The password for creating the database
     */
    void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DatabaseOperationException, UnsupportedOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #createModel(Database, boolean, boolean)} instead.
     */
    void createTables(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #createModel(Connection, Database, boolean, boolean)} instead.
     */
    void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #createModel(Database, CreationParameters, boolean, boolean)} instead.
     */
    void createTables(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #createModel(Connection, Database, CreationParameters, boolean, boolean)} instead.
     */
    void createTables(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Returns the SQL for creating the tables defined in the database model.
     * 
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     * @deprecated Use {@link #getCreateModelSql(Database, boolean, boolean)} instead.
     */
    String getCreateTablesSql(Database model, boolean dropTablesFirst, boolean continueOnError);

    /**
     * Returns the SQL for creating the tables defined in the database model.
     * 
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     * @deprecated Use {@link #getCreateModelSql(Database, CreationParameters, boolean, boolean)} instead.
     */
    String getCreateTablesSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError);

    /**
     * Creates the tables defined in the database model.
     * 
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void createModel(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void createModel(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void createModel(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Creates the tables defined in the database model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void createModel(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Returns the SQL for creating the tables defined in the database model.
     * 
     * @param model           The database model
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     */
    String getCreateModelSql(Database model, boolean dropTablesFirst, boolean continueOnError);

    /**
     * Returns the SQL for creating the tables defined in the database model.
     * 
     * @param model           The database model
     * @param params          The parameters used in the creation
     * @param dropTablesFirst Whether to drop the tables prior to creating them (anew)
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     */
    String getCreateModelSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError);

    /**
     * Returns the necessary changes to apply to the current database to make it the desired one.
     * These changes are in the correct order and have been adjusted for the current platform.
     * 
     * @param currentModel The current model
     * @param desiredModel The desired model
     * @return The list of changes, adjusted to the platform and sorted for execution
     */
    List getChanges(Database currentModel, Database desiredModel);

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param desiredDb       The desired database schema
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Database, Database, boolean)} together with
     *             {@link #readModelFromDatabase(String)} instead.
     */
    void alterTables(Database desiredDb, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param desiredDb       The desired database schema
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Database, Database, CreationParameters, boolean)} together with
     *             {@link #readModelFromDatabase(String)} instead.
     */
    void alterTables(Database desiredDb, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param catalog         The catalog in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param schema          The schema in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param tableTypes      The table types to read from the existing database;
     *                        use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb       The desired database schema
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Database, Database, boolean)} together with
     *             {@link #readModelFromDatabase(String, String, String, List<String>)} instead.
     */
    void alterTables(String catalog, String schema, List<String> tableTypes, Database desiredDb, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param catalog         The catalog in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param schema          The schema in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param tableTypes      The table types to read from the existing database;
     *                        use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb       The desired database schema
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Database, Database, CreationParameters, boolean)} together with
     *             {@link #readModelFromDatabase(String, String, String, List<String>)} instead.
     */
    void alterTables(String catalog, String schema, List<String> tableTypes, Database desiredDb, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param desiredDb       The desired database schema
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Connection, Database, Database, boolean)} together with
     *             {@link #readModelFromDatabase(Connection, String)} instead.
     */
    void alterTables(Connection connection, Database desiredDb, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param desiredDb       The desired database schema
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Connection, Database, Database, CreationParameters, boolean)} together with
     *             {@link #readModelFromDatabase(Connection, String)} instead.
     */
    void alterTables(Connection connection, Database desiredDb, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param catalog         The catalog in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param schema          The schema in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param tableTypes      The table types to read from the existing database;
     *                        use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb       The desired database schema
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Connection, Database, Database, boolean)} together with
     *             {@link #readModelFromDatabase(Connection, String, String, String, List<String>)} instead.
     */
    void alterTables(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredDb, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the database schema so that it match the given model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param catalog         The catalog in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param schema          The schema in the existing database to read (can be a pattern);
     *                        use <code>null</code> for the platform-specific default value
     * @param tableTypes      The table types to read from the existing database;
     *                        use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb       The desired database schema
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     * @deprecated Use {@link #alterModel(Connection, Database, Database, CreationParameters, boolean)} together with
     *             {@link #readModelFromDatabase(Connection, String, String, String, List<String>)} instead.
     */
    void alterTables(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredDb, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param desiredDb The desired database schema
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database)} together with
     *             {@link #readModelFromDatabase(String)} instead.
     */
    String getAlterTablesSql(Database desiredDb) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param desiredDb The desired database schema
     * @param params    The parameters used in the creation
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database, CreationParameters)} together with
     *             {@link #readModelFromDatabase(String)} instead.
     */
    String getAlterTablesSql(Database desiredDb, CreationParameters params) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param catalog    The catalog in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param schema     The schema in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param tableTypes The table types to read from the existing database;
     *                   use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb  The desired database schema
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database)} together with
     *             {@link #readModelFromDatabase(String, String, String, List<String>)} instead.
     */
    String getAlterTablesSql(String catalog, String schema, List<String> tableTypes, Database desiredDb) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param catalog    The catalog in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param schema     The schema in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param tableTypes The table types to read from the existing database;
     *                   use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb  The desired database schema
     * @param params     The parameters used in the creation
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database, CreationParameters)} together with
     *             {@link #readModelFromDatabase(String, String, String, List<String>)} instead.
     */
    String getAlterTablesSql(String catalog, String schema, List<String> tableTypes, Database desiredDb, CreationParameters params) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param connection A connection to the existing database that shall be modified
     * @param desiredDb  The desired database schema
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database)} together with
     *             {@link #readModelFromDatabase(Connection, String)} instead.
     */
    String getAlterTablesSql(Connection connection, Database desiredDb) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param connection A connection to the existing database that shall be modified
     * @param desiredDb  The desired database schema
     * @param params     The parameters used in the creation
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database, CreationParameters)} together with
     *             {@link #readModelFromDatabase(Connection, String)} instead.
     */
    String getAlterTablesSql(Connection connection, Database desiredDb, CreationParameters params) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param connection A connection to the existing database that shall be modified
     * @param catalog    The catalog in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param schema     The schema in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param tableTypes The table types to read from the existing database;
     *                   use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb  The desired database schema
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database)} together with
     *             {@link #readModelFromDatabase(Connection, String, String, String, List<String>)} instead.
     */
    String getAlterTablesSql(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredDb) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the database schema so that it match the given model.
     *
     * @param connection A connection to the existing database that shall be modified
     * @param catalog    The catalog in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param schema     The schema in the existing database to read (can be a pattern);
     *                   use <code>null</code> for the platform-specific default value
     * @param tableTypes The table types to read from the existing database;
     *                   use <code>null</code> or an empty array for the platform-specific default value
     * @param desiredDb  The desired database schema
     * @param params     The parameters used in the creation
     * @return The SQL statements
     * @deprecated Use {@link #getAlterModelSql(Database, Database, CreationParameters)} together with
     *             {@link #readModelFromDatabase(Connection, String, String, String, List<String>)} instead.
     */
    String getAlterTablesSql(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredDb, CreationParameters params) throws DatabaseOperationException;

    /**
     * Alters the given live database model so that it match the desired model, using the default database conneciton.
     *
     * @param currentModel    The current database model
     * @param desiredModel    The desired database model
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     */
    void alterModel(Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the given live database model so that it match the desired model, using the default database conneciton.
     *
     * @param currentModel    The current database model
     * @param desiredModel    The desired database model
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     */
    void alterModel(Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the given live database model so that it match the desired model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param currentModel    The current database model
     * @param desiredModel    The desired database model
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     */
    void alterModel(Connection connection, Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Alters the given live database model so that it match the desired model.
     *
     * @param connection      A connection to the existing database that shall be modified
     * @param currentModel    The current database model
     * @param desiredModel    The desired database model
     * @param params          The parameters used in the creation
     * @param continueOnError Whether to continue with the next sql statement when an error occurred
     */
    void alterModel(Connection connection, Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the given current model so that it match the desired model.
     *
     * @param currentModel The current database model
     * @param desiredModel The desired database model
     * @return The SQL statements
     */
    String getAlterModelSql(Database currentModel, Database desiredModel) throws DatabaseOperationException;

    /**
     * Returns the SQL for altering the given current model so that it match the desired model.
     *
     * @param currentModel The current database model
     * @param desiredModel The desired database model
     * @param params       The parameters used in the creation of tables etc.
     * @return The SQL statements
     */
    String getAlterModelSql(Database currentModel, Database desiredModel, CreationParameters params) throws DatabaseOperationException;

    /**
     * Drops the specified table and all foreign keys pointing to it.
     * 
     * @param model           The database model
     * @param table           The table to drop
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void dropTable(Database model, Table table, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Returns the SQL for dropping the given table and all foreign keys pointing to it.
     * 
     * @param model           The database model
     * @param table           The table to drop
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     */
    String getDropTableSql(Database model, Table table, boolean continueOnError);

    /**
     * Drops the specified table and all foreign keys pointing to it.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param table           The table to drop
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void dropTable(Connection connection, Database model, Table table, boolean continueOnError) throws DatabaseOperationException; 

    /**
     * Returns the SQL for dropping the given model.
     * 
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @return The SQL statements
     * @deprecated Use {@link #getDropModelSql(Database)} instead.
     */
    String getDropTablesSql(Database model, boolean continueOnError);

    /**
     * Drops the given model using the default database connection.
     * 
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #dropModel(Database, boolean)} instead.
     */
    void dropTables(Database model, boolean continueOnError) throws DatabaseOperationException;

    /**
     * Drops the given model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     * @deprecated Use {@link #dropModel(Connection, Database, boolean)} instead.
     */
    void dropTables(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException; 

    /**
     * Returns the SQL for dropping the given model.
     * 
     * @param model The database model
     * @return The SQL statements
     */
    String getDropModelSql(Database model);

    /**
     * Drops the given model using the default database connection.
     * 
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void dropModel(Database model, boolean continueOnError) throws DatabaseOperationException; 

    /**
     * Drops the given model.
     * 
     * @param connection      The connection to the database
     * @param model           The database model
     * @param continueOnError Whether to continue executing the sql commands when an error occurred
     */
    void dropModel(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException; 

    /**
     * Reads the database model from the live database as specified by the data source set for
     * this platform.
     * 
     * @param name The name of the resulting database; <code>null</code> when the default name (the catalog)
     *             is desired which might be <code>null</code> itself though
     * @return The database model
     * @throws DatabaseOperationException If an error occurred during reading the model
     */
    Database readModelFromDatabase(String name) throws DatabaseOperationException;

    /**
     * Reads the database model from the live database as specified by the data source set for
     * this platform.
     * 
     * @param name       The name of the resulting database; <code>null</code> when the default name (the catalog)
     *                   is desired which might be <code>null</code> itself though
     * @param catalog    The catalog to access in the database; use <code>null</code> for the default value
     * @param schema     The schema to access in the database; use <code>null</code> for the default value
     * @param tableTypes The table types to process; use <code>null</code> or an empty list for the default ones
     * @return The database model
     * @throws DatabaseOperationException If an error occurred during reading the model
     */
    Database readModelFromDatabase(String name, String catalog, String schema, List<String> tableTypes) throws DatabaseOperationException;

    /**
     * Reads the database model from the live database to which the given connection is pointing.
     * 
     * @param connection The connection to the database
     * @param name       The name of the resulting database; <code>null</code> when the default name (the catalog)
     *                   is desired which might be <code>null</code> itself though
     * @return The database model
     * @throws DatabaseOperationException If an error occurred during reading the model
     */
    Database readModelFromDatabase(Connection connection, String name) throws DatabaseOperationException;

    /**
     * Reads the database model from the live database to which the given connection is pointing.
     * 
     * @param connection The connection to the database
     * @param name       The name of the resulting database; <code>null</code> when the default name (the catalog)
     *                   is desired which might be <code>null</code> itself though
     * @param catalog    The catalog to access in the database; use <code>null</code> for the default value
     * @param schema     The schema to access in the database; use <code>null</code> for the default value
     * @param tableTypes The table types to process; use <code>null</code> or an empty list for the default ones
     * @return The database model
     * @throws DatabaseOperationException If an error occurred during reading the model
     */
    Database readModelFromDatabase(Connection connection, String name, String catalog, String schema, List<String> tableTypes) throws DatabaseOperationException;

	Map<String, Object> insert(Database model, String table, Map<String, Object> data);

	Map<String, Object> insert(Connection connection, Database model, String table, Map<String, Object> data);

	void delete(Database model, String tableName, Map<String, Object> data);

	void delete(Connection connection, Database model, String tableName, Map<String, Object> dynaBean) throws DatabaseOperationException;

	List<Map<String, Object>> fetch(Database model, String sql, @Nullable Collection<Object> parameters, List<Table> queryHints, int start, int end) throws DatabaseOperationException;


	default List<Map<String, Object>> fetch(Database model, String sql, Collection<Object> parameters, List<Table> queryHints) throws DatabaseOperationException {
		return fetch(model, sql, parameters, queryHints, 0, -1);
	}

	default List<Map<String, Object>> fetch(Database model, String sql, List<Table> queryHints) throws DatabaseOperationException {
		return fetch(model, sql, null, queryHints, 0, -1);
	}

	default List<Map<String, Object>> fetch(Database model, String sql, Table queryHint) throws DatabaseOperationException {
		return fetch(model, sql, null, List.of(queryHint), 0, -1);
	}

	void update(Database model, Table table, Map<String, Object> columnValues);

	void update(Connection connection, Database model, Table table, Map<String, Object> columnValues);
}
