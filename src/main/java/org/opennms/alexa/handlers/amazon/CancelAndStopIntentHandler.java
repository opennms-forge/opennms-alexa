package org.opennms.alexa.handlers.amazon;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;

import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

public class CancelAndStopIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
    }

    @Override
    public Optional<Response> handle(final HandlerInput handlerInput) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(handlerInput);

        return handlerInput.getResponseBuilder()
                .withSpeech(localeManager.text("CANCEL_AND_STOP"))
                .withSimpleCard("OpenNMS Alexa Skill", localeManager.text("GOODBYE"))
                .build();
    }
}
