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

        IOUtils.copy(openNMSRestClient.graph(resourceId, report, start, end, width, height), response.getOutputStream());
    }
}
