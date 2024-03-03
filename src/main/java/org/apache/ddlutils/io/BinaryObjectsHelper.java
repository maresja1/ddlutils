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

import org.apache.ddlutils.DdlUtilsException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Helper class for dealing with the serialization and Base64 encoding of objects.
 * 
 * @version $Revision: $
 */
public class BinaryObjectsHelper
{
    /**
     * Serializes the given object to a byte array representation.
     * 
     * @param obj The object to serialize
     * @return The byte array containing the serialized form of the object
     */
    public byte[] serialize(Object obj)
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream    objOut = new ObjectOutputStream(output);

            objOut.writeObject(obj);
            objOut.close();

            return output.toByteArray();
        }
        catch (IOException ex)
        {
            throw new DdlUtilsException("Could not serialize object", ex);
        }
    }

    /**
     * Deserializes the object from its byte array representation.
     * 
     * @param serializedForm The byte array containing the serialized form of the object
     * @return The object
     */
    public Object deserialize(byte[] serializedForm)
    {
        try
        {
            ByteArrayInputStream input = new ByteArrayInputStream(serializedForm);
            ObjectInputStream    objIn = new ObjectInputStream(input);

            return objIn.readObject();
        }
        catch (IOException ex)
        {
            throw new DdlUtilsException("Could not deserialize object", ex);
        }
        catch (ClassNotFoundException ex)
        {
            throw new DdlUtilsException("Could find class for deserialized object", ex);
        }
    }

}
