package org.opennms.alexa.handlers.custom;

import static com.amazon.ask.request.Predicates.requestType;

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;

public class SessionEndedRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(final HandlerInput input) {
        return input.matches(requestType(SessionEndedRequest.class));
    }

    @Override
    public Optional<Response> handle(final HandlerInput input) {
        // cleanup code
        return input.getResponseBuilder().build();
    }

}
