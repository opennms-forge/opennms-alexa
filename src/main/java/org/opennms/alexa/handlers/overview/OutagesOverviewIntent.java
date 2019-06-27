/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
