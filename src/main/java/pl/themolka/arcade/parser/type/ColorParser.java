package pl.themolka.arcade.parser.type;

import org.bukkit.Color;
import pl.themolka.arcade.dom.Element;
import pl.themolka.arcade.parser.ElementParser;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.ParserUtils;
import pl.themolka.arcade.parser.Produces;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Produces(Color.class)
public class ColorParser extends ElementParser<Color>
                         implements InstallableParser {
    private Parser<Integer> redParser;
    private Parser<Integer> greenParser;
    private Parser<Integer> blueParser;

    @Override
    public void install(ParserContext context) {
        this.redParser = context.type(Integer.class);
        this.greenParser = context.type(Integer.class);
        this.blueParser = context.type(Integer.class);
    }

    @Override
    public Set<Object> expect() {
        return Collections.singleton("RGB color");
    }

    @Override
    protected ParserResult<Color> parseElement(Element element, String name, String value) throws ParserException {
        List<String> array = ParserUtils.array(value, 3);
        if (array.size() < 3) {
            throw this.fail(element, name, value, "RGB requires a list of 3 numbers");
        }

        int red = this.color(element, name, value, "red", this.redParser.parseWithValue(element, value).orFail());
        int green = this.color(element, name, value, "green", this.greenParser.parseWithValue(element, value).orFail());
        int blue = this.color(element, name, value, "blue", this.blueParser.parseWithValue(element, value).orFail());
        return ParserResult.fine(element, name, value, Color.fromRGB(red, green, blue));
    }

    private int color(Element element, String name, String value, String friendly, int color) throws ParserException {
        if (color < 0 || color > 255) {
            throw this.fail(element, name, value, friendly + " must be in range 0-255");
        }

        return color;
    }
}