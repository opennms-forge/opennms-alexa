package org.opennms.alexa.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name = "prefab-graphs")
public final class PrefabGraphCollection extends JaxbListWrapper<PrefabGraph> {
    public PrefabGraphCollection() {
        super();
    }

    @XmlElement(name = "prefab-graph")
    public List<PrefabGraph> getObjects() {
        return super.getObjects();
    }
}
