package org.opennms.alexa.handlers.api;

import java.util.Optional;

import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public abstract class AbstractScreenOnlyIntent extends AbstractIntent {

    @Override
    public final Optional<Response> handleDeviceWithoutScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager) {
        return handlerInput.getResponseBuilder().withSpeech(localeManager.text("SUPPORTED_ONLY_SCREEN_ON_SCREEN_DEVICES"))
                .build();
    }
}
