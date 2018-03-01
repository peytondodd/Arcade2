package pl.themolka.arcade.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import pl.themolka.arcade.dom.Cursor;
import pl.themolka.arcade.dom.Locatable;
import pl.themolka.arcade.dom.Selection;
import pl.themolka.arcade.util.OptionalProvider;
import pl.themolka.arcade.util.StringId;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * String ID reference to an object.
 */
public class Ref<T> implements OptionalProvider<T>, Locatable {
    public static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{3,}$");

    private final String id;
    private transient Reference<T> provider;
    private Selection location;

    protected Ref(T provider) {
        this.id = this.fetchId(Objects.requireNonNull(provider, "provider cannot be null"));
        this.provide(provider);
    }

    protected Ref(String id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
    }

    @Override
    public void locate(Cursor start, Cursor end) {
        this.location = Selection.between(start, end);
    }

    @Override
    public boolean isSelectable() {
        return this.location != null;
    }

    @Override
    public Optional<T> optional() {
        return this.isProvided() ? Optional.of(this.get()) : Optional.empty();
    }

    @Override
    public Selection select() {
        return this.location;
    }

    public boolean clear() {
        boolean ok = this.isProvided();
        this.provider = null;
        return ok;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ref && this.id.equals(((Ref) obj).id);
    }

    /**
     * @throws NullPointerException if there is no provider.
     */
    public T get() {
        if (!this.isProvided()) {
            throw new NullPointerException("There is no provider object in this reference");
        }

        return this.provider.get();
    }

    public String getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public boolean isProvided() {
        return this.provider != null && this.provider.get() != null;
    }

    public boolean provide(T provider) {
        if (provider != null) {
            this.provider = new WeakReference<>(provider);
            return true;
        } else {
            this.clear();
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(this.id)
                .build();
    }

    private String fetchId(T provider) {
        if (provider instanceof StringId) {
            String providerId = ((StringId) provider).getId();
            if (providerId != null && !providerId.isEmpty()) {
                return providerId;
            }
        }

        return this.generateRandomId(provider);
    }

    private String generateRandomId(T provider) {
        return "_undefined-" + RandomStringUtils.randomAlphabetic(10);
    }

    //
    // Instancing
    //

    public static <T> Ref<T> of(String id) {
        return ofProvided(id, null);
    }

    public static <T> Ref<T> ofProvided(T provider) {
        return new Ref<>(provider);
    }

    public static <T> Ref<T> ofProvided(String id, T provider) {
        Ref<T> ref = new Ref<>(id);
        ref.provide(provider);
        return ref;
    }
}
