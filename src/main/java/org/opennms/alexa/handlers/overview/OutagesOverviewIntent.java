package org.opennms.alexa.handlers.overview;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.List;
import java.util.Optional;

import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.handlers.api.AbstractIntent;
import org.opennms.alexa.locale.LocaleUtils;
import org.opennms.alexa.rest.OpenNMSRestClient;
import org.opennms.netmgt.model.OnmsOutage;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.BodyTemplate2;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.RichText;
import com.amazon.ask.model.interfaces.display.TextContent;

public class OutagesOverviewIntent extends AbstractIntent {

    @Override
    public boolean canHandle(final HandlerInput handlerInput, final IntentRequest intentRequest) {
        return handlerInput.matches(intentName("OutagesOverviewIntent"));
    }

    @Override
    public Optional<Response> handleDeviceWithScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager) {
        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        final List<OnmsOutage> outages = openNMSRestClient.getEntities(OnmsOutage.class, "/rest/outages?serviceRegainedEvent=NULL", 0, 0);
        final long affectedNodes = outages.stream().map(o -> openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId())).distinct().count();

        final String speech;

        if (outages.size() == 0) {
            speech = localeManager.text("OVERVIEW_NO_OUTAGES");
        } else {
            speech = localeManager.text("OVERVIEW_OUTAGES").replaceAll("<COUNT>", String.valueOf(outages.size())).replaceAll("<NODES>", String.valueOf(affectedNodes));
        }

        return handlerInput.getResponseBuilder()
                .addRenderTemplateDirective(BodyTemplate2.builder()
                        .withTitle(localeManager.text("OVERVIEW_TITLE_OUTAGES"))
                        .withBackgroundImage(Image.builder()
                                .addSourcesItem(ImageInstance.builder()
                                        .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/overview-background.png")
                                        .withWidthPixels(1024)
                                        .withHeightPixels(600)
                                        .build())
                                .build())
                        .withTextContent(TextContent.builder()
                                .withPrimaryText(RichText.builder().withText(speech).build())
                                .build())
                        .build())
                .withSpeech(speech)
                .build();
    }

    @Override
    public Optional<Response> handleDeviceWithoutScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager) {
        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        final List<OnmsOutage> outages = openNMSRestClient.getEntities(OnmsOutage.class, "/rest/outages?serviceRegainedEvent=NULL", 0, 0);
        final long affectedNodes = outages.stream().map(o -> openNMSRestClient.getNodeLabelForIfService(o.getMonitoredService().getId())).distinct().count();

        final String speech;

        if (outages.size() == 0) {
            speech = localeManager.text("OVERVIEW_NO_OUTAGES");
        } else {
            speech = localeManager.text("OVERVIEW_OUTAGES").replaceAll("<COUNT>", String.valueOf(outages.size())).replaceAll("<NODES>", String.valueOf(affectedNodes));
        }

        return handlerInput.getResponseBuilder().withSpeech(localeManager.text("OVERVIEW_TITLE_OUTAGES"))
                .withSimpleCard("Outages", "Overview")
                .withSpeech(speech)
                .build();
    }
}
