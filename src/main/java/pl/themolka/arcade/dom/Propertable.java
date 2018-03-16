package pl.themolka.arcade.dom;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Something that can hold properties.
 */
public interface Propertable {
    void appendProperties(Iterable<Property> append);

    default void appendProperties(Property... append) {
        if (append != null) {
            this.appendProperties(Arrays.asList(append));
        }
    }

    boolean hasProperties();

    List<Property> properties();

    List<Property> properties(Iterable<String> names);

    default List<Property> properties(String... names) {
        return names != null ? this.properties(Arrays.asList(names))
                             : null;
    }

    Property property(Iterable<String> names);

    default Property property(String... names) {
        return names != null ? this.property(Arrays.asList(names))
                             : null;
    }

    String propertyValue(Iterable<String> names);

    default String propertyValue(String... names) {
        return names != null ? this.propertyValue(Arrays.asList(names))
                             : null;
    }

    String propertyValue(Iterable<String> names, String def);

    Property setProperty(Property property);

    default Property setProperty(Namespace namespace, String name, String value) {
        return name != null ? this.setProperty(Property.of(namespace, name, value))
                            : null;
    }

    void setProperties(Iterable<Property> properties);

    default void setProperties(Property... properties) {
        if (properties != null) {
            this.setProperties(Arrays.asList(properties));
        }
    }

    void sortProperties(Comparator<? super Property> comparator);

    int unsetProperties();

    boolean unsetProperty(Property property);

    boolean unsetProperty(String name);
}
