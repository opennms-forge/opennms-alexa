package org.opennms.alexa.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.resource.ResourceDTO;

@XmlRootElement(name = "graph-resource")
public final class GraphResourceDTO {
    @XmlElement(name = "resource")
    ResourceDTO resource;

    @XmlElement(name = "prefab-graphs")
    PrefabGraphCollection prefabGraphs;

    private GraphResourceDTO() {
        super();
    }

    public ResourceDTO getResource() {
        return resource;
    }

    public PrefabGraphCollection getPrefabGraphs() {
        return prefabGraphs;
    }
}
