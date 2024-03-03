package org.apache.ddlutils.alteration;

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

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the addition of a primary key to a table which does not have one.
 * 
 * @version $Revision: $
 */
public class AddPrimaryKeyChange extends TableChangeImplBase
{
    /** The names of the columns making up the primary key. */
    private List<String> _primaryKeyColumns;

    /**
     * Creates a new change object.
     * 
     * @param tableName         The name of the table to add the primary key to
     * @param primaryKeyColumns The names of the columns making up the primary key
     */
    public AddPrimaryKeyChange(String tableName, List<String> primaryKeyColumns)
    {
        super(tableName);
        if (primaryKeyColumns == null)
        {
            _primaryKeyColumns = List.of();
        }
        else
        {
            _primaryKeyColumns = new ArrayList<>(primaryKeyColumns);
        }
    }

    /**
     * Returns the primary key column names making up the new primary key.
     *
     * @return The primary key column names
     */
    public List<String> getPrimaryKeyColumns()
    {
        return _primaryKeyColumns;
    }

    /**
     * {@inheritDoc}
     */
    public void apply(Database model, boolean caseSensitive)
    {
        Table table = findChangedTable(model, caseSensitive);

		for (String primaryKeyColumn : _primaryKeyColumns) {
			Column column = table.findColumn(primaryKeyColumn, caseSensitive)
				.orElseThrow();
			column.setPrimaryKey(true);
		}
    }
}
