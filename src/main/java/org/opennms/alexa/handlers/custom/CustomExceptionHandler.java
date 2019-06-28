package org.opennms.alexa.handlers.custom;

import java.util.Optional;

import org.opennms.alexa.locale.LocaleUtils;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public class CustomExceptionHandler implements ExceptionHandler {
    @Override
    public boolean canHandle(final HandlerInput input, final Throwable throwable) {
        return throwable instanceof AskSdkException;
    }

    @Override
    public Optional<Response> handle(final HandlerInput handlerInput, final Throwable throwable) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(handlerInput);
        System.out.println(handlerInput.getRequestEnvelope().getRequest().getType());

        if (handlerInput.getRequestEnvelope().getRequest() instanceof IntentRequest) {
            System.out.println(((IntentRequest) handlerInput.getRequestEnvelope().getRequest()).getIntent());
        }

        throwable.printStackTrace();

        return handlerInput.getResponseBuilder()
                .withSpeech(localeManager.text("ERROR"))
                .build();
    }
}