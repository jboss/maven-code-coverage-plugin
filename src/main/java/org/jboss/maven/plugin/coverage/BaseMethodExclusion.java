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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class BaseMethodExclusion implements MethodExclusion {
    private static final Set<String> EXCLUDES;

    static {
        EXCLUDES = new HashSet<>();
        EXCLUDES.add("equals@(Ljava/lang/Object;)Z");
        EXCLUDES.add("hashCode@()I");
        EXCLUDES.add("toString@()Ljava/lang/String;");
    }

    public static boolean isPublic(MethodInfo methodInfo) {
        return Modifier.isPublic(methodInfo.getAccessFlags());
    }

    public static boolean isBridge(MethodInfo methodInfo) {
        return ((methodInfo.getAccessFlags() & AccessFlag.BRIDGE) == AccessFlag.BRIDGE);
    }

    public boolean exclude(ClassFile clazz, MethodInfo mi) {
        if (isPublic(mi) == false || isBridge(mi)) {
            return true;
        }

        AnnotationsAttribute aa = (AnnotationsAttribute) mi.getAttribute(AnnotationsAttribute.visibleTag);
        if (aa != null) {
            Annotation annotation = aa.getAnnotation(Deprecated.class.getName());
            if (annotation != null) {
                return true;
            }
        }

        String methodName = mi.getName();
        String descriptor = mi.getDescriptor();
        return EXCLUDES.contains(methodName + "@" + descriptor);
    }
}
