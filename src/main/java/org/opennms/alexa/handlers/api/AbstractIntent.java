package org.opennms.alexa.handlers.api;

import java.util.Optional;

import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public abstract class AbstractIntent implements IntentRequestHandler {

    @Override
    public abstract boolean canHandle(final HandlerInput input, final IntentRequest intentRequest);

    public abstract Optional<Response> handleDeviceWithScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager);

    public abstract Optional<Response> handleDeviceWithoutScreen(final HandlerInput handlerInput, final IntentRequest intentRequest, final LocaleUtils.LocaleManager localeManager);

    @Override
    public Optional<Response> handle(final HandlerInput handlerInput, final IntentRequest intentRequest) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(handlerInput);
        if (handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces().getDisplay() != null) {
            return handleDeviceWithScreen(handlerInput, intentRequest, localeManager);
        } else {
            return handleDeviceWithoutScreen(handlerInput, intentRequest, localeManager);
        }
    }
}
