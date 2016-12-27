package pl.themolka.arcade.game;

import java.util.List;

public interface GameEvents {
    void onEnable();

    void onDisable();

    List<Object> onListenersRegister(List<Object> register);
}
