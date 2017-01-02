package pl.themolka.arcade.example;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.module.Module;
import pl.themolka.arcade.module.ModuleInfo;

@ModuleInfo(id = "example")
public class ExampleModule extends Module<ExampleGame> {
    @Override
    public ExampleGame buildGameModule(Element xml, Game game) throws JDOMException {
        return new ExampleGame();
    }

    public void test() {

    }
}
