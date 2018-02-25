package pl.themolka.arcade.dom;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Node extends Element implements MutableLocatable, Parent<Node>, Propertable {
    enum Type {
        PRIMITIVE, TREE, UNKNOWN
    }

    private Node(String name) {
        super(name);
    }

    private Node(String name, String value) {
        super(name, value);
    }

    private final Propertable properties = new Properties();
    private final List<Node> children = new ArrayList<>();

    private Cursor location;
    private Node parent;

    @Override
    public boolean add(Collection<Node> children) {
        if (children != null) {
            this.resetPrimitive(); // switch to the tree type if needed
            return this.children.addAll(children);
        }

        return false;
    }

    @Override
    public void appendChildren(Iterable<Node> append, boolean deep) {
        if (append != null) {
            this.append(append, deep, false);
        }
    }

    @Override
    public void appendProperties(Iterable<Property> append) {
        this.properties.appendProperties(append);
    }

    @Override
    public List<Node> children() {
        return new ArrayList<>(this.children);
    }

    @Override
    public List<Node> children(Iterable<String> names) {
        List<Node> results = new ArrayList<>();

        if (names != null) {
            for (Node child : this.children) {
                for (String name : names) {
                    if (name != null && child.getName().equals(name)) {
                        results.add(child);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public int clearChildren() {
        int count = this.children.size();
        this.children.clear();
        return count;
    }

    @Override
    public Node firstChild() {
        return this.children.isEmpty() ? null : this.children.get(0);
    }

    @Override
    public Node firstChild(Iterable<String> names) {
        List<Node> children = this.children(names);
        return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public Cursor getLocation() {
        return this.location;
    }

    @Override
    public Node getParent() {
        return this.parent;
    }

    @Override
    public boolean hasLocation() {
        return this.location != null;
    }

    @Override
    public boolean hasParent() {
        return this.parent != null;
    }

    @Override
    public boolean hasProperties() {
        return this.properties.hasProperties();
    }

    @Override
    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    @Override
    public boolean isSelectable() {
        return this.hasLocation();
    }

    @Override
    public Node lastChild() {
        int size = this.children.size();
        return size == 0 ? null : this.children.get(size - 1);
    }

    @Override
    public Node lastChild(Iterable<String> names) {
        List<Node> children = this.children(names);
        return children.isEmpty() ? null : children.get(children.size() - 1);
    }

    @Override
    public List<Property> properties() {
        return this.properties.properties();
    }

    @Override
    public List<Property> properties(Iterable<String> names) {
        return this.properties.properties(names);
    }

    @Override
    public Property property(Iterable<String> names) {
        return this.properties.property(names);
    }

    @Override
    public String propertyValue(Iterable<String> names) {
        return this.properties.propertyValue(names);
    }

    @Override
    public String propertyValue(Iterable<String> names, String def) {
        return this.properties.propertyValue(names, def);
    }

    @Override
    public boolean remove(Iterable<Node> children) {
        if (children == null || this.isEmpty()) {
            return false;
        }

        boolean result = true;
        for (Node child : children) {
            if (!this.children.remove(child)) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean removeByName(Iterable<String> children) {
        if (children == null || this.isEmpty()) {
            return false;
        }

        boolean result = false;
        for (Node child : new ArrayList<>(this.children)) {
            for (String name : children) {
                if (name != null && child.getName().equals(name)) {
                    if (this.remove(child)) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    // TODO: Implement selections properly.
    @Override
    public Selection select() {
        return this.isSelectable() ? Selection.cursor(this.getLocation())
                                   : null;
    }

    @Override
    public void setLocation(Cursor location) {
        this.location = location;
    }

    @Override
    public Node setParent(Node parent) {
        Node oldParent = this.parent;
        this.parent = parent;

        return oldParent;
    }

    @Override
    public Property setProperty(Property property) {
        return this.properties.setProperty(property);
    }

    @Override
    public void setProperties(Iterable<Property> properties) {
        this.properties.setProperties(properties);
    }

    @Override
    public String setValue(String value) {
        this.resetTree();
        return super.setValue(value);
    }

    @Override
    public void sortChildren(Comparator<? super Node> comparator) {
        if (comparator != null) {
            this.children.sort(comparator);
        }
    }

    @Override
    public void sortProperties(Comparator<? super Property> comparator) {
        this.properties.sortProperties(comparator);
    }

    @Override
    public String toShortString() {
        return this.toString(true, false);
    }

    @Override
    public int unsetProperties() {
        return this.properties.unsetProperties();
    }

    @Override
    public boolean unsetProperty(Property property) {
        return this.properties.unsetProperty(property);
    }

    @Override
    public boolean unsetProperty(String name) {
        return this.properties.unsetProperty(name);
    }

    public void append(Iterable<Node> append, boolean deep, boolean properties) {
        if (append != null) {
            for (Node toAppend : append) {
                if (properties) {
                    this.appendProperties(toAppend.properties());
                }

                if (this.isPrimitive()) {
                    // Primitive types cannot hold children.
                    continue;
                }

                this.add(toAppend);
                if (deep) {
                    // Append again with its children if deep is true
                    toAppend.append(toAppend.children, true, properties);
                }
            }
        }
    }

    public void append(boolean deep, boolean properties, Node... append) {
        if (append != null) {
            this.append(Arrays.asList(append), deep, properties);
        }
    }

    public Type getType() {
        if (this.isPrimitive()) {
            return Type.PRIMITIVE;
        } else if (this.isTree()) {
            return Type.TREE;
        }

        return Type.UNKNOWN;
    }

    public boolean isPrimitive() {
        return this.hasValue();
    }

    public boolean isTree() {
        return !this.isEmpty();
    }

    @Override
    public String toString() {
        return this.toString(true, true);
    }

    public String toString(boolean children) {
        return this.toString(true, children);
    }

    public String toString(boolean properties, boolean children) {
        String nodeName = this.getName();

        String tag = nodeName;
        if (properties && this.hasProperties()) {
            tag += " " + StringUtils.join(this.properties, " ");
        }

        String value = null;
        if (this.hasValue()) {
            String realValue = this.getValue();
            if (!realValue.isEmpty()) {
                value = realValue.trim();
            }
        }

        List<Node> node;
        if (children && !this.isEmpty()) {
            node = this.children();
        } else {
            node = Collections.emptyList();
        }

        boolean closingTag = value == null || node.isEmpty();

        String startTag = this.toStringTag(false, tag, closingTag);
        StringBuilder builder = new StringBuilder(startTag);

        if (value != null) {
            builder.append(value);
        } else if (!node.isEmpty()) {
            builder.append(StringUtils.join(node, " "));
        }

        if (!closingTag) {
            builder.append(this.toStringTag(true, nodeName, false));
        }

        return builder.toString();
    }

    //
    // Helper Methods
    //

    private boolean resetPrimitive() {
        boolean ok = this.isPrimitive();
        this.setValue(null);

        return ok;
    }

    private boolean resetTree() {
        boolean ok = this.isTree();
        this.clearChildren();

        return ok;
    }

    private String toStringTag(boolean end, String tag, boolean closing) {
        return (end ? "</" : "<") + tag + (closing ? "/>" : ">");
    }

    //
    // Instancing
    //

    public static Node of(String name) {
        return new Node(name);
    }

    public static Node of(String name, List<Property> properties) {
        Node node = of(name);
        node.setProperties(properties);
        return node;
    }

    public static Node ofPrimitive(String name, String value) {
        return new Node(name, value);
    }

    public static Node ofPrimitive(String name, List<Property> properties, String value) {
        Node node = ofPrimitive(name, value);
        node.setProperties(properties);
        return node;
    }

    public static Node ofChildren(String name, List<Node> children) {
        Node node = of(name);
        node.add(children);
        return node;
    }

    public static Node ofChildren(String name, List<Property> properties, List<Node> children) {
        Node node = ofChildren(name, children);
        node.setProperties(properties);
        return node;
    }
}
