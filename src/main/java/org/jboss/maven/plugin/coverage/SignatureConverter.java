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

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SignatureConverter {

    public static String convertMethodSignature(String methodName, String descriptor) {
        return convertToMethodSignature(methodName, descriptor, false);
    }

    public static String convertFullMethodSignature(String methodName, String descriptor) {
        return convertToMethodSignature(methodName, descriptor, true);
    }

    protected static String convertToMethodSignature(String methodName, String descriptor, boolean full) {
        if (descriptor.charAt(0) != '(') {
            throw new IllegalArgumentException("Can't convert " + descriptor);
        }

        int p = descriptor.lastIndexOf(')');
        String params = descriptor.substring(1, p);

        StringTokenizer tokenizer = new StringTokenizer(params, ";");
        StringBuilder sb = new StringBuilder();

        if (full) {
            String retParam = descriptor.substring(p + 1);
            if (retParam.endsWith(";")) {
                retParam = retParam.substring(0, retParam.length() - 1);
            }
            String ret = convertParam(retParam);
            // only use simple name for return type
            int r = ret.lastIndexOf(".");
            if (r > 0) {
                ret = ret.substring(r + 1);
            }
            sb.append(ret).append("  ");
        }

        sb.append(methodName).append("(");
        while (tokenizer.hasMoreTokens()) {
            String param = tokenizer.nextToken();
            sb.append(convertParam(param));
            if (tokenizer.hasMoreTokens()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static String convertParam(String param) {
        int i = 0;
        StringBuilder appendix = new StringBuilder();
        for (; param.charAt(i) == '['; i++) {
            appendix.append("[]");
        }

        String subparam = param.substring(i);
        String result = convertNonArrayParam(subparam) + appendix;
        if (isPrimitive(subparam) && subparam.length() > 1) {
            return result + ", " + convertParam(subparam.substring(1));
        } else {
            return result;
        }
    }

    private static String convertNonArrayParam(String param) {
        switch (param.charAt(0)) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'Z':
                return "boolean";
            case 'V':
                return "void";
//            case 'L':
//                int lastDotIndex = param.lastIndexOf('/');
//                return lastDotIndex == -1 ? param.substring(1) : param.substring(lastDotIndex+1);
            case 'L':
                return param.substring(1).replace('/', '.');
            default:
                throw new IllegalArgumentException("Unknown param type " + param);
        }
    }

    private static boolean isPrimitive(String param) {
        switch (param.charAt(0)) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
                return true;
            default:
                return false;
        }
    }
}
