package org.apache.ddlutils.model;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Test case for DDLUTILS-6.
 * 
 * @version $Revision: 289996 $
 */
public class TestArrayAccessAtTable
{
    /** The tested table. */
    private Table          _testedTable;
    /** The first tested column. */
    private Column         _column1;
    /** The second tested column. */
    private Column         _column2;
    /** The tested unique index. */
    private UniqueIndex    _uniqueIndex;
    /** The tested non-unique index. */
    private NonUniqueIndex _nonUniqueIndex;

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        _testedTable = new Table();

        _column1 = new Column();
        _column1.setName("column1");
        _column1.setPrimaryKey(true);

        _column2 = new Column();
        _column2.setName("column2");

        _testedTable.addColumn(_column1);
        _testedTable.addColumn(_column2);

        _uniqueIndex = new UniqueIndex();
        _testedTable.addIndex(_uniqueIndex);

        _nonUniqueIndex = new NonUniqueIndex();
        _testedTable.addIndex(_nonUniqueIndex);
    }

    /**
     * Tests that the primary key columns are correctly extracted.
     */
    public void testGetPrimaryKeyColumns()
    {
        List<Column> primaryKeyColumns = _testedTable.getPrimaryKeyColumns()
                .toList();

        assertEquals(1, primaryKeyColumns.size());
        assertSame(_column1, primaryKeyColumns.get(0));
    }

    /**
     * Tests that the columns are correctly extracted.
     */
    public void testGetColumns()
    {
        List<Column> columns = _testedTable.getColumns();

        assertEquals(2,
                     columns.size());
        assertSame(_column1, columns.get(0));
        assertSame(_column2, columns.get(1));
    }

    /**
     * Tests that the non-unique indices are correctly extracted.
     */
    public void testGetNonUniqueIndices()
    {
        List<Index> nonUniqueIndices = _testedTable.getNonUniqueIndices().toList();

        assertEquals(1, nonUniqueIndices.size());
        assertSame(_nonUniqueIndex, nonUniqueIndices.get(0));
    }

    /**
     * Tests that the unique indices are correctly extracted.
     */
    public void testGetUniqueIndices()
    {
        List<Index> uniqueIndices = _testedTable.getUniqueIndices().toList();

        assertEquals(1, uniqueIndices.size());
        assertSame(_uniqueIndex, uniqueIndices.get(0));
    }
}
