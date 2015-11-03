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

import javassist.bytecode.ConstPool;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BytecodeUtils {
    static String getClassName(ConstPool pool, int i) {
        return getRef(pool, i).getClassName(pool, i);
    }

    static String getName(ConstPool pool, int i) {
        return getRef(pool, i).getName(pool, i);
    }

    static String getDesc(ConstPool pool, int i) {
        return getRef(pool, i).getDesc(pool, i);
    }

    static Ref getRef(ConstPool pool, int i) {
        int tag = pool.getTag(i);
        switch (tag) {
            case ConstPool.CONST_Methodref:
                return RefType.METHOD;
            case ConstPool.CONST_InterfaceMethodref:
                return RefType.INTERFACE_METHOD;
            default:
                return RefType.NOOP;
        }
    }

    static interface Ref {
        int getTag();

        String getClassName(ConstPool pool, int i);

        String getName(ConstPool pool, int i);

        String getDesc(ConstPool pool, int i);
    }

    private static enum RefType implements Ref {
        NOOP {
            public int getTag() {
                return -1;
            }

            public String getClassName(ConstPool pool, int i) {
                return null;
            }

            public String getName(ConstPool pool, int i) {
                return null;
            }

            public String getDesc(ConstPool pool, int i) {
                return null;
            }
        },
        METHOD {
            public int getTag() {
                return ConstPool.CONST_Methodref;
            }

            public String getClassName(ConstPool pool, int i) {
                return pool.getMethodrefClassName(i);
            }

            public String getName(ConstPool pool, int i) {
                return pool.getMethodrefName(i);
            }

            public String getDesc(ConstPool pool, int i) {
                return pool.getMethodrefType(i);
            }
        },
        INTERFACE_METHOD {
            public int getTag() {
                return ConstPool.CONST_InterfaceMethodref;
            }

            public String getClassName(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefClassName(i);
            }

            public String getName(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefName(i);
            }

            public String getDesc(ConstPool pool, int i) {
                return pool.getInterfaceMethodrefType(i);
            }
        }
    }
}
