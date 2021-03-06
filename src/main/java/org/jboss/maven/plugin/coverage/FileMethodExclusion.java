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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class FileMethodExclusion extends BaseMethodExclusion {
    private Set<String> exclusions = new HashSet<String>();

    private FileMethodExclusion() {
    }

    public static MethodExclusion create(File root) {
        File ef = new File(root, "exclusions.txt");
        if (ef.exists()) {
            FileMethodExclusion fme = new FileMethodExclusion();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(ef));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    fme.exclusions.add(line);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            return fme;
        } else {
            return new BaseMethodExclusion();
        }
    }

    @Override
    public boolean exclude(ClassFile clazz, MethodInfo mi) {
        return (super.exclude(clazz, mi) || doExclude(clazz, mi));
    }

    protected boolean doExclude(ClassFile clazz, MethodInfo mi) {
        String exc = clazz.getName() + "@" + mi.getName() + "@" + mi.getDescriptor();
        return exclusions.contains(exc);
    }
}
