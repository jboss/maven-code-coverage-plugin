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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @goal coverage
 * @phase process-test-classes
 * @requiresDependencyResolution
 */
public class CoverageMojo extends AbstractMojo implements Configuration {
    /**
     * The Maven Project Object
     *
     * @parameter property="project"
     * @required
     */
    protected MavenProject project;

    /**
     * Module.
     *
     * @parameter
     */
    protected String module;

    /**
     * Tests.
     *
     * @parameter
     */
    protected boolean tests = true;

    /**
     * Exclusion.
     *
     * @parameter
     */
    protected String exclusion;

    /**
     * Interfaces.
     *
     * @parameter
     */
    protected List<String> interfaces;

    /**
     * Coverage file.
     *
     * @parameter
     */
    protected String coverageFile = "coverage.txt";

    /**
     * Repository host.
     *
     * @parameter
     */
    protected String repositoryHost = "github.com";

    /**
     * Repository user.
     *
     * @parameter
     */
    protected String repositoryUser;

    /**
     * Repository project.
     *
     * @parameter
     */
    protected String repositoryProject;

    /**
     * Repository branch.
     *
     * @parameter
     */
    protected String repositoryBranch = "master";

    /**
     * Coverage title.
     *
     * @parameter
     */
    protected String coverageTitle;

    /**
     * Javadoc root.
     *
     * @parameter
     */
    protected String javadocRoot;

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<URL> classPathUrls = getClassPathUrls(tests);
        ClassLoader cl = new URLClassLoader(classPathUrls.toArray(new URL[classPathUrls.size()]), getClass().getClassLoader());
        Build build = project.getBuild();
        File classesToScan = tests ? new File(build.getTestOutputDirectory()) : new File(build.getOutputDirectory());
        getLog().info("Classes to scan: " + classesToScan);
        List<String> classes = new ArrayList<>();
        if (interfaces != null) {
            classes.addAll(interfaces);
        }
        try {
            readInterfaces(classesToScan, classes);
            MethodExclusion me = createExclusion(cl, classesToScan);
            CodeCoverage.report(this, module, cl, project.getBasedir(), classesToScan, me, classes.toArray(new String[classes.size()]));
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute coverage report.", e);
        }
    }

    /**
     * Builds a classpath based on the maven project's compile classpath elements.
     *
     * @param isTest do we modify tests
     * @return The {@link ClassLoader} made up of the maven project's compile classpath elements.
     * @throws MojoExecutionException Indicates an issue processing one of the classpath elements
     */
    private List<URL> getClassPathUrls(boolean isTest) throws MojoExecutionException {
        List<URL> classPathUrls = new ArrayList<>();
        for (String path : projectCompileClasspathElements(isTest)) {
            try {
                getLog().debug("Adding project compile classpath element : " + path);
                classPathUrls.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Unable to build path URL [" + path + "]");
            }
        }
        return classPathUrls;
    }

    /**
     * Essentially a call to {@link MavenProject#getCompileClasspathElements} except that here we
     * cast it to the generic type and internally handle {@link org.apache.maven.artifact.DependencyResolutionRequiredException}.
     *
     * @param isTest do we modify tests
     * @return The compile classpath elements
     * @throws MojoExecutionException Indicates a {@link org.apache.maven.artifact.DependencyResolutionRequiredException} was encountered
     */
    private List<String> projectCompileClasspathElements(boolean isTest) throws MojoExecutionException {
        try {
            if (isTest) {
                return project.getTestClasspathElements();
            } else {
                return project.getCompileClasspathElements();
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Call to MavenProject#getCompileClasspathElements required dependency resolution");
        }
    }

    @SuppressWarnings("unchecked")
    private MethodExclusion createExclusion(ClassLoader cl, File root) throws Exception {
        if (exclusion != null) {
            Class<MethodExclusion> clazz = (Class<MethodExclusion>) cl.loadClass(exclusion);
            return clazz.getConstructor(File.class).newInstance(root);
        } else {
            return FileMethodExclusion.create(root);
        }
    }

    private void readInterfaces(File root, List<String> classes) throws IOException {
        String cf = System.getProperty("coverage.file", coverageFile);
        File coverage = new File(root, cf);
        if (coverage.exists()) {
            getLog().info("Reading interfaces from " + coverage);
            try (BufferedReader reader = new BufferedReader(new FileReader(coverage))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.startsWith("#") == false) {
                        classes.add(line);
                    }
                }
            }
        }
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public boolean isTests() {
        return tests;
    }

    public void setTests(boolean tests) {
        this.tests = tests;
    }

    public String getExclusion() {
        return exclusion;
    }

    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getCoverageFile() {
        return coverageFile;
    }

    public void setCoverageFile(String coverageFile) {
        this.coverageFile = coverageFile;
    }

    public String getRepositoryHost() {
        return repositoryHost;
    }

    public void setRepositoryHost(String repositoryHost) {
        this.repositoryHost = repositoryHost;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    public String getRepositoryProject() {
        return repositoryProject;
    }

    public void setRepositoryProject(String repositoryProject) {
        this.repositoryProject = repositoryProject;
    }

    public String getRepositoryBranch() {
        return repositoryBranch;
    }

    public void setRepositoryBranch(String repositoryBranch) {
        this.repositoryBranch = repositoryBranch;
    }

    public String getCoverageTitle() {
        return coverageTitle;
    }

    public void setCoverageTitle(String coverageTitle) {
        this.coverageTitle = coverageTitle;
    }

    public String getJavadocRoot() {
        return javadocRoot;
    }

    public void setJavadocRoot(String javadocRoot) {
        this.javadocRoot = javadocRoot;
    }
}
