package org.opennms.alexa.model;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class PrefabGraph {
    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "title")
    private String title;

    @XmlElement(name = "columns")
    private String[] columns;

    @XmlElement(name = "command")
    private String command;

    @XmlElement(name = "externalValues")
    private String[] externalValues;

    @XmlElement(name = "propertiesValues")
    private String[] propertiesValues;

    @XmlAttribute(name = "order")
    private int order;

    @XmlElement(name = "types")
    private String[] types = new String[0];

    @XmlAttribute(name = "description")
    private String description;

    @XmlAttribute(name = "width")
    private Integer graphWidth;

    @XmlAttribute(name = "height")
    private Integer graphHeight;

    @XmlElement(name = "suppress")
    private String[] suppress;

    public PrefabGraph() {
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getCommand() {
        return command;
    }

    public String[] getExternalValues() {
        return externalValues;
    }

    public String[] getPropertiesValues() {
        return propertiesValues;
    }

    public int getOrder() {
        return order;
    }

    public String[] getTypes() {
        return types;
    }

    public String getDescription() {
        return description;
    }

    public Integer getGraphWidth() {
        return graphWidth;
    }

    public Integer getGraphHeight() {
        return graphHeight;
    }

    public String[] getSuppress() {
        return suppress;
    }

    @Override
    public String toString() {
        return "PrefabGraph{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", columns=" + Arrays.toString(columns) +
                ", externalValues=" + Arrays.toString(externalValues) +
                ", propertiesValues=" + Arrays.toString(propertiesValues) +
                ", order=" + order +
                ", types=" + Arrays.toString(types) +
                ", description='" + description + '\'' +
                ", graphWidth=" + graphWidth +
                ", graphHeight=" + graphHeight +
                ", suppress=" + Arrays.toString(suppress) +
                '}';
    }
}
