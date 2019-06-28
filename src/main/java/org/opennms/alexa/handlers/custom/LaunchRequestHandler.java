package org.opennms.alexa.handlers.custom;

import static com.amazon.ask.request.Predicates.requestType;

import java.util.Optional;

import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.BodyTemplate6;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.PlainText;
import com.amazon.ask.model.interfaces.display.TextContent;

public class LaunchRequestHandler implements RequestHandler {
    @Override
    public boolean canHandle(final HandlerInput handlerInput) {
        return handlerInput.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(final HandlerInput handlerInput) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(handlerInput);

        if (handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces().getDisplay() == null) {
            return handlerInput.getResponseBuilder().withSpeech(localeManager.text("SUPPORTED_ONLY_SCREEN_ON_SCREEN_DEVICES")).build();
        }

        final BodyTemplate6 template = BodyTemplate6.builder()
                .withBackgroundImage(Image.builder()
                        .addSourcesItem(ImageInstance.builder()
                                .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/launch-background.png")
                                .withWidthPixels(1025)
                                .withHeightPixels(600)
                                .build())
                        .build())
                .withTextContent(TextContent.builder()
                        .withPrimaryText(PlainText.builder()
                                .withText(localeManager.text("LAUNCH_TEXT"))
                                .build())
                        .build())
                .build();

        return handlerInput.getResponseBuilder()
                .withSpeech(localeManager.text("LAUNCH_SPEECH_TEXT"))
                .addRenderTemplateDirective(template)
                .addHintDirective(localeManager.text("LAUNCH_HINT"))
                .build();
    }
}
