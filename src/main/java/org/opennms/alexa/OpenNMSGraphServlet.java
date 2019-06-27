/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.alexa;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.alexa.rest.OpenNMSRestClient;

import com.amazonaws.util.IOUtils;

public class OpenNMSGraphServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String resourceId = request.getParameter("resourceId");
        final String report = request.getParameter("report");

        String start = request.getParameter("start");
        String end = request.getParameter("end");
        String width = request.getParameter("width");
        String height = request.getParameter("height");

        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        response.setContentType("image/png");

        IOUtils.copy(openNMSRestClient.graph(resourceId,report,start,end,width,height), response.getOutputStream());
    }
}
