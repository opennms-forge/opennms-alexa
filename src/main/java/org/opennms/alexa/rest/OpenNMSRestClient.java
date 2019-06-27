package org.opennms.alexa.rest;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.model.GraphNameCollection;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetail;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.resource.ResourceDTO;

import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.ListItem;
import com.amazon.ask.model.interfaces.display.PlainText;
import com.amazon.ask.model.interfaces.display.TextContent;

public class OpenNMSRestClient {
    private final String baseUrl;
    final String username;
    final String password;
    final Client httpClient;
    final String graphUrl;

    final Map<Integer, String> nodeLabelCache = new TreeMap<>();

    public OpenNMSRestClient() {
        this(
                System.getProperty("org.opennms.baseUrl"),
                System.getProperty("org.opennms.username"),
                System.getProperty("org.opennms.password"),
                System.getProperty("org.opennms.servletUrl")
        );
    }

    private OpenNMSRestClient(final String baseUrl, final String username, final String password, final String graphUrl) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.httpClient = ClientBuilder.newClient().register(HttpAuthenticationFeature.basic(this.username, this.password));
        this.graphUrl = graphUrl;
    }

    private Client getHttpClient() {
        return this.httpClient;
    }

    public <T> T getEntity(final Class<T> clazz, final String path, final int id) {
        return getHttpClient()
                .target(this.baseUrl + path + "/" + id)
                .request(MediaType.APPLICATION_XML)
                .get(clazz);
    }

    public <T> List<T> getEntities(final Class<T> clazz, final String path, int offset, int limit) {
        final ParameterizedType parameterizedGenericType = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            public Type getRawType() {
                return List.class;
            }

            public Type getOwnerType() {
                return List.class;
            }
        };

        final GenericType<List<T>> genericType = new GenericType<List<T>>(
                parameterizedGenericType) {
        };

        return getHttpClient()
                .target(this.baseUrl + path + (path.contains("?") ? "&" : "?") + "offset=" + offset + "&limit=" + limit)
                .request(MediaType.APPLICATION_XML)
                .get(genericType);
    }

    public String getOutagesForNode(final int nodeId) {
        final List<OnmsOutage> outages = getEntities(OnmsOutage.class, "/rest/outages/forNode/" + nodeId + "?serviceRegainedEvent=NULL", 0,0);

        if (outages.size() > 0) {
            String richText = String.format("<b>%d ongoing outages:</b><br/>", outages.size());

            for (final OnmsOutage outage : outages) {
                richText += "Outage #"+outage.getId()+" ("+outage.getServiceLostEvent().getEventTime()+"):<br/><font size='2'><i>"+InetAddressUtils.toIpAddrString(outage.getServiceLostEvent().getIpAddr())+" / "+outage.getServiceLostEvent().getServiceType().getName()+"</i></font><br/>";

            }
            return richText;
        } else {
            return "<b>No outages</b><br/>";
        }
    }

    public String getAlarmsForNode(final int nodeId) {
        final List<OnmsAlarm> alarms = getEntities(OnmsAlarm.class, "/rest/alarms/?nodeId=" + nodeId + "&alarmAckTime=null", 0,0);

        if (alarms.size() > 0) {
            String richText = String.format("<b>%d unacknowledged alarms:</b><br/>", alarms.size());


            for (final OnmsAlarm alarm : alarms) {
                richText += "<img src='"+ OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/alarm-" + alarm.getSeverityLabel().toLowerCase() + ".png' height='37' width='14' /> Alarm #" + alarm.getId() + " (" + alarm.getLastEventTime() + "):<br/><font size='2'><i>"+alarm.getLogMsg().replaceAll("\\<.*?>", "")+"</i></font>";
            }
            return richText;
        } else {
            return "<b>No unacknowledged alarms</b><br/>";
        }
    }

    public List<ListItem> getGraphsForNode(final int nodeId) {
        final List<ListItem> listItems = new ArrayList<>();

        final ResourceDTO resourceDTO = getHttpClient()
                .target(this.baseUrl + "/rest/resources/fornode/" + nodeId)
                .request(MediaType.APPLICATION_XML)
                .get(ResourceDTO.class);

        for (final ResourceDTO childRessource : resourceDTO.getChildren().getObjects()) {

            final GraphNameCollection graphNameCollection = getHttpClient()
                    .target(this.baseUrl + "/rest/graphs/for/" + encode(childRessource.getId()))
                    .request(MediaType.APPLICATION_XML)
                    .get(GraphNameCollection.class);

            for (final String string : graphNameCollection.getObjects()) {
                final ListItem listItem = ListItem.builder()
                        .withTextContent(TextContent.builder()
                                .withPrimaryText(PlainText.builder().withText(string).build())
                                .withSecondaryText(PlainText.builder().withText(childRessource.getLabel()).build())
                                .build())
                        .withToken(string)
                        .withImage(Image.builder()
                                .addSourcesItem(ImageInstance.builder()
                                        .withUrl(this.graphUrl + "?resourceId=" + encode(childRessource.getId()) + "&report=" + string + "&start=1553543994750&end=1553630394750&width=498&height=280")
                                        .withWidthPixels(498)
                                        .withHeightPixels(280)
                                        .build())
                                .build())
                        .build();

                listItems.add(listItem);
            }
        }

        return listItems;
    }

    public synchronized String getNodeLabelForIfService(final int id) {
        final OnmsMonitoredServiceDetail onmsMonitoredServiceDetail = getHttpClient()
                .target(this.baseUrl + "/rest/ifservices/" + id)
                .request(MediaType.APPLICATION_XML)
                .get(OnmsMonitoredServiceDetail.class);

        return onmsMonitoredServiceDetail.getNodeLabel();
    }

    public synchronized OnmsNode getNodeForId(final int id) {
        return getEntity(OnmsNode.class, "/rest/nodes",id);
    }

    public InputStream graph(final String resourceId, final String report, final String start, final String end, final String width, final String height) {
        final String url = String.format(this.baseUrl + "/graph/graph.png?resourceId=%s&report=%s&start=%s&end=%s&width=%s&height=%s", resourceId, report, start, end, width, height);

        return getHttpClient()
                .target(url)
                .request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);
    }

    private String encode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
