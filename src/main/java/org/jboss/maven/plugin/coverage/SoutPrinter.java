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

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class SoutPrinter implements Printer {
    static final Printer INSTANCE = new SoutPrinter();

    public void print(Map<String, Map<Tuple, Set<CodeLine>>> report) {
        StringBuilder builder = new StringBuilder("\n");
        for (String iface : report.keySet()) {
            builder.append("\nInterface / Class: ").append(iface).append("\n");
            Map<Tuple, Set<CodeLine>> map = report.get(iface);
            for (Map.Entry<Tuple, Set<CodeLine>> entry : map.entrySet()) {
                builder.append("\t").append(entry.getKey()).append("\n");
                Set<CodeLine> value = entry.getValue();
                if (value.isEmpty()) {
                    builder.append("\t\t").append("MISSING -- TODO?").append("\n");
                } else {
                    for (CodeLine cl : value) {
                        builder.append("\t\t").append(cl).append("\n");
                    }
                }
            }
        }
        System.out.println(builder);
    }
}
