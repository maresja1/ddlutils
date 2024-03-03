package org.apache.ddlutils.platform;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddForeignKeyChange;
import org.apache.ddlutils.alteration.AddIndexChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.AddTableChange;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.alteration.ColumnOrderChange;
import org.apache.ddlutils.alteration.ForeignKeyChange;
import org.apache.ddlutils.alteration.IndexChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RecreateTableChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemoveForeignKeyChange;
import org.apache.ddlutils.alteration.RemoveIndexChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveTableChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.ModelException;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.util.JdbcSupport;
import org.apache.ddlutils.util.SqlTokenizer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Base class for platform implementations.
 * 
 * @version $Revision: 231110 $
 */
public abstract class PlatformImplBase extends JdbcSupport implements Platform
{
    /** The default name for models read from the database, if no name as given.*/
    protected static final String MODEL_DEFAULT_NAME = "default";

    /** The log for this platform. */
    private final Log _log = LogFactory.getLog(getClass());

    /** The platform info. */
    private PlatformInfo _info = new PlatformInfo();
    /** The sql builder for this platform. */
    private SqlBuilder _builder;
    /** The model reader for this platform. */
    private JdbcModelReader _modelReader;
    /** Whether script mode is on. */
    private boolean _scriptModeOn;
    /** Whether SQL comments are generated or not. */
    private boolean _sqlCommentsOn = true;
    /** Whether delimited identifiers are used or not. */
    private boolean _delimitedIdentifierModeOn;
    /** Whether identity override is enabled. */
    private boolean _identityOverrideOn;
    /** Whether read foreign keys shall be sorted alphabetically. */
    private boolean _foreignKeysSorted;
    /** Whether to use the default ON UPDATE action if the specified one is unsupported. */
    private boolean _useDefaultOnUpdateActionIfUnsupported = true;
    /** Whether to use the default ON DELETE action if the specified one is unsupported. */
    private boolean _useDefaultOnDeleteActionIfUnsupported = true;

    /**
     * {@inheritDoc}
     */
    public SqlBuilder getSqlBuilder()
    {
        return _builder;
    }

    /**
     * Sets the sql builder for this platform.
     * 
     * @param builder The sql builder
     */
    protected void setSqlBuilder(SqlBuilder builder)
    {
        _builder = builder;
    }

    /**
     * {@inheritDoc}
     */
    public JdbcModelReader getModelReader()
    {
        if (_modelReader == null)
        {
            _modelReader = new JdbcModelReader(this);
        }
        return _modelReader;
    }

    /**
     * Sets the model reader for this platform.
     * 
     * @param modelReader The model reader
     */
    protected void setModelReader(JdbcModelReader modelReader)
    {
        _modelReader = modelReader;
    }

    /**
     * {@inheritDoc}
     */
    public PlatformInfo getPlatformInfo()
    {
        return _info;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isScriptModeOn()
    {
        return _scriptModeOn;
    }

    /**
     * {@inheritDoc}
     */
    public void setScriptModeOn(boolean scriptModeOn)
    {
        _scriptModeOn = scriptModeOn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSqlCommentsOn()
    {
        return _sqlCommentsOn;
    }

    /**
     * {@inheritDoc}
     */
    public void setSqlCommentsOn(boolean sqlCommentsOn)
    {
        if (!getPlatformInfo().isSqlCommentsSupported() && sqlCommentsOn)
        {
            throw new DdlUtilsException("Platform " + getName() + " does not support SQL comments");
        }
        _sqlCommentsOn = sqlCommentsOn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDelimitedIdentifierModeOn()
    {
        return _delimitedIdentifierModeOn;
    }

    /**
     * {@inheritDoc}
     */
    public void setDelimitedIdentifierModeOn(boolean delimitedIdentifierModeOn)
    {
        if (!getPlatformInfo().isDelimitedIdentifiersSupported() && delimitedIdentifierModeOn)
        {
            throw new DdlUtilsException("Platform " + getName() + " does not support delimited identifier");
        }
        _delimitedIdentifierModeOn = delimitedIdentifierModeOn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isIdentityOverrideOn()
    {
        return _identityOverrideOn;
    }

    /**
     * {@inheritDoc}
     */
    public void setIdentityOverrideOn(boolean identityOverrideOn)
    {
        _identityOverrideOn = identityOverrideOn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isForeignKeysSorted()
    {
        return _foreignKeysSorted;
    }

    /**
     * {@inheritDoc}
     */
    public void setForeignKeysSorted(boolean foreignKeysSorted)
    {
        _foreignKeysSorted = foreignKeysSorted;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDefaultOnUpdateActionUsedIfUnsupported()
    {
        return _useDefaultOnUpdateActionIfUnsupported;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultOnUpdateActionUsedIfUnsupported(boolean useDefault)
    {
        _useDefaultOnUpdateActionIfUnsupported = useDefault;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDefaultOnDeleteActionUsedIfUnsupported()
    {
        return _useDefaultOnDeleteActionIfUnsupported;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultOnDeleteActionUsedIfUnsupported(boolean useDefault)
    {
        _useDefaultOnDeleteActionIfUnsupported = useDefault;
    }

    /**
     * Returns the log for this platform.
     * 
     * @return The log
     */
    protected Log getLog()
    {
        return _log;
    }

    /**
     * Logs any warnings associated to the given connection. Note that the connection needs
     * to be open for this.
     * 
     * @param connection The open connection
     */
    protected void logWarnings(Connection connection) throws SQLException
    {
        SQLWarning warning = connection.getWarnings();

        while (warning != null)
        {
            getLog().warn(warning.getLocalizedMessage(), warning.getCause());
            warning = warning.getNextWarning();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int evaluateBatch(String sql, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            return evaluateBatch(connection, sql, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DatabaseOperationException
    {
        Statement statement    = null;
        int       errors       = 0;
        int       commandCount = 0;

        // we tokenize the SQL along the delimiters, and we also make sure that only delimiters
        // at the end of a line or the end of the string are used (row mode)
        try
        {
            statement = connection.createStatement();

            SqlTokenizer tokenizer = new SqlTokenizer(sql);

            while (tokenizer.hasMoreStatements())
            {
                String command = tokenizer.getNextStatement();
                
                // ignore whitespace
                command = command.trim();
                if (command.length() == 0)
                {
                    continue;
                }
                
                commandCount++;
                
                if (_log.isDebugEnabled())
                {
                    _log.debug("About to execute SQL " + command);
                }
                try
                {
                    int results = statement.executeUpdate(command);

                    if (_log.isDebugEnabled())
                    {
                        _log.debug("After execution, " + results + " row(s) have been changed");
                    }
                }
                catch (SQLException ex)
                {
                    if (continueOnError)
                    {
                        // Since the user deciced to ignore this error, we log the error
                        // on level warn, and the exception itself on level debug
                        _log.warn("SQL Command " + command + " failed with: " + ex.getMessage());
                        if (_log.isDebugEnabled())
                        {
                            _log.debug(ex);
                        }
                        errors++;
                    }
                    else
                    {
                        throw new DatabaseOperationException("Error while executing SQL "+command, ex);
                    }
                }

                // lets display any warnings
                SQLWarning warning = connection.getWarnings();

                while (warning != null)
                {
                    _log.warn(warning.toString());
                    warning = warning.getNextWarning();
                }
                connection.clearWarnings();
            }
            _log.info("Executed "+ commandCount + " SQL command(s) with " + errors + " error(s)");
        }
        catch (SQLException ex)
        {
            throw new DatabaseOperationException("Error while executing SQL", ex);
        }
        finally
        {
            closeStatement(statement);
        }

        return errors;
    }

    /**
     * {@inheritDoc}
     */
    public void shutdownDatabase() throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            shutdownDatabase(connection);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void shutdownDatabase(Connection connection) throws DatabaseOperationException
    {
        // Per default do nothing as most databases don't need this
    }

    /**
     * {@inheritDoc}
     */
    public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map parameters) throws DatabaseOperationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database creation is not supported for the database platform "+getName());
    }

    /**
     * {@inheritDoc}
     */
    public void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DatabaseOperationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Database deletion is not supported for the database platform "+getName());
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        createModel(model, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        createModel(model, params, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        createModel(connection, model, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void createTables(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        createModel(connection, model, params, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateTablesSql(Database model, boolean dropTablesFirst, boolean continueOnError)
    {
        return getCreateModelSql(model, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateTablesSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError)
    {
        return getCreateModelSql(model, params, dropTablesFirst, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void createModel(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            createModel(connection, model, dropTablesFirst, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createModel(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        String sql = getCreateModelSql(model, dropTablesFirst, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void createModel(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            createModel(connection, model, params, dropTablesFirst, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createModel(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException
    {
        String sql = getCreateModelSql(model, params, dropTablesFirst, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateModelSql(Database model, boolean dropTablesFirst, boolean continueOnError)
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().createTables(model, dropTablesFirst);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public String getCreateModelSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError)
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().createTables(model, params, dropTablesFirst);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * Returns the model comparator to be used for this platform. This method is intendeded
     * to be redefined by platforms that need to customize the model reader.
     * 
     * @return The model comparator
     */
    protected ModelComparator getModelComparator()
    {
        return new ModelComparator(getPlatformInfo(),
                                   getTableDefinitionChangesPredicate(),
                                   isDelimitedIdentifierModeOn());
    }

    /**
     * Returns the predicate that defines which changes are supported by the platform.
     * 
     * @return The predicate
     */
    protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate()
    {
        return new DefaultTableDefinitionChangesPredicate();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<? extends ModelChange> getChanges(Database currentModel, Database desiredModel)
    {
        var changes = getModelComparator().compare(currentModel, desiredModel);

        return sortChanges(changes);
    }

    /**
     * Sorts the changes so that they can be executed by the database. E.g. tables need to be created before
     * they can be referenced by foreign keys, indexes should be dropped before a table is dropped etc.
     * 
     * @param changes The original changes
     * @return The sorted changes - this can be the original list object or a new one
     */
    protected List<? extends ModelChange> sortChanges(List<? extends ModelChange> changes)
    {
        final Map<Class<?>, Integer> typeOrder = new HashMap<>();

        typeOrder.put(RemoveForeignKeyChange.class, 0);
        typeOrder.put(RemoveIndexChange.class,      1);
        typeOrder.put(RemoveTableChange.class,      2);
        typeOrder.put(RecreateTableChange.class,    3);
        typeOrder.put(RemovePrimaryKeyChange.class, 3);
        typeOrder.put(RemoveColumnChange.class,     4);
        typeOrder.put(ColumnDefinitionChange.class, 5);
        typeOrder.put(ColumnOrderChange.class,      5);
        typeOrder.put(AddColumnChange.class,        5);
        typeOrder.put(PrimaryKeyChange.class,       5);
        typeOrder.put(AddPrimaryKeyChange.class,    6);
        typeOrder.put(AddTableChange.class,         7);
        typeOrder.put(AddIndexChange.class,         8);
        typeOrder.put(AddForeignKeyChange.class,    9);

        changes.sort((objA, objB) -> {
			Integer orderValueA = typeOrder.get(objA.getClass());
			Integer orderValueB = typeOrder.get(objB.getClass());

			if (orderValueA == null) {
				return (orderValueB == null ? 0 : 1);
			} else if (orderValueB == null) {
				return -1;
			} else {
				return orderValueA.compareTo(orderValueB);
			}
		});
    	return changes;
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

            alterModel(currentModel, desiredModel, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

            alterModel(currentModel, desiredModel, params, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(String catalog, String schema, List<String> tableTypes, Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

            alterModel(currentModel, desiredModel, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(String catalog, String schema, List<String> tableTypes, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

            alterModel(currentModel, desiredModel, params, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

        alterModel(currentModel, desiredModel, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

        alterModel(currentModel, desiredModel, params, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

        alterModel(currentModel, desiredModel, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterTables(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

        alterModel(currentModel, desiredModel, params, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Database desiredModel) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

            return getAlterModelSql(currentModel, desiredModel);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Database desiredModel, CreationParameters params) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

            return getAlterModelSql(currentModel, desiredModel, params);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(String catalog, String schema, List<String> tableTypes, Database desiredModel) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

            return getAlterModelSql(currentModel, desiredModel);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(String catalog, String schema, List<String> tableTypes, Database desiredModel, CreationParameters params) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

            return getAlterModelSql(currentModel, desiredModel, params);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, Database desiredModel) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

        return getAlterModelSql(currentModel, desiredModel);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, Database desiredModel, CreationParameters params) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

        return getAlterModelSql(currentModel, desiredModel, params);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredModel) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

        return getAlterModelSql(currentModel, desiredModel);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterTablesSql(Connection connection, String catalog, String schema, List<String> tableTypes, Database desiredModel, CreationParameters params) throws DatabaseOperationException
    {
        Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

        return getAlterModelSql(currentModel, desiredModel, params);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterModelSql(Database currentModel, Database desiredModel) throws DatabaseOperationException
    {
        return getAlterModelSql(currentModel, desiredModel, null);
    }

    /**
     * {@inheritDoc}
     */
    public String getAlterModelSql(Database currentModel, Database desiredModel, CreationParameters params) throws DatabaseOperationException
    {
        List   changes = getChanges(currentModel, desiredModel);
        String sql     = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            processChanges(currentModel, changes, params);
            sql = buffer.toString();
        }
        catch (IOException ex)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void alterModel(Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            alterModel(connection, currentModel, desiredModel, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterModel(Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            alterModel(connection, currentModel, desiredModel, params, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void alterModel(Connection connection, Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException
    {
        String sql = getAlterModelSql(currentModel, desiredModel);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void alterModel(Connection connection, Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException
    {
        String sql = getAlterModelSql(currentModel, desiredModel, params);

        evaluateBatch(connection, sql, continueOnError);
    }

	/**
     * {@inheritDoc}
     */
    public void dropTable(Connection connection, Database model, Table table, boolean continueOnError) throws DatabaseOperationException
    {
        String sql = getDropTableSql(model, table, continueOnError);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Database model, Table table, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            dropTable(connection, model, table, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDropTableSql(Database model, Table table, boolean continueOnError)
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().dropTable(model, table);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public void dropTables(Database model, boolean continueOnError) throws DatabaseOperationException
    {
        dropModel(model, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public void dropTables(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException
    {
        dropModel(connection, model, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getDropTablesSql(Database model, boolean continueOnError)
    {
        return getDropModelSql(model);
    }

    /**
     * {@inheritDoc}
     */
    public void dropModel(Database model, boolean continueOnError) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            dropModel(connection, model, continueOnError);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropModel(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException 
    {
        String sql = getDropModelSql(model);

        evaluateBatch(connection, sql, continueOnError);
    }

    /**
     * {@inheritDoc}
     */
    public String getDropModelSql(Database model) 
    {
        String sql = null;

        try
        {
            StringWriter buffer = new StringWriter();

            getSqlBuilder().setWriter(buffer);
            getSqlBuilder().dropTables(model);
            sql = buffer.toString();
        }
        catch (IOException e)
        {
            // won't happen because we're using a string writer
        }
        return sql;
    }

    /**
     * Processes the given changes in the specified order. Basically, this method finds the
     * appropriate handler method (one of the <code>processChange</code> methods) defined in
     * the concrete sql builder for each change, and invokes it.
     * 
     * @param model   The database model; this object is not going to be changed by this method
     * @param changes The changes
     * @param params  The parameters used in the creation of new tables. Note that for existing
     *                tables, the parameters won't be applied
     * @return The changed database model
     */
    protected Database processChanges(Database           model,
                                      Collection<? extends ModelChange>         changes,
                                      CreationParameters params) throws IOException, DdlUtilsException
    {
        Database currentModel = new CloneHelper().clone(model);

		for (final var change : changes) {
			invokeChangeHandler(currentModel, params, change);
		}
        return currentModel;
    }

    /**
     * Invokes the change handler (one of the <code>processChange</code> methods) for the given
     * change object.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    private void invokeChangeHandler(Database           currentModel,
                                     CreationParameters params,
                                     ModelChange        change) throws IOException
    {
        Class curClass = getClass();

        // find the handler for the change
        while ((curClass != null) && !Object.class.equals(curClass))
        {
            try
            {
                Method method = null;

                try
                {
                    method = curClass.getDeclaredMethod("processChange",
                                                        new Class[] { Database.class,
                                                                      CreationParameters.class,
                                                                      change.getClass() });
                }
                catch (NoSuchMethodException ex)
                {
                    // we actually expect this one
                }

                if (method != null)
                {
                    method.invoke(this, new Object[] { currentModel, params, change });
                    return;
                }
                else
                {
                    curClass = curClass.getSuperclass();
                }
            }
            catch (InvocationTargetException ex)
            {
                if (ex.getTargetException() instanceof IOException)
                {
                    throw (IOException)ex.getTargetException();
                }
                else
                {
                    throw new DdlUtilsException(ex.getTargetException());
                }
            }
            catch (Exception ex)
            {
                throw new DdlUtilsException(ex);
            }
        }
        throw new DdlUtilsException("No handler for change of type " + change.getClass().getName() + " defined");
    }

    /**
     * Finds the table changed by the change object in the given model.
     *  
     * @param currentModel The model to find the table in
     * @param change       The table change
     * @return The table
     * @throws ModelException If the table could not be found
     */
    protected Table findChangedTable(Database currentModel, TableChange change) throws ModelException
    {
        return currentModel.findTable(change.getChangedTable(), getPlatformInfo().isDelimitedIdentifiersSupported())
			.orElseThrow(() -> new ModelException("Could not find table " + change.getChangedTable() + " in the given model"));
    }

    /**
     * Finds the index changed by the change object in the given model.
     *  
     * @param currentModel The model to find the index in
     * @param change       The index change
     * @return The index
     * @throws ModelException If the index could not be found
     */
    protected Index findChangedIndex(Database currentModel, IndexChange change) throws ModelException
    {
        Index index = change.findChangedIndex(currentModel,
                                              getPlatformInfo().isDelimitedIdentifiersSupported());

        if (index == null)
        {
            throw new ModelException("Could not find the index to change in table " + change.getChangedTable() + " in the given model");
        }
        else
        {
            return index;
        }
    }

    /**
     * Finds the foreign key changed by the change object in the given model.
     *  
     * @param currentModel The model to find the foreign key in
     * @param change       The foreign key change
     * @return The foreign key
     * @throws ModelException If the foreign key could not be found
     */
    protected ForeignKey findChangedForeignKey(Database currentModel, ForeignKeyChange change) throws ModelException
    {
        ForeignKey fk = change.findChangedForeignKey(currentModel,
                                                     getPlatformInfo().isDelimitedIdentifiersSupported());

        if (fk == null)
        {
            throw new ModelException("Could not find the foreign key to change in table " + change.getChangedTable() + " in the given model");
        }
        else
        {
            return fk;
        }
    }

    /**
     * Processes a change representing the addition of a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              AddTableChange     change) throws IOException
    {
        getSqlBuilder().createTable(currentModel,
                                    change.getNewTable(),
                                    params == null ? null : params.getParametersFor(change.getNewTable()));
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the removal of a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              RemoveTableChange  change) throws IOException, ModelException
    {
        Table changedTable = findChangedTable(currentModel, change);

        getSqlBuilder().dropTable(changedTable);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the addition of a foreign key.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database            currentModel,
                              CreationParameters  params,
                              AddForeignKeyChange change) throws IOException
    {
        Table changedTable = findChangedTable(currentModel, change);

        getSqlBuilder().createForeignKey(currentModel,
                                         changedTable,
                                         change.getNewForeignKey());
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the removal of a foreign key.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database               currentModel,
                              CreationParameters     params,
                              RemoveForeignKeyChange change) throws IOException, ModelException
    {
        Table      changedTable = findChangedTable(currentModel, change);
        ForeignKey changedFk    = findChangedForeignKey(currentModel, change);

        getSqlBuilder().dropForeignKey(changedTable, changedFk);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the addition of an index.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              AddIndexChange     change) throws IOException
    {
        Table changedTable = findChangedTable(currentModel, change);

        getSqlBuilder().createIndex(changedTable, change.getNewIndex());
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the removal of an index.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              RemoveIndexChange  change) throws IOException, ModelException
    {
        Table changedTable = findChangedTable(currentModel, change);
        Index changedIndex = findChangedIndex(currentModel, change);

        getSqlBuilder().dropIndex(changedTable, changedIndex);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the addition of a column.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database           currentModel,
                              CreationParameters params,
                              AddColumnChange    change) throws IOException
    {
        Table changedTable = findChangedTable(currentModel, change);

        getSqlBuilder().addColumn(currentModel, changedTable, change.getNewColumn());
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the addition of a primary key.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database            currentModel,
                              CreationParameters  params,
                              AddPrimaryKeyChange change) throws IOException
    {
        Table    changedTable  = findChangedTable(currentModel, change);
        var pkColumnNames = change.getPrimaryKeyColumns();
        var pkColumns     = pkColumnNames.stream()
			.map(keyColumn -> changedTable.findColumn(keyColumn, isDelimitedIdentifierModeOn()).orElseThrow())
			.toList();

        getSqlBuilder().createPrimaryKey(changedTable, pkColumns);
        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }

    /**
     * Processes a change representing the recreation of a table.
     * 
     * @param currentModel The current database schema
     * @param params       The parameters used in the creation of new tables. Note that for existing
     *                     tables, the parameters won't be applied
     * @param change       The change object
     */
    public void processChange(Database            currentModel,
                              CreationParameters  params,
                              RecreateTableChange change) throws IOException
    {
        // we can only copy the data if no required columns without default value and
        // non-autoincrement have been added
        boolean canMigrateData = true;

        for (Iterator it = change.getOriginalChanges().iterator(); canMigrateData && it.hasNext();)
        {
            TableChange curChange = (TableChange)it.next();

            if (curChange instanceof AddColumnChange)
            {
                AddColumnChange addColumnChange = (AddColumnChange)curChange;

                if (addColumnChange.getNewColumn().isRequired() &&
                    !addColumnChange.getNewColumn().isAutoIncrement() &&
                    (addColumnChange.getNewColumn().getDefaultValue() == null))
                {
                    _log.warn("Data cannot be retained in table " + change.getChangedTable() + 
                              " because of the addition of the required column " + addColumnChange.getNewColumn().getName());
                    canMigrateData = false;
                }
            }
        }

        Table changedTable = findChangedTable(currentModel, change);
        Table targetTable  = change.getTargetTable();
        Map   parameters   = (params == null ? null : params.getParametersFor(targetTable));

        if (canMigrateData)
        {
            Table tempTable = getTemporaryTableFor(targetTable);

            getSqlBuilder().createTemporaryTable(currentModel, tempTable, parameters);
            getSqlBuilder().copyData(changedTable, tempTable);
            // Note that we don't drop the indices here because the DROP TABLE will take care of that
            // Likewise, foreign keys have already been dropped as necessary
            getSqlBuilder().dropTable(changedTable);
            getSqlBuilder().createTable(currentModel, targetTable, parameters);
            getSqlBuilder().copyData(tempTable, targetTable);
            getSqlBuilder().dropTemporaryTable(currentModel, tempTable);
        }
        else
        {
            getSqlBuilder().dropTable(changedTable);
            getSqlBuilder().createTable(currentModel, targetTable, parameters);
        }

        change.apply(currentModel, isDelimitedIdentifierModeOn());
    }
    
    /**
     * Creates a temporary table object that corresponds to the given table.
     * Database-specific implementations may redefine this method if e.g. the
     * database directly supports temporary tables. The default implementation
     * simply appends an underscore to the table name and uses that as the
     * table name.  
     * 
     * @param targetTable The target table
     * @return The temporary table
     */
    protected Table getTemporaryTableFor(Table targetTable)
    {
        CloneHelper cloneHelper = new CloneHelper();
        Table       table       = new Table();

        table.setCatalog(targetTable.getCatalog());
        table.setSchema(targetTable.getSchema());
        table.setName(targetTable.getName() + "_");
        table.setType(targetTable.getType());
        for (int idx = 0; idx < targetTable.getColumnCount(); idx++)
        {
            // TODO: clone PK status ?
            table.addColumn(cloneHelper.clone(targetTable.getColumn(idx), true));
        }

        return table;
    }

    /**
     * Allows platforms to issue statements directly before rows are inserted into
     * the specified table.
     *  
     * @param connection The connection used for the insertion
     * @param table      The table that the rows are inserted into
     */
    protected void beforeInsert(Connection connection, Table table) throws SQLException
    {
    }
    
    /**
     * Allows platforms to issue statements directly after rows have been inserted into
     * the specified table.
     *  
     * @param connection The connection used for the insertion
     * @param table      The table that the rows have been inserted into
     */
    protected void afterInsert(Connection connection, Table table) throws SQLException
    {
    }

	/**
	 * Creates the SQL for updating an object of the given type. If a concrete bean is given,
	 * then a concrete update statement is created, otherwise an update statement usable in a
	 * prepared statement is build.
	 *
	 * @param table   The type
	 * @param columnValues The primary keys
	 * @return The SQL required to update the instance
	 */
	protected String createUpdateSql(Table table, Map<String, Object> columnValues)
	{
		return _builder.getUpdateSql(table, columnValues, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(Connection connection, Database model, Table table, Map<String, Object> dynaBean) throws DatabaseOperationException
	{
		List<Column> primaryKeys = table.getPrimaryKeyColumns().toList();

		if (primaryKeys.isEmpty())
		{
			throw new RuntimeException("Cannot update instances of type " + table.getName() + " because it has no primary keys");
		}

		Set<String> nonPrimaryColNames = table.getColumns()
			.stream()
			.filter(col -> !col.isPrimaryKey())
			.map(Column::getName)
			.collect(Collectors.toSet());

		List<Column> nonPrimaryCols = table.getColumns()
			.stream()
			.filter(col -> !col.isPrimaryKey())
			.toList();

		var propertiesToUpdate = dynaBean.entrySet()
			.stream()
			.filter(e -> nonPrimaryColNames.contains(e.getKey()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		String sql = createUpdateSql(table, propertiesToUpdate);
		PreparedStatement statement  = null;

		if (_log.isDebugEnabled())
		{
			_log.debug("About to execute SQL: " + sql);
		}
		try
		{
			beforeUpdate(connection, table);

			statement = connection.prepareStatement(sql);

			int sqlIndex = 1;

			for (int idx = 0; idx < nonPrimaryCols.size(); idx++)
			{
				setObject(statement, sqlIndex++, dynaBean, nonPrimaryCols);
			}
			for (int idx = 0; idx < primaryKeys.size(); idx++)
			{
				setObject(statement, sqlIndex++, dynaBean, primaryKeys);
			}

			int count = statement.executeUpdate();

			afterUpdate(connection, table);

			if (count != 1)
			{
				_log.warn("Attempted to insert a single row " + dynaBean +
					" into table " + table.getName() +
					" but changed " + count + " row(s)");
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseOperationException("Error while updating in the database", ex);
		}
		finally
		{
			closeStatement(statement);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(Database model, Table table, Map<String, Object> dynaBean) throws DatabaseOperationException
	{
		Connection connection = borrowConnection();

		try
		{
			update(connection, model, table, dynaBean);
		}
		finally
		{
			returnConnection(connection);
		}
	}

    /**
     * Allows platforms to issue statements directly before rows are updated in
     * the specified table.
     *  
     * @param connection The connection used for the update
     * @param table      The table that the rows are updateed into
     */
    protected void beforeUpdate(Connection connection, Table table) throws SQLException
    {
    }
    
    /**
     * Allows platforms to issue statements directly after rows have been updated in
     * the specified table.
     *  
     * @param connection The connection used for the update
     * @param table      The table that the rows have been updateed into
     */
    protected void afterUpdate(Connection connection, Table table) throws SQLException
    {
    }

	/**
	 * {@inheritDoc}
	 */
	public void delete(Database model, String tableName, Map<String, Object> dynaBean) throws DatabaseOperationException
	{
		Connection connection = borrowConnection();

		try
		{
			delete(connection, model, tableName, dynaBean);
		}
		finally
		{
			returnConnection(connection);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(Connection connection, Database model, String tableName, Map<String, Object> dynaBean) throws DatabaseOperationException
	{
		PreparedStatement statement  = null;
		Table table = model.findTable(tableName).orElseThrow();

		try
		{
			var primaryKeys = table.getPrimaryKeyColumns()
				.toList();

			if (primaryKeys.isEmpty())
			{
				_log.warn("Cannot delete instances of type " + tableName + " because it has no primary keys");
				return;
			}

			var primaryKeyNames = primaryKeys.stream()
				.map(Column::getName)
				.collect(Collectors.toSet());

			var pkValues = dynaBean.entrySet()
				.stream()
				.filter(e -> primaryKeyNames.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			String sql = _builder.getDeleteSql(table, pkValues, false);

			if (_log.isDebugEnabled())
			{
				_log.debug("About to execute SQL " + sql);
			}

			statement = connection.prepareStatement(sql);

			for (int idx = 0; idx < primaryKeys.size(); idx++)
			{
				setObject(statement, idx, dynaBean, primaryKeys);
			}

			int count = statement.executeUpdate();

			if (count != 1)
			{
				_log.warn("Attempted to delete a single row " + dynaBean +
					" in table " + tableName +
					" but changed " + count + " row(s).");
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseOperationException("Error while deleting from the database", ex);
		}
		finally
		{
			closeStatement(statement);
		}
	}

    /**
     * {@inheritDoc}
     */    
    public Database readModelFromDatabase(String name) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            return readModelFromDatabase(connection, name);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */    
    public Database readModelFromDatabase(Connection connection, String name) throws DatabaseOperationException
    {
        try
        {
            Database model = getModelReader().getDatabase(connection, name);

            postprocessModelFromDatabase(model);
            return model;
        }
        catch (SQLException ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Database readModelFromDatabase(String name, String catalog, String schema, List<String> tableTypes) throws DatabaseOperationException
    {
        Connection connection = borrowConnection();

        try
        {
            return readModelFromDatabase(connection, name, catalog, schema, tableTypes);
        }
        finally
        {
            returnConnection(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Database readModelFromDatabase(Connection connection, String name, String catalog, String schema, List<String> tableTypes) throws DatabaseOperationException
    {
        try
        {
            JdbcModelReader reader = getModelReader();
            Database        model  = reader.getDatabase(connection, name, catalog, schema, tableTypes);

            postprocessModelFromDatabase(model);
            if ((model.getName() == null) || (model.getName().length() == 0))
            {
                model.setName(MODEL_DEFAULT_NAME);
            }
            return model;
        }
        catch (SQLException ex)
        {
            throw new DatabaseOperationException(ex);
        }
    }

    /**
     * Allows the platform to postprocess the model just read from the database.
     * 
     * @param model The model
     */
    protected void postprocessModelFromDatabase(Database model)
    {
        // Default values for CHAR/VARCHAR/LONGVARCHAR columns have quotation marks
        // around them which we'll remove now
        for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++)
        {
            Table table = model.getTable(tableIdx);

            for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++)
            {
                Column column = table.getColumn(columnIdx);

                if (TypeMap.isTextType(column.getTypeCode()) ||
                    TypeMap.isDateTimeType(column.getTypeCode()))
                {
                    String defaultValue = column.getDefaultValue();

                    if ((defaultValue != null) && (defaultValue.length() >= 2) &&
                        defaultValue.startsWith("'") && defaultValue.endsWith("'"))
                    {
                        defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                        column.setDefaultValue(defaultValue);
                    }
                }
            }
        }
    }

	/**
	 * This is the core method to set the parameter of a prepared statement to a given value.
	 * The primary purpose of this method is to call the appropriate method on the statement,
	 * and to give database-specific implementations the ability to change this behavior.
	 * 
	 * @param statement The statement
	 * @param sqlIndex  The parameter index
	 * @param typeCode  The JDBC type code
	 * @param value     The value
	 * @throws SQLException If an error occurred while setting the parameter value
	 */
	protected void setStatementParameterValue(PreparedStatement statement, int sqlIndex, int typeCode, Object value) throws SQLException
	{
		if (value == null)
        {
            statement.setNull(sqlIndex, typeCode);
        }
        else if (value instanceof String)
        {
            statement.setString(sqlIndex, (String)value);
        }
        else if (value instanceof byte[])
        {
            statement.setBytes(sqlIndex, (byte[])value);
        }
        else if (value instanceof Boolean)
        {
            statement.setBoolean(sqlIndex, ((Boolean)value).booleanValue());
        }
        else if (value instanceof Byte)
        {
            statement.setByte(sqlIndex, ((Byte)value).byteValue());
        }
        else if (value instanceof Short)
        {
            statement.setShort(sqlIndex, ((Short)value).shortValue());
        }
        else if (value instanceof Integer)
        {
            statement.setInt(sqlIndex, ((Integer)value).intValue());
        }
        else if (value instanceof Long)
        {
            statement.setLong(sqlIndex, ((Long)value).longValue());
        }
        else if (value instanceof BigDecimal)
        {
            // setObject assumes a scale of 0, so we rather use the typed setter
            statement.setBigDecimal(sqlIndex, (BigDecimal)value);
        }
        else if (value instanceof Float)
        {
            statement.setFloat(sqlIndex, ((Float)value).floatValue());
        }
        else if (value instanceof Double)
        {
            statement.setDouble(sqlIndex, ((Double)value).doubleValue());
        }
        else
        {
            statement.setObject(sqlIndex, value, typeCode);
        }
	}

	/**
	 * This is the core method to retrieve a value for a column from a result set. Its  primary
	 * purpose is to call the appropriate method on the result set, and to provide an extension
	 * point where database-specific implementations can change this behavior.
	 * 
	 * @param resultSet  The result set to extract the value from
	 * @param columnName The name of the column; can be <code>null</code> in which case the
     *                   <code>columnIdx</code> will be used instead
     * @param columnIdx  The index of the column's value in the result set; is only used if
     *                   <code>columnName</code> is <code>null</code>
	 * @param jdbcType   The jdbc type to extract
	 * @return The value
	 * @throws SQLException If an error occurred while accessing the result set
	 */
	protected Object extractColumnValue(ResultSet resultSet, String columnName, int columnIdx, int jdbcType) throws SQLException
	{
        boolean useIdx = (columnName == null);
		Object  value;

		switch (jdbcType)
		{
		    case Types.CHAR:
		    case Types.VARCHAR:
		    case Types.LONGVARCHAR:
		        value = useIdx ? resultSet.getString(columnIdx) : resultSet.getString(columnName);
		        break;
		    case Types.NUMERIC:
		    case Types.DECIMAL:
		        value = useIdx ? resultSet.getBigDecimal(columnIdx) : resultSet.getBigDecimal(columnName);
		        break;
		    case Types.BIT:
            case Types.BOOLEAN:
		        value = useIdx ? resultSet.getBoolean(columnIdx) : resultSet.getBoolean(columnName);
		        break;
		    case Types.TINYINT:
		    case Types.SMALLINT:
		    case Types.INTEGER:
		        value = useIdx ? resultSet.getInt(columnIdx) : resultSet.getInt(columnName);
		        break;
		    case Types.BIGINT:
		        value = useIdx ? resultSet.getLong(columnIdx) : resultSet.getLong(columnName);
		        break;
		    case Types.REAL:
		        value = useIdx ? resultSet.getFloat(columnIdx) : resultSet.getFloat(columnName);
		        break;
		    case Types.FLOAT:
		    case Types.DOUBLE:
		        value = useIdx ? resultSet.getDouble(columnIdx) : resultSet.getDouble(columnName);
		        break;
		    case Types.BINARY:
		    case Types.VARBINARY:
		    case Types.LONGVARBINARY:
		        value = useIdx ? resultSet.getBytes(columnIdx) : resultSet.getBytes(columnName);
		        break;
		    case Types.DATE:
		        value = useIdx ? resultSet.getDate(columnIdx) : resultSet.getDate(columnName);
		        break;
		    case Types.TIME:
		        value = useIdx ? resultSet.getTime(columnIdx) : resultSet.getTime(columnName);
		        break;
		    case Types.TIMESTAMP:
		        value = useIdx ? resultSet.getTimestamp(columnIdx) : resultSet.getTimestamp(columnName);
		        break;
		    case Types.CLOB:
		        Clob clob = useIdx ? resultSet.getClob(columnIdx) : resultSet.getClob(columnName);

                if (clob == null)
                {
                    value = null;
                }
                else
                {
                    long length = clob.length();
    
    		        if (length > Integer.MAX_VALUE)
    		        {
    		            value = clob;
    		        }
                    else if (length == 0)
                    {
                        // the javadoc is not clear about whether Clob.getSubString
                        // can be used with a substring length of 0
                        // thus we do the safe thing and handle it ourselves
                        value = "";
                    }
    		        else
    		        {
    		            value = clob.getSubString(1l, (int)length);
    		        }
                }
		        break;
		    case Types.BLOB:
		        Blob blob = useIdx ? resultSet.getBlob(columnIdx) : resultSet.getBlob(columnName);

                if (blob == null)
                {
                    value = null;
                }
                else
                {
                    long length = blob.length();
    
    		        if (length > Integer.MAX_VALUE)
    		        {
    		            value = blob;
    		        }
                    else if (length == 0)
                    {
                        // the javadoc is not clear about whether Blob.getBytes
                        // can be used with for 0 bytes to be copied
                        // thus we do the safe thing and handle it ourselves
                        value = new byte[0];
                    }
    		        else
    		        {
    		            value = blob.getBytes(1l, (int)length);
    		        }
                }
		        break;
		    case Types.ARRAY:
		        value = useIdx ? resultSet.getArray(columnIdx) : resultSet.getArray(columnName);
		        break;
		    case Types.REF:
		        value = useIdx ? resultSet.getRef(columnIdx) : resultSet.getRef(columnName);
		        break;
		    default:
	            value = useIdx ? resultSet.getObject(columnIdx) : resultSet.getObject(columnName);
		        break;
		}
        return resultSet.wasNull() ? null : value;
	}

	/**
	 * Returns all identity properties whose value were defined by the database and which
	 * now need to be read back from the DB.
	 *
	 * @return The columns
	 */
	private List<Column> getRelevantIdentityColumns(Table table, Map<String, Object> data)
	{
		return data.keySet()
			.stream()
			.map(col -> table.findColumn(col).orElseThrow())
			.filter(col -> col.isAutoIncrement() && (!isIdentityOverrideOn() || !getPlatformInfo().isIdentityOverrideAllowed()))
			.toList();
	}

	/**
	 * Helper method esp. for the {@link ModelBasedResultSetIterator} class that retrieves
	 * the value for a column from the given result set. If a table was specified,
	 * and it contains the column, then the jdbc type defined for the column is used for extracting
	 * the value, otherwise the object directly retrieved from the result set is returned.<br/>
	 * The method is defined here rather than in the {@link ModelBasedResultSetIterator} class
	 * so that concrete platforms can modify its behavior.
	 *
	 * @param resultSet  The result set
	 * @param columnName The name of the column
	 * @param table      The table
	 * @return The value
	 */
	protected Object getObjectFromResultSet(ResultSet resultSet, String columnName, Table table) throws SQLException
	{
		Column column = (table == null ? null : table.findColumn(columnName, isDelimitedIdentifierModeOn()).orElse(null));
		Object value  = null;

		if (column != null)
		{
			int originalJdbcType = column.getTypeCode();
			int targetJdbcType   = getPlatformInfo().getTargetJdbcType(originalJdbcType);
			int jdbcType         = originalJdbcType;

			// in general we're trying to retrieve the value using the original type
			// but sometimes we also need the target type:
			if ((originalJdbcType == Types.BLOB) && (targetJdbcType != Types.BLOB))
			{
				// we should not use the Blob interface if the database doesn't map to this type
				jdbcType = targetJdbcType;
			}
			if ((originalJdbcType == Types.CLOB) && (targetJdbcType != Types.CLOB))
			{
				// we should not use the Clob interface if the database doesn't map to this type
				jdbcType = targetJdbcType;
			}
			value = extractColumnValue(resultSet, columnName, 0, jdbcType);
		}
		else
		{
			value = resultSet.getObject(columnName);
		}
		return resultSet.wasNull() ? null : value;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> insert(Connection connection, Database model, String tableName, Map<String, Object> columnValues) throws DatabaseOperationException
	{
		var table = model.findTable(tableName).orElseThrow();

		var properties = columnValues.keySet()
			.stream()
			.map(col -> table.findColumn(col).orElseThrow())
			.toList();

		var autoIncrColumns = getRelevantIdentityColumns(table, columnValues);

		String insertSql        = _builder.getInsertSql(table, columnValues, false);
		String queryIdentitySql = null;

		var result = new HashMap<>(columnValues);

		if (_log.isDebugEnabled())
		{
			_log.debug("About to execute SQL: " + insertSql);
		}

		if (!autoIncrColumns.isEmpty())
		{
			if (!getPlatformInfo().isLastIdentityValueReadable())
			{
				_log.warn("The database does not support querying for auto-generated column values");
			}
			else
			{
				queryIdentitySql = _builder.getSelectLastIdentityValues(table);
			}
		}

		boolean           autoCommitMode = false;
		PreparedStatement statement      = null;

		try
		{
			if (!getPlatformInfo().isAutoCommitModeForLastIdentityValueReading())
			{
				autoCommitMode = connection.getAutoCommit();
				connection.setAutoCommit(false);
			}

			beforeInsert(connection, table);

			statement = connection.prepareStatement(insertSql);

			for (int idx = 0; idx < properties.size(); idx++ )
			{
				setObject(statement, idx, columnValues, properties);
			}

			int count = statement.executeUpdate();

			afterInsert(connection, table);

			if (count != 1)
			{
				_log.warn("Attempted to insert a single row " + columnValues +
					" in table " + tableName +
					" but changed " + count + " row(s)");
			}
		}
		catch (SQLException ex)
		{
			throw new DatabaseOperationException("Error while inserting into the database: " + ex.getMessage(), ex);
		}
		finally
		{
			closeStatement(statement);
		}
		if (queryIdentitySql != null)
		{
			Statement queryStmt       = null;
			ResultSet lastInsertedIds = null;

			try
			{
				if (getPlatformInfo().isAutoCommitModeForLastIdentityValueReading())
				{
					// we'll commit the statement(s) if no auto-commit is enabled because
					// otherwise it is possible that the auto increment hasn't happened yet
					// (the db didn't actually perform the insert yet so no triggering of
					// sequences did occur)
					if (!connection.getAutoCommit())
					{
						connection.commit();
					}
				}

				queryStmt       = connection.createStatement();
				lastInsertedIds = queryStmt.executeQuery(queryIdentitySql);

				lastInsertedIds.next();

				for (Column autoIncrColumn : autoIncrColumns) {
					// we're using the index rather than the name because we cannot know how
					// the SQL statement looks like; rather we assume that we get the values
					// back in the same order as the auto increment columns
					Object value = getObjectFromResultSet(lastInsertedIds, autoIncrColumn.getName(), table);
					result.put(autoIncrColumn.getName(), value);
				}
			}
			catch (SQLException ex)
			{
				throw new DatabaseOperationException("Error while retrieving the identity column value(s) from the database", ex);
			}
			finally
			{
				if (lastInsertedIds != null)
				{
					try
					{
						lastInsertedIds.close();
					}
					catch (SQLException ex)
					{
						// we ignore this one
					}
				}
				closeStatement(statement);
			}
		}
		if (!getPlatformInfo().isAutoCommitModeForLastIdentityValueReading())
		{
			try
			{
				// we need to do a manual commit now
				connection.commit();
				connection.setAutoCommit(autoCommitMode);
			}
			catch (SQLException ex)
			{
				throw new DatabaseOperationException(ex);
			}
		}
		return result;
	}

	private void setObject(
		final PreparedStatement statement,
		final int idx,
		final Map<String, Object> columnValues,
		final List<Column> properties
	) throws SQLException
	{
		int     typeCode = properties.get(idx).getTypeCode();
		Object  value    = columnValues.get(properties.get(idx).getName());

		setStatementParameterValue(statement, idx + 1, typeCode, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> insert(Database model, String table, Map<String, Object> data) throws DatabaseOperationException
	{
		Connection connection = borrowConnection();

		try
		{
			return insert(connection, model, table, data);
		}
		finally
		{
			returnConnection(connection);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> fetch(Database model, String sql, @Nullable Collection<Object> parameters, List<Table> queryHints, int start, int end) throws DatabaseOperationException
	{
		Connection        connection = borrowConnection();
		PreparedStatement statement  = null;
		ResultSet         resultSet  = null;
		var result = new ArrayList<Map<String, Object>>();

		try
		{
			statement = connection.prepareStatement(sql);
			if (parameters != null) {
				int paramIdx = 1;

				for (var iter = parameters.iterator(); iter.hasNext(); paramIdx++)
				{
					Object arg = iter.next();

					if (arg instanceof BigDecimal)
					{
						// to avoid scale problems because setObject assumes a scale of 0
						statement.setBigDecimal(paramIdx, (BigDecimal)arg);
					}
					else
					{
						statement.setObject(paramIdx, arg);
					}
				}
			}
			resultSet = statement.executeQuery();

			int rowIdx = 0;

			for (var it = createResultSetIterator(model, resultSet, queryHints); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++)
			{
				if (rowIdx >= start)
				{
					result.add(it.next());
				}
				else
				{
					it.advance();
				}
			}
		}
		catch (SQLException ex)
		{
			// any other exception comes from the iterator which closes the resources automatically
			closeStatement(statement);
			returnConnection(connection);
			throw new DatabaseOperationException("Error while fetching data from the database", ex);
		}
		return result;
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	public void insert(Connection connection, Database model, List<Map<String, Object>> dynaBeans) throws DatabaseOperationException
//	{
//		SqlDynaClass      dynaClass              = null;
//		SqlDynaProperty[] properties             = null;
//		PreparedStatement statement              = null;
//		int               addedStmts             = 0;
//		boolean           identityWarningPrinted = false;
//
//		for (Iterator it = dynaBeans.iterator(); it.hasNext();)
//		{
//			DynaBean     dynaBean     = (DynaBean)it.next();
//			SqlDynaClass curDynaClass = model.getDynaClassFor(dynaBean);
//
//			if (curDynaClass != dynaClass)
//			{
//				if (dynaClass != null)
//				{
//					executeBatch(statement, addedStmts, dynaClass.getTable());
//					addedStmts = 0;
//				}
//
//				dynaClass  = curDynaClass;
//				properties = getPropertiesForInsertion(model, curDynaClass, dynaBean);
//
//				if (properties.length == 0)
//				{
//					_log.warn("Cannot insert instances of type " + dynaClass + " because it has no usable properties");
//					continue;
//				}
//				if (!identityWarningPrinted &&
//					(getRelevantIdentityColumns(model, curDynaClass, dynaBean).length > 0))
//				{
//					_log.warn("Updating the bean properties corresponding to auto-increment columns is not supported in batch mode");
//					identityWarningPrinted = true;
//				}
//
//				String insertSql = createInsertSql(model, dynaClass, properties, null);
//
//				if (_log.isDebugEnabled())
//				{
//					_log.debug("Starting new batch with SQL: " + insertSql);
//				}
//				try
//				{
//					statement = connection.prepareStatement(insertSql);
//				}
//				catch (SQLException ex)
//				{
//					throw new DatabaseOperationException("Error while preparing insert statement", ex);
//				}
//			}
//			try
//			{
//				for (int idx = 0; idx < properties.length; idx++ )
//				{
//					setObject(statement, idx + 1, dynaBean, properties[idx]);
//				}
//				statement.addBatch();
//				addedStmts++;
//			}
//			catch (SQLException ex)
//			{
//				throw new DatabaseOperationException("Error while adding batch insert", ex);
//			}
//		}
//		if (dynaClass != null)
//		{
//			executeBatch(statement, addedStmts, dynaClass.getTable());
//		}
//	}


	/**
	 * Creates an iterator over the given result set.
	 *
	 * @param model      The database model
	 * @param resultSet  The result set to iterate over
	 * @param queryHints The tables that were queried in the query that produced the
	 *                   given result set (optional)
	 * @return The iterator
	 */
	protected ModelBasedResultSetIterator createResultSetIterator(Database model, ResultSet resultSet, List<Table> queryHints)
	{
		return new ModelBasedResultSetIterator(this, model, resultSet, queryHints, true);
	}
}
