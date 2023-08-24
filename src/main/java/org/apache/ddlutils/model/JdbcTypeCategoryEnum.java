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

/**
 * Represents the different categories of jdbc types.
 * 
 * @version $Revision: $
 */
public enum JdbcTypeCategoryEnum
{

	/** The enum value for numeric jdbc types. */
	NUMERIC,
	/** The enum value for date/time jdbc types. */
	DATETIME ,
	/** The enum value for textual jdbc types. */
	TEXTUAL,
	/** The enum value for binary jdbc types. */
	BINARY ,
	/** The enum value for special jdbc types. */
	SPECIAL,
	/** The enum value for other jdbc types. */
	OTHER;

    /** Version id for this class as relevant for serialization. */
    private static final long serialVersionUID = -2695615907467866410L;
}
