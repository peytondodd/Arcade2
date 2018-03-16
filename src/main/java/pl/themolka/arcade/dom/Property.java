package pl.themolka.arcade.dom;

public class Property extends Element implements Locatable {
    private Selection location;
    private Node parent;

    protected Property(Namespace namespace, String name, String value) {
        super(namespace, name, normalizeValue(value));
    }

    @Override
    public Property clone() {
        Property clone = (Property) super.clone();
        clone.location = this.location;
        clone.parent = this.parent;
        return clone;
    }

    @Override
    public boolean detach() {
        return this.setParent(null) != null;
    }

    @Override
    public Node getParent() {
        return this.parent;
    }

    @Override
    public boolean hasParent() {
        return this.parent != null;
    }

    @Override
    public boolean isSelectable() {
        return this.location != null || (this.parent != null && this.parent.isSelectable());
    }

    @Override
    public void locate(Selection selection) {
        this.location = selection;
    }

    @Override
    public Selection select() {
        if (this.location != null) {
            return this.location;
        } else if (this.hasParent()) {
            Selectable parent = this.getParent();
            if (parent.isSelectable()) {
                return parent.select();
            }
        }

        return null;
    }

    @Override
    public Node setParent(Node parent) {
        Node oldParent = this.parent;
        this.parent = parent;

        return oldParent;
    }

    @Override
    public String setValue(String value) {
        return super.setValue(normalizeValue(value));
    }

    @Override
    public String toString() {
        return this.getNamespace().format(this) + "=\"" + (this.hasValue() ? this.getValue() : "") + "\"";
    }

    /**
     * Make sure that value is never null.
     */
    static String normalizeValue(String value) {
        return value != null ? value : "";
    }

    //
    // Instancing
    //

    public static ImmutableProperty empty() {
        return ImmutableProperty.of(Namespace.getDefault(), "EmptyProperty");
    }

    public static Property of(Namespace namespace, String name) {
        return of(namespace, name, null);
    }

    public static Property of(Namespace namespace, String name, String value) {
        return new Property(namespace, name, value);
    }
}
