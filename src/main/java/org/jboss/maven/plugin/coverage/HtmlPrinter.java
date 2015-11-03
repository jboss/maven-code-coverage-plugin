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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
class HtmlPrinter implements Printer {
    private Configuration configuration;
    private File baseDir;
    private File index;
    private String module;

    HtmlPrinter(Configuration configuration, File baseDir, File index, String module) {
        this.configuration = configuration;
        this.baseDir = baseDir;
        this.index = index;
        this.module = module;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void print(Map<String, Map<Tuple, Set<CodeLine>>> report) throws Exception {
        if (index.exists()) index.delete();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(index))) {
            writer.write("<html>\n" +
                "<head>" +
                "<title>" + configuration.getCoverageTitle() + " Code Coverage</title>" +
                "    <style type=\"text/css\">\n" +
                "body {\n" +
                "    font-family: Arial, sans-serif;\n" +
                "}\n" +
                "\n" +
                "div.apiClass {\n" +
                "    background-color: #dee3e9;\n" +
                "    border: 1px solid #9eadc0;\n" +
                "    margin: 5px 5px 50px 5px;\n" +
                "    padding: 2px 5px;\n" +
                "}\n" +
                "\n" +
                "ul.blockList {\n" +
                "    margin:10px 0 10px 0;\n" +
                "    padding:0;\n" +
                "}\n" +
                "ul.blockList li.blockList {\n" +
                "    list-style:none;\n" +
                "    margin-bottom:5px;\n" +
                "    padding:5px 20px 5px 10px;\n" +
                "    border:1px solid #9eadc0;\n" +
                "    background-color:#f9f9f9;\n" +
                "}\n" +
                "ul.blockList li.blockList h3 {\n" +
                "    margin: 3px;\n" +
                "    color: black;\n" +
                "    font-weight: bold;\n" +
                "}\n" +
                "ul.blockList ul.blockList {\n" +
                "    padding:5px 5px 5px 8px;\n" +
                "    background-color:#ffffff;\n" +
                "    border:1px solid #9eadc0;\n" +
                "}\n" +
                "ul.blockList ul.blockList li.blockList{\n" +
                "    border: 0;\n" +
                "    padding:0 0 5px 8px;\n" +
                "    background-color:#ffffff;\n" +
                "    padding: 3px;\n" +
                "    margin: 0px;\n" +
                "}\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>");
            writer.write("<h3>" + baseDir.getName() + "</h3>");
            for (String iface : report.keySet()) {
                writer.append("<div class=\"apiClass\">");
                writer.append("<h4>").append(iface).append("</h4>");
                Map<Tuple, Set<CodeLine>> map = report.get(iface);
                writer.write("<ul class=\"blockList\">");
                for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                    String methodSignature = SignatureConverter.convertMethodSignature(entry.getKey().getMethodName(), entry.getKey().getMethodDesc());
                    String fullMethodSignature = SignatureConverter.convertFullMethodSignature(entry.getKey().getMethodName(), entry.getKey().getMethodDesc());
                    String methodJavaDocUrl = configuration.getJavadocRoot() + iface.replace('.', '/').replace('$', '.') + "#" + methodSignature;
                    writer.append("<li class=\"blockList\">").append("<a href=\"").append(methodJavaDocUrl).append("\" target=\"_top\">").append("<h5>").append(esc(fullMethodSignature)).append("</h5></a>");
                    writer.append("<ul class=\"blockList\">");
                    Set<CodeLine> value = entry.getValue();
                    if (value.isEmpty()) {
                        writer.append("<li class=\"blockList\">").append("MISSING -- TODO?").append("</li>");
                    } else {
                        for (CodeLine cl : value) {
                            writer.append("<li class=\"blockList\">").append(toLink(cl)).append("</li>");
                        }
                    }
                    writer.append("</ul>");
                    writer.append("</li>");
                    writer.newLine();
                }
                writer.write("</ul>");
                writer.write("</div>");
            }
            writer.write("</body></html>");
        }
    }

    protected String toLink(CodeLine cl) {
        String url = createGitHubUrl(
            configuration.getRepositoryHost(),
            configuration.getRepositoryUser(),
            configuration.getRepositoryProject(),
            configuration.getRepositoryBranch(),
            getPath(cl),
            cl.getLine()
        );

        StringBuilder sb = new StringBuilder(cl.getSimpleClassName());
        sb.append(cl.getExt());
        if (cl.getLine() > 0) {
            sb.append(":");
            sb.append(cl.getLine());
        }
        String text = esc(sb.toString());

        return esc(cl.getClassName() + "." + cl.getMethodName()) + " (<a href=\"" + url + "\" target=\"_top\">" + esc(text) + "</a>)";
    }

    private String getPath(CodeLine cl) {
        String className = cl.getClassName();
        int p = className.indexOf("$"); // ignore inner classes
        if (p > 0) {
            className = className.substring(0, p);
        }
        return "/" + module + "/" + baseDir.getName() + "/src/test/" + cl.getType() + "/" + className.replace('.', '/') + cl.getExt();
    }

    private static String createGitHubUrl(String host, String user, String project, String branch, String path, int lineNumber) {
        StringBuilder link = new StringBuilder("http://" + host + "/" + user + "/" + project + "/blob/" + branch + path);
        if (lineNumber > 0) {
            link.append("#L").append(lineNumber);
        }
        return link.toString();
    }

    static String esc(String token) {
        token = token.replace("<", "&lt;");
        token = token.replace(">", "&gt;");
        return token;
    }
}
