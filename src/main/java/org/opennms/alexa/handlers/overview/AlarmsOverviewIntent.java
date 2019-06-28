package org.opennms.alexa.handlers.overview;

import static com.amazon.ask.request.Predicates.intentName;
import static org.opennms.alexa.locale.LocaleUtils.severity;

import java.util.List;
import java.util.Optional;

import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.handlers.api.AbstractIntent;
import org.opennms.alexa.locale.LocaleUtils;
import org.opennms.alexa.rest.OpenNMSRestClient;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.BodyTemplate2;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.RichText;
import com.amazon.ask.model.interfaces.display.TextContent;

public class AlarmsOverviewIntent extends AbstractIntent {
    @Override
    public boolean canHandle(HandlerInput handlerInput, final IntentRequest intentRequest) {
        return handlerInput.matches(intentName("AlarmsOverviewIntent"));
    }

    @Override
    public Optional<Response> handleDeviceWithScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager) {
        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        final List<OnmsAlarm> alarms = openNMSRestClient.getEntities(OnmsAlarm.class, "/rest/alarms","alarmAckTime=NULL", 0, 0);
        final OnmsSeverity maxSeverity = alarms.stream().map(a -> a.getSeverity()).max(OnmsSeverity::compareTo).get();

        final String speech;

        if (alarms.size() == 0) {
            speech = localeManager.text("OVERVIEW_NO_ALARMS");
        } else {
            speech = localeManager.text("OVERVIEW_ALARMS").replaceAll("<COUNT>", String.valueOf(alarms.size())).replaceAll("<SEVERITY>", severity(localeManager.getLocale(), maxSeverity.getLabel()));
        }

        return handlerInput.getResponseBuilder()
                .addRenderTemplateDirective(BodyTemplate2.builder()
                        .withTitle(localeManager.text("OVERVIEW_TITLE_ALARMS"))
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

        final List<OnmsAlarm> alarms = openNMSRestClient.getEntities(OnmsAlarm.class, "/rest/alarms","alarmAckTime=NULL", 0, 0);
        final OnmsSeverity maxSeverity = alarms.stream().map(a -> a.getSeverity()).max(OnmsSeverity::compareTo).get();

        final String speech;

        if (alarms.size() == 0) {
            speech = localeManager.text("OVERVIEW_NO_ALARMS");
        } else {
            speech = localeManager.text("OVERVIEW_ALARMS").replaceAll("<COUNT>", String.valueOf(alarms.size())).replaceAll("<SEVERITY>", severity(localeManager.getLocale(), maxSeverity.getLabel()));
        }

        return handlerInput.getResponseBuilder().withSpeech(localeManager.text("OVERVIEW_TITLE_ALARMS"))
                .withSimpleCard("Alarms", "Overview")
                .withSpeech(speech)
                .build();
    }
}
