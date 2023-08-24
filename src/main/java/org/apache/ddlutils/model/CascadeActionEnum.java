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

import org.apache.commons.lang3.EnumUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Represents the different cascade actions for the <code>onDelete</code> and
 * <code>onUpdate</code> properties of {@link ForeignKey}.
 * 
 * @version $Revision: $
 */
public enum CascadeActionEnum
{

	/** The enum value for a cascade action which directs the database to apply the change to
	 the referenced table also to this table. E.g. if the referenced row is deleted, then
	 the local one will also be deleted when this value is used for the onDelete action. */
	CASCADE,
	/** The enum value for a cascade action which directs the database to set the local columns
	 referenced by the foreign key to null when the referenced row changes/is deleted. */
	SET_NULL,
	/** The enum value for a cascade action which directs the database to set the local columns
	 referenced by the foreign key to the default value when the referenced row changes/is deleted. */
	SET_DEFAULT,
	/** The enum value for a cascade action which directs the database to restrict the change
	 changes to the referenced column. The interpretation of this is database-dependent, but it is
	 usually the same as {@link #NONE}. */
	RESTRICT,
	/** The enum value for the cascade action that directs the database to not change the local column
	 when the value of the referenced column changes, only check the foreign key constraint. */
	NONE;

	/** The integer value for the enum value for a cascade action. */
    public static final int VALUE_CASCADE     = 1;
    /** The integer value for the enum value for a set-null action. */
    public static final int VALUE_SET_NULL    = 2;
    /** The integer value for the enum value for a set-null action. */
    public static final int VALUE_SET_DEFAULT = 3;
    /** The integer value for the enum value for a restrict action. */
    public static final int VALUE_RESTRICT    = 4;
    /** The integer value for the enum value for no-action. */
    public static final int VALUE_NONE        = 5;
    /** Version id for this class as relevant for serialization. */
    private static final long serialVersionUID = -6378050861446415790L;


    /**
     * Returns the enum value that corresponds to the given textual
     * representation.
     * 
     * @param defaultTextRep The textual representation
     * @return The enum value
     */
    public static CascadeActionEnum getEnum(String defaultTextRep)
    {
        return EnumUtils.getEnum(CascadeActionEnum.class, defaultTextRep);
    }

    /**
     * Returns a list of all enum values.
     * 
     * @return The list of enum values
     */
    public static List<CascadeActionEnum> getEnumList()
    {
        return EnumUtils.getEnumList(CascadeActionEnum.class);
    }

    /**
     * Returns an iterator of all enum values.
     * 
     * @return The iterator
     */
    public static Iterator<CascadeActionEnum> iterator()
    {
        return getEnumList().iterator();
    }
}
