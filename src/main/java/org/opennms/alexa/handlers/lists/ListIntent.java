package org.opennms.alexa.handlers.lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.alexa.OpenNMSAlexaSkillServlet;
import org.opennms.alexa.locale.LocaleUtils;
import org.opennms.alexa.rest.OpenNMSRestClient;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.BackButtonBehavior;
import com.amazon.ask.model.interfaces.display.ElementSelectedRequest;
import com.amazon.ask.model.interfaces.display.Image;
import com.amazon.ask.model.interfaces.display.ImageInstance;
import com.amazon.ask.model.interfaces.display.ListItem;
import com.amazon.ask.model.interfaces.display.ListTemplate1;
import com.amazon.ask.model.interfaces.display.Template;

public class ListIntent<T> implements RequestHandler {

    public static int ITEM_LIMIT = 10;

    public static class Builder<B> {
        private String title;
        private String intentName;
        private String path;
        private Function<? super B, ? extends ListItem> listFunction;
        private Map<String, Function<? super B, ? extends Template>> selectionFunction = new HashMap<>();
        private Map<String, Function<? super B, String>> selectionSpeechFunction = new HashMap<>();
        private String parameters = "";

        private Class<B> clazz;

        private Builder() {
        }

        public Builder<B> withIntentName(final String intentName) {
            this.intentName = intentName;
            return this;
        }

        public Builder<B> withPath(final String path) {
            this.path = path;
            return this;
        }

        public Builder<B> withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder<B> withListFunction(final Function<? super B, ? extends ListItem> listFunction) {
            this.listFunction = listFunction;
            return this;
        }

        public Builder<B> withSelectionFunction(final String locale, final Function<? super B, ? extends Template> selectionFunction) {
            this.selectionFunction.put(locale, selectionFunction);
            return this;
        }

        public Builder<B> withSelectionSpeechFunction(final String locale, final Function<? super B, String> selectionSpeechFunction) {
            this.selectionSpeechFunction.put(locale, selectionSpeechFunction);
            return this;
        }

        public Builder<B> withParameters(final String parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder<B> withClass(final Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public ListIntent<B> build() {
            return new ListIntent<>(this);
        }
    }

    private final String title;
    private final String intentName;
    private final String path;
    private final Function<? super T, ? extends ListItem> listFunction;
    private final Map<String, Function<? super T, ? extends Template>> selectionFunction;
    private Map<String, Function<? super T, String>> selectionSpeechFunction = new HashMap<>();
    private final Class<T> clazz;
    private String parameters;

    private ListIntent(Builder<T> builder) {
        this.intentName = builder.intentName;
        this.path = builder.path;
        this.listFunction = builder.listFunction;
        this.selectionFunction = builder.selectionFunction;
        this.selectionSpeechFunction = builder.selectionSpeechFunction;
        this.parameters = builder.parameters;
        this.clazz = builder.clazz;
        this.title = builder.title;
    }

    public static <T> ListIntent.Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public boolean canHandle(final HandlerInput handlerInput) {
        boolean isIntentRequest = handlerInput.getRequestEnvelope().getRequest() instanceof IntentRequest;

        final String searchIntent = (String) handlerInput.getAttributesManager().getSessionAttributes().get("searchIntent");

        if (isIntentRequest) {
            final IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
            final String requestIntentName = intentRequest.getIntent().getName();


            return (// matching intent / initial search
                    this.intentName.equals(requestIntentName) ||
                            // search more results
                            "SearchMoreIntent".equals(requestIntentName) && this.intentName.equals(searchIntent) ||
                            // item selection by voice
                            this.intentName.equals(searchIntent) && requestIntentName.equals("ListSelectionIntent"));

        } else {
            // handle selection by touch
            return this.intentName.equals(searchIntent) && "Display.ElementSelected".equals(handlerInput.getRequestEnvelope().getRequest().getType());
        }
    }

    @Override
    public Optional<Response> handle(final HandlerInput handlerInput) {
        final LocaleUtils.LocaleManager localeManager = LocaleUtils.createLocaleManager(handlerInput);
        final String searchIntent = (String) handlerInput.getAttributesManager().getSessionAttributes().get("searchIntent");
        final String requestIntentName;

        // retrieve the intent name
        if (handlerInput.getRequestEnvelope().getRequest() instanceof IntentRequest) {
            requestIntentName = ((IntentRequest) handlerInput.getRequestEnvelope().getRequest()).getIntent().getName();
        } else {
            requestIntentName = null;
        }

        boolean selectionByTouch = false;
        boolean selectionByVoice = false;
        boolean initialSearch = false;
        boolean searchMore = false;

        // check for non-screen device
        if (handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces().getDisplay() == null) {
            return handlerInput.getResponseBuilder().withSpeech(localeManager.text("SUPPORTED_ONLY_SCREEN_ON_SCREEN_DEVICES"))
                    .build();
        }

        // check for inital search
        if (this.intentName.equals(requestIntentName)) {
            initialSearch = true;
        }

        // check for search more
        if ("SearchMoreIntent".equals(requestIntentName) && this.intentName.equals(searchIntent)) {
            searchMore = true;
        }

        // check for selection by touch
        if (this.intentName.equals(searchIntent) && "Display.ElementSelected".equals(handlerInput.getRequestEnvelope().getRequest().getType())) {
            selectionByTouch = true;
        }

        // check for selection by voice
        if ("ListSelectionIntent".equals(requestIntentName)) {
            selectionByVoice = true;
        }

        final OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient();

        int offset = 0;

        // retrieve the offset
        if (!initialSearch && handlerInput.getAttributesManager().getSessionAttributes().containsKey("offset")) {
            offset = Integer.valueOf(handlerInput.getAttributesManager().getSessionAttributes().get("offset").toString());
        }

        // add item limit when searching for more results
        if (searchMore) {
            offset += ITEM_LIMIT;
        }

        // check for selection
        if (selectionByTouch || selectionByVoice) {
            T entity = null;

            if (selectionByTouch) {
                final int id;
                try {
                    id = Integer.parseInt(((ElementSelectedRequest) handlerInput.getRequestEnvelope().getRequest()).getToken());

                } catch (NumberFormatException e) {
                    return handlerInput.getResponseBuilder()
                            .withSpeech(localeManager.text("ERROR"))
                            .build();
                }
                entity = (T) openNMSRestClient.getEntity(clazz, path, id);
            }

            if (selectionByVoice) {
                final int id;
                try {
                    id = Integer.parseInt(((IntentRequest) handlerInput.getRequestEnvelope().getRequest()).getIntent().getSlots().get("number").getValue());
                } catch (NumberFormatException e) {
                    return handlerInput.getResponseBuilder()
                            .withSpeech(localeManager.text("ERROR"))
                            .build();
                }

                final List<T> entities = openNMSRestClient.getEntities(clazz, path, parameters,offset - 1 + id, 1);
                if (entities.size() == 1) {
                    entity = entities.get(0);
                }
            }

            if (entity != null && this.selectionFunction != null) {
                handlerInput.getAttributesManager().getSessionAttributes().remove("searchIntent");
                handlerInput.getAttributesManager().getSessionAttributes().remove("offset");

                final String speech;

                if (selectionSpeechFunction.containsKey(localeManager.getLocale())) {
                    speech = selectionSpeechFunction.get(localeManager.getLocale()).apply(entity);
                } else {
                    speech = selectionSpeechFunction.get(LocaleUtils.DEFAULT).apply(entity);
                }

                final Template template;

                if (selectionFunction.containsKey(localeManager.getLocale())) {
                    template = selectionFunction.get(localeManager.getLocale()).apply(entity);
                } else {
                    template = selectionFunction.get(LocaleUtils.DEFAULT).apply(entity);
                }

                return handlerInput.getResponseBuilder()
                        .addRenderTemplateDirective(template)
                        .withSpeech(speech)
                        .withShouldEndSession(null)
                        .withReprompt(localeManager.text("REPROMPT"))
                        .build();
            } else {
                return handlerInput.getResponseBuilder()
                        .withSpeech(localeManager.text("NO_DETAILS_AVAILABLE"))
                        .withSimpleCard(this.title, localeManager.text("NO_DETAILS_AVAILABLE"))
                        .withShouldEndSession(null).build();
            }
        }

        // handle listing of search results
        handlerInput.getAttributesManager().getSessionAttributes().put("searchIntent", this.intentName);
        handlerInput.getAttributesManager().getSessionAttributes().put("offset", offset);

        final List<T> entities = openNMSRestClient.getEntities(clazz, path, parameters, offset, ITEM_LIMIT);
        final List<ListItem> items = entities.stream().map(listFunction).collect(Collectors.toList());

        if (entities.size() > 0) {
            final Template template = ListTemplate1.builder()
                    .withTitle(this.title)
                    .withListItems(items)
                    .withBackButton(BackButtonBehavior.HIDDEN)
                    .withBackgroundImage(Image.builder()
                            .addSourcesItem(ImageInstance.builder()
                                    .withUrl(OpenNMSAlexaSkillServlet.SERVLET_URL + "/images/background.png")
                                    .withWidthPixels(1025)
                                    .withHeightPixels(600)
                                    .build())
                            .build())
                    .build();

            return handlerInput.getResponseBuilder()
                    .withSpeech(localeManager.text("LIST_RESULTS_FOUND"))
                    .addRenderTemplateDirective(template)
                    .withShouldEndSession(null).build();
        } else {
            if (offset == 0) {
                return handlerInput.getResponseBuilder()
                        .withSpeech(localeManager.text("LIST_NO_RESULTS"))
                        .withSimpleCard(this.title, localeManager.text("LIST_NO_RESULTS"))
                        .withShouldEndSession(null).build();
            } else {
                return handlerInput.getResponseBuilder()
                        .withSpeech(localeManager.text("LIST_NO_MORE_RESULTS"))
                        .withSimpleCard(this.title, localeManager.text("LIST_NO_MORE_RESULTS"))
                        .withShouldEndSession(null).build();
            }
        }
    }
}
