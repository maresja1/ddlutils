package org.apache.ddlutils.io;

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

import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.derby.DerbyPlatform;
import org.apache.ddlutils.platform.mssql.MSSqlPlatform;
import org.apache.ddlutils.platform.mysql.MySql50Platform;
import org.apache.ddlutils.platform.mysql.MySqlPlatform;
import org.apache.ddlutils.platform.postgresql.PostgreSqlPlatform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Contains misc tests.
 * 
 * @version $Revision: $
 */
public class TestMisc extends TestAgainstLiveDatabaseBase
{

	@BeforeEach
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	@AfterEach
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

    /**
     * Test for DDLUTILS-178.
     */
    @Test
    public void testDdlUtils178() throws Exception
    {
        if (!getPlatformInfo().isIndicesSupported())
        {
            return;
        }

        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "  <database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "    <table name='ad_sequence_no'>\n"+
            "      <column name='ad_sequence_id' required='true' type='NUMERIC' size='10'/>\n"+
            "      <column name='ad_year' required='true' type='VARCHAR' size='4' default='0000'/>\n"+
            "      <column name='ad_client_id' required='true' type='NUMERIC' size='10'/>\n"+
            "      <unique name='ad_sequence_no_key'>\n"+
            "        <unique-column name='ad_sequence_id'/>\n"+
            "        <unique-column name='ad_year'/>\n"+
            "      </unique>\n"+
            "    </table>\n"+
            "</database>";

        createDatabase(modelXml);

        assertEquals(getAdjustedModel(),
                     readModelFromDatabase("roundtriptest"));
    }

    /**
     * Test for DDLUTILS-179.
     */
    @Test
    public void testDdlUtils179() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='A'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key name='AtoA' foreignTable='A'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n" +
            "  </table>\n"+
            "  <table name='B'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk1' type='INTEGER' required='false'/>\n"+
            "    <column name='fk2' type='INTEGER' required='false'/>\n"+
            "    <foreign-key name='BtoB' foreignTable='B'>\n"+
            "      <reference local='fk1' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "    <foreign-key name='BtoG' foreignTable='G'>\n"+
            "      <reference local='fk2' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='C'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key name='CtoD' foreignTable='D'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='D'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key name='DtoF' foreignTable='F'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='E'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='F'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='fk' type='INTEGER' required='false'/>\n"+
            "    <foreign-key name='FtoC' foreignTable='C'>\n"+
            "      <reference local='fk' foreign='pk'/>\n"+
            "    </foreign-key>\n"+
            "  </table>\n"+
            "  <table name='G'>\n"+
            "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        Database readModel = readModelFromDatabase("roundtriptest");
        
        assertEquals(getAdjustedModel(),
                     readModel);

        File tmpFile = File.createTempFile("model", ".xml"); 

        try
        {
            DatabaseIO       dbIO = new DatabaseIO();
            FileOutputStream out  = new FileOutputStream(tmpFile);
    
            dbIO.write(readModel, out);
            out.flush();
            out.close(); 

            assertEquals(getAdjustedModel(),
                         dbIO.read(tmpFile));
        }
        finally
        {
            if (!tmpFile.delete())
            {
                getLog().warn("Could not delete the temporary file " + tmpFile.getAbsolutePath());
            }
        }
    }

    /**
     * Test for DDLUTILS-214.
     */
    @Test
    public void testDdlUtils214() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='roundtrip1'>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "  <table name='roundtrip2'>\n"+
            "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n"+
            "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        Database readModel = readModelFromDatabase("roundtriptest");
        
        assertEquals(getAdjustedModel(),
                     readModel);

        insertRow("roundtrip1", new Object[] { 1, "foo" });
        insertRow("roundtrip1", new Object[] { 2, "bar" });
        insertRow("roundtrip2", new Object[] { "foo", 1 });
        insertRow("roundtrip2", new Object[] { "bar", 2 });

        var beans1 = getRows("roundtrip1", "pk1");
        var beans2 = getRows("roundtrip2", "pk1");

        assertEquals(2, beans1.size());
        assertEquals(2, beans2.size());
        assertEqualsAttr(1, beans1.get(0), "pk1");
        assertEqualsAttr((Object)"foo",  beans1.get(0), "pk2");
        assertEqualsAttr(2, beans1.get(1), "pk1");
        assertEqualsAttr((Object)"bar",  beans1.get(1), "pk2");
        assertEqualsAttr(1, beans2.get(0), "pk1");
        assertEqualsAttr((Object)"foo",  beans2.get(0), "pk2");
        assertEqualsAttr(2, beans2.get(1), "pk1");
        assertEqualsAttr((Object)"bar",  beans2.get(1), "pk2");

        deleteRow("roundtrip1", new Object[] { 1, "foo" });
        deleteRow("roundtrip2", new Object[] { "foo", 1 });

        beans1 = getRows("roundtrip1", "pk1");
        beans2 = getRows("roundtrip2", "pk1");

        assertEquals(1, beans1.size());
        assertEquals(1, beans2.size());
        assertEqualsAttr(2, beans1.get(0), "pk1");
        assertEqualsAttr((Object)"bar",  beans1.get(0), "pk2");
        assertEqualsAttr(2, beans2.get(0), "pk1");
        assertEqualsAttr((Object)"bar",  beans2.get(0), "pk2");
    }

    /**
     * Test for DDLUTILS-227.
     */
    @Test
    public void testDdlUtils227() throws Exception
    {
        final String modelXml = 
            "<?xml version='1.0' encoding='ISO-8859-1'?>\n"+
            "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n"+
            "  <table name='Roundtrip'>\n"+
            "    <column name='Pk' type='INTEGER' primaryKey='true' required='true'/>\n"+
            "    <column name='Avalue' type='VARCHAR'/>\n"+
            "  </table>\n"+
            "</database>";

        createDatabase(modelXml);

        Database readModel = readModelFromDatabase("roundtriptest");
        
        assertEquals(getAdjustedModel(),
                     readModel);

        insertRow("Roundtrip", new Object[] { 1, "foo" });

        var beans = getRows("Roundtrip");

        assertEquals(1, beans.size());
        assertEqualsAttr(1, beans.get(0), "Pk");
        assertEqualsAttr((Object)"foo",  beans.get(0), "Avalue");

        Table table = getModel().findTable("Roundtrip", getPlatform().isDelimitedIdentifierModeOn())
                .orElseThrow();
        StringBuffer query = new StringBuffer();

        query.append("SELECT * FROM (SELECT * FROM ");
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        query.append(table.getName());
        if (getPlatform().isDelimitedIdentifierModeOn())
        {
            query.append(getPlatformInfo().getDelimiterToken());
        }
        query.append(")");
        // Some JDBC drivers do not allow us to perform the query without an explicit alias 
        if (MySqlPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            MySql50Platform.DATABASENAME.equals(getPlatform().getName()) ||
            PostgreSqlPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            DerbyPlatform.DATABASENAME.equals(getPlatform().getName()) ||
            MSSqlPlatform.DATABASENAME.equals(getPlatform().getName()))
        {
            query.append(" AS ");
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
            query.append(table.getName());
            if (getPlatform().isDelimitedIdentifierModeOn())
            {
                query.append(getPlatformInfo().getDelimiterToken());
            }
        }

        beans = getPlatform().fetch(getModel(), query.toString(), List.of(table));

        assertEquals(1, beans.size());
        assertEqualsAttr(1, beans.get(0), "Pk");
        assertEqualsAttr((Object)"foo",  beans.get(0), "Avalue");
    }
}
