/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.client.configuration.compiler;

import java.io.Serializable;

/**
 * Configuration information for plugging in a custom date-time-method.
 */
public class ConfigurationCompilerPlugInDateTimeMethod implements Serializable {
    private String name;
    private String forgeClassName;

    /**
     * Ctor.
     */
    public ConfigurationCompilerPlugInDateTimeMethod() {
    }

    /**
     * Ctor.
     *
     * @param name           of the date-time method
     * @param forgeClassName the name of the date-time method forge factory class
     */
    public ConfigurationCompilerPlugInDateTimeMethod(String name, String forgeClassName) {
        this.name = name;
        this.forgeClassName = forgeClassName;
    }

    /**
     * Returns the date-time method name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the date-time method name.
     *
     * @param name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the class name of the date-time method forge factory class.
     *
     * @return class name
     */
    public String getForgeClassName() {
        return forgeClassName;
    }

    /**
     * Sets the class name of the aggregation function factory class.
     *
     * @param forgeClassName class name to set
     */
    public void setForgeClassName(String forgeClassName) {
        this.forgeClassName = forgeClassName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigurationCompilerPlugInDateTimeMethod that = (ConfigurationCompilerPlugInDateTimeMethod) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return forgeClassName != null ? forgeClassName.equals(that.forgeClassName) : that.forgeClassName == null;

    }

    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (forgeClassName != null ? forgeClassName.hashCode() : 0);
        return result;
    }
}
