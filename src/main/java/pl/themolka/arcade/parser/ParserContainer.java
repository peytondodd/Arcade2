package pl.themolka.arcade.parser;

import pl.themolka.arcade.util.Container;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParserContainer implements Container<Parser> {
    private final Map<Class<?>, Parser<?>> parsers = new HashMap<>();

    @Override
    public Class<Parser> getType() {
        return Parser.class;
    }

    public boolean contains(Class<?> clazz) {
        return this.parsers.containsKey(clazz);
    }

    public boolean containsParser(Parser<?> parser) {
        return this.parsers.containsValue(parser);
    }

    public <T extends Parser<T>> T getParser(Class<T> clazz) {
        return (T) this.parsers.get(clazz);
    }

    public Set<Class<?>> getParserClasses() {
        return this.parsers.keySet();
    }

    public Collection<Parser<?>> getParsers() {
        return this.parsers.values();
    }

    public void register(ParserContainer container) {
        Collection<Parser<?>> maps = container.getParsers();
        this.register(maps.toArray(new Parser<?>[maps.size()]));
    }

    public void register(Parser<?>... parsers) {
        for (Parser<?> parser : parsers) {
            if (!this.containsParser(parser)) {
                this.parsers.put(parser.getClass(), parser);
            }
        }
    }
}
