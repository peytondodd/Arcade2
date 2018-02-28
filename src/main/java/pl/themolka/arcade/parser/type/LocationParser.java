package pl.themolka.arcade.parser.type;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NodeParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.Produces;

import java.util.Collections;
import java.util.Set;

@Produces(Location.class)
public class LocationParser extends NodeParser<Location>
                            implements InstallableParser {
    private Parser<Vector> vectorParser;
    private Parser<Float> yawParser;
    private Parser<Float> pitchParser;

    @Override
    public void install(ParserContext context) {
        this.vectorParser = context.type(Vector.class);
        this.yawParser = context.type(Float.class);
        this.pitchParser = context.type(Float.class);
    }

    @Override
    public Set<Object> expect() {
        return Collections.singleton("a location");
    }

    @Override
    protected ParserResult<Location> parseNode(Node node, String name, String value) throws ParserException {
        Vector vector = this.vectorParser.parseWithDefinition(node, name, value).orFail();
        float yaw = this.yawParser.parse(node.property("yaw", "horizontal")).orDefault(180F); // north
        float pitch = this.pitchParser.parse(node.property("pitch", "vertical")).orDefault(0F); // forward
        return ParserResult.fine(node, name, value, new Location((World) null, vector, yaw, pitch));
    }
}