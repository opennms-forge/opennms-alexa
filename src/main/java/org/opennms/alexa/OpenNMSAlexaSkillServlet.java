package org.opennms.alexa;

import static org.opennms.alexa.locale.LocaleUtils.severity;
import static org.opennms.alexa.locale.LocaleUtils.time;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.opennms.alexa.handlers.amazon.CancelAndStopIntentHandler;
import org.opennms.alexa.handlers.amazon.FallbackIntentHandler;
import org.opennms.alexa.handlers.amazon.HelpIntentHandler;
import org.opennms.alexa.handlers.amazon.NullRequestHandler;
import org.opennms.alexa.handlers.custom.CustomExceptionHandler;
import org.opennms.alexa.handlers.custom.LaunchRequestHandler;
import org.opennms.alexa.handlers.custom.SessionEndedRequestHandler;
import org.opennms.alexa.handlers.lists.ListIntent;
import org.opennms.alexa.handlers.overview.AlarmsOverviewIntent;
import org.opennms.alexa.handlers.overview.OutagesOverviewIntent;
import org.opennms.alexa.rest.OpenNMSRestClient;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.model.interfaces.display.BodyTemplate2;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.ListItem;
import com.amazon.ask.model.interfaces.display.PlainText;
import com.amazon.ask.model.interfaces.display.RichText;
import com.amazon.ask.model.interfaces.display.TextContent;
import com.amazon.ask.servlet.SkillServlet;

public class OpenNMSAlexaSkillServlet extends SkillServlet {

    private static final String SKILL_NAME = "OpenNMS Alexa Skill";
    private static final String SKILL_LOGO = "images/opennms-logo.png";
    private static final String DEV_NAME = "Christian Pape";
    private static final String DEV_EMAIL = "christian@opennms.org";
    public static final String SERVLET_URL = System.getProperty("org.opennms.servletUrl");

    public OpenNMSAlexaSkillServlet() {
        super(getSkill());
    }

    private static Skill getSkill() {
        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        //noinspection unchecked
        return Skills.standard()
                .addRequestHandlers(
                        new CancelAndStopIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new FallbackIntentHandler(),

                        ListIntent.<OnmsNode>builder()
                                .withClass(OnmsNode.class)
                                .withIntentName("NodesIntent")
                                .withTitle("Nodes")
                                .withPath("/rest/nodes")
                                .withListFunction(
                                        n -> ListItem.builder()
                                                .withToken(n.getNodeId())
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(PlainText.builder()
                                                                .withText(n.getLabel()).build())
                                                        .withSecondaryText(RichText.builder()
                                                                .withText(size(2, n.getForeignSource() + ":" + n.getForeignId() + " (" + n.getNodeId() + ")")).build())
                                                        .build())
                                                .build())
                                .withSelectionFunction("en-US",
                                        n -> BodyTemplate2.builder()
                                                .withTitle("Node #" + n.getId())
                                                .withBackgroundImage(Image.builder()
                                                        .addSourcesItem(ImageInstance.builder()
                                                                .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/nodes-background.png")
                                                                .withWidthPixels(1024)
                                                                .withHeightPixels(600)
                                                                .build())
                                                        .build())
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(RichText.builder().withText(n.getLabel() + "<br/>" + n.getForeignSource() + ":" + n.getForeignId() + " (" + n.getNodeId() + ")").build())
                                                        .withSecondaryText(RichText.builder().withText(openNMSRestClient.getOutagesForNode(n.getId()) + openNMSRestClient.getAlarmsForNode(n.getId())).build())
                                                        .build())
                                                .build())
                                .withSelectionSpeechFunction("en-US", n -> "node #" + n.getId() + ", " + n.getLabel())
                                .withSelectionSpeechFunction("de-DE", n -> "Knoten #" + n.getId() + ", " + n.getLabel())
                                .build(),
                        ListIntent.<OnmsOutage>builder()
                                .withClass(OnmsOutage.class)
                                .withIntentName("OutagesIntent")
                                .withTitle("Outages")
                                .withPath("/rest/outages")
                                .withParameters("ifRegainedService=null")
                                .withListFunction(
                                        o -> ListItem.builder()
                                                .withToken(String.valueOf(o.getId()))
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(PlainText.builder().withText("Outage #" + o.getId() + " (" + time("en-US", o.getIfLostService()) + " ago)").build())
                                                        .withSecondaryText(RichText.builder().withText(size(2, openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId()) + " / " + inetAddress(o) + " / " + o.getServiceType().getName())).build())
                                                        .build())
                                                .build())
                                .withSelectionFunction("en-US",
                                        o -> BodyTemplate2.builder()
                                                .withTitle("Outage #" + o.getId())
                                                .withBackgroundImage(Image.builder()
                                                        .addSourcesItem(ImageInstance.builder()
                                                                .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/outages-background.png")
                                                                .withWidthPixels(1024)
                                                                .withHeightPixels(600)
                                                                .build())
                                                        .build())
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(PlainText.builder().withText(o.getIfLostService() + " (" + time("en-US", o.getIfLostService()) + " ago)").build())
                                                        .withSecondaryText(RichText.builder().withText(openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId()) + " / " + inetAddress(o) + " / " + o.getServiceType().getName()).build())
                                                        .build())
                                                .build())
                                .withSelectionSpeechFunction("en-US", o -> "outage #" + o.getId() + ", " + time("en-US", o.getIfLostService()) + " ago, interface " + inetAddress(o) + ", node " + openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId()) + ", service " + o.getServiceType().getName())
                                .withSelectionSpeechFunction("de-DE", o -> "Ausfall #" + o.getId() + ", vor " + time("de-DE", o.getIfLostService()) + ", Schnittstelle " + inetAddress(o) + ", Knoten " + openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId()) + ", Dienst " + o.getServiceType().getName())
                                .build(),
                        ListIntent.<OnmsAlarm>builder()
                                .withClass(OnmsAlarm.class)
                                .withIntentName("AlarmsIntent")
                                .withTitle("Alarms")
                                .withPath("/rest/alarms")
                                .withParameters("comparator=gt&severity=NORMAL&alarmAckTime=null")
                                .withListFunction(
                                        a -> ListItem.builder()
                                                .withToken(String.valueOf(a.getId()))
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(PlainText.builder().withText("Alarm #" + a.getId() + " (" + time("en-US", a.getLastEventTime()) + " ago)").build())
                                                        .withSecondaryText(RichText.builder()
                                                                .withText(size(2, a.getLogMsg().replaceAll("\\<.*?>", "")))
                                                                .build())
                                                        .build())
                                                .withImage(Image.builder().addSourcesItem(ImageInstance.builder().withUrl(SERVLET_URL + "/images/alarm-" + a.getSeverityLabel().toLowerCase() + ".png").build()).build())
                                                .build())
                                .withSelectionFunction("en-US",
                                        a -> BodyTemplate2.builder()
                                                .withTitle("Alarm #" + a.getId())
                                                .withBackgroundImage(Image.builder()
                                                        .addSourcesItem(ImageInstance.builder()
                                                                .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/alarms-background.png")
                                                                .withWidthPixels(1024)
                                                                .withHeightPixels(600)
                                                                .build())
                                                        .build())
                                                .withTextContent(TextContent.builder()
                                                        .withPrimaryText(RichText.builder().withText("<img src='" + SERVLET_URL + "/images/alarm-" + a.getSeverityLabel().toLowerCase() + ".png' height='37' width='14' /> " + a.getSeverityLabel()).build())
                                                        .withSecondaryText(RichText.builder().withText(a.getLastEventTime() + " (" + time("en-US", a.getLastEventTime()) + " ago)<br/>" + a.getLogMsg().replaceAll("\\<.*?>", "")).build())
                                                        .build())
                                                .build())
                                .withSelectionSpeechFunction("en-US", a -> a.getSeverityLabel().toLowerCase() + " alarm #" + a.getId() + ", " + time("en-US", a.getLastEventTime()) + " ago")
                                .withSelectionSpeechFunction("de-DE", a -> " Alarm #" + a.getId() + " mit dem Schweregrad " + severity("de-DE", a.getSeverityLabel().toLowerCase()) + ", vor " + time("de-DE", a.getLastEventTime()))
                                .build(),

                        new OutagesOverviewIntent(),
                        new AlarmsOverviewIntent(),
                        new SessionEndedRequestHandler(),
                        new NullRequestHandler())

                .addExceptionHandler(new CustomExceptionHandler())
                .build();
    }

    private static String inetAddress(final OnmsOutage o) {
        if (o == null) {
            return "-";
        }

        if (o.getServiceLostEvent()==null) {
            return "-";
        }

        if (o.getServiceLostEvent().getIpAddr() == null) {
            return "-";
        }

        return InetAddressUtils.toIpAddrString(o.getServiceLostEvent().getIpAddr());
    }
    private static String size(final int size, final String text) {
        return "<font size=\"" + size + "\">" + StringEscapeUtils.escapeXml(text) + "</font>";
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<html>" +
                "<head><title>" + SKILL_NAME + "</title></head>" +
                "<body><img src='" + SKILL_LOGO + "'/><br/><br/>" +
                "<b>" + SKILL_NAME + "</b> - " + DEV_NAME + " &lt;" +
                "<a href='mailto:" + DEV_EMAIL + "'>" + DEV_EMAIL + "</a>&gt;" +
                "</body></html>");
    }
}
