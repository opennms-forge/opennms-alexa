package org.opennms.alexa.handlers.amazon;


import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;

import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.BodyTemplate2;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.RichText;
import com.amazon.ask.model.interfaces.display.TextContent;

// 2018-July-09: AMAZON.FallackIntent is only currently available in en-US locale.
//              This handler will not be triggered except in that locale, so it can be
//              safely deployed for any locale.
public class FallbackIntentHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.FallbackIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(input);
        return input.getResponseBuilder()
                .addRenderTemplateDirective(BodyTemplate2.builder()
                        .withTitle("OpenNMS Alexa Skill")
                        .withBackgroundImage(Image.builder()
                                .addSourcesItem(ImageInstance.builder()
                                        .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/help-background.png")
                                        .withWidthPixels(1024)
                                        .withHeightPixels(600)
                                        .build())
                                .build())
                        .withTextContent(TextContent.builder()
                                .withPrimaryText(RichText.builder().withText(localeManager.text("FALLBACK")).build())
                                .build())
                        .build())
                .withSpeech(localeManager.text("FALLBACK"))
                .build();
    }
}
