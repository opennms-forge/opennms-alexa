package org.opennms.alexa.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name = "names")
public final class GraphNameCollection extends JaxbListWrapper<String> {

    public GraphNameCollection() {
        super();
    }

    @XmlElement(name = "name")
    public List<String> getObjects() {
        return super.getObjects();
    }
}