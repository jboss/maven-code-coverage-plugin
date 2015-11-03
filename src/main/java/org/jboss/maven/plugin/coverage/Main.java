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

import java.io.File;
import java.util.Arrays;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 2)
            throw new IllegalArgumentException("Invalid args: " + Arrays.toString(args));

        File classesToScan = new File(args[0]);
        if (classesToScan.exists() == false)
            throw new IllegalArgumentException("No such dir: " + classesToScan);
        if (classesToScan.isDirectory() == false)
            throw new IllegalArgumentException("Is not directory: " + classesToScan);

        CodeCoverage.report(new DummyConfiguration(), null, null, new File("").getAbsoluteFile(), classesToScan, FileMethodExclusion.create(classesToScan), args[1]);
    }

    private static class DummyConfiguration implements Configuration {
        public String getRepositoryHost() {
            return "github.com";
        }

        public String getRepositoryUser() {
            return "jboss";
        }

        public String getRepositoryProject() {
            return "maven-code-coverage-plugin";
        }

        public String getRepositoryBranch() {
            return "master";
        }

        public String getCoverageTitle() {
            return "Temp API";
        }

        public String getJavadocRoot() {
            return "http://www.jboss.org/temp/javadoc/";
        }
    }
}
