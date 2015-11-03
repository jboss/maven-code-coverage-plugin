/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.maven.plugin.coverage;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@SuppressWarnings("unchecked")
public class CodeLine implements Comparable<CodeLine> {
    static String JSP = ".jsp";
    static String JSP_PREFIX = "org.apache" + JSP + ".";
    static String JSP_SUFFIX = "_jsp";

    private String type = "java";
    private String className;
    private String ext = ".java";
    private String methodName;
    private int line;

    public CodeLine(String className, String methodName, int line) {
        this.className = className;
        this.methodName = methodName;
        this.line = line;
    }

    public CodeLine modify() {
        if (className.startsWith(JSP_PREFIX) && className.endsWith(JSP_SUFFIX)) {
            className = className.substring(JSP_PREFIX.length(), className.length() - JSP_SUFFIX.length());
            type = "resources";
            ext = JSP;
            line = 0;
        }
        return this;
    }

    public int compareTo(CodeLine cl) {
        int diff = className.compareTo(cl.className);
        if (diff != 0)
            return diff;

        diff = methodName.compareTo(cl.methodName);
        if (diff != 0)
            return diff;

        diff = line - cl.line;
        if (diff != 0)
            return diff;

        return 0;
    }

    public String getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    public String getExt() {
        return ext;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return className + " @ " + methodName + " # " + line;
    }

    public String getSimpleClassName() {
        int lastDotIndex = className.lastIndexOf('.');
        return lastDotIndex == -1 ? className : className.substring(lastDotIndex + 1);
    }
}
