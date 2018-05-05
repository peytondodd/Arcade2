package pl.themolka.arcade.util.state;

public class ObjectState<T extends State> implements Statable<T> {
    private T state;

    protected ObjectState(T state) {
        this.state = state;
    }

    @Override
    public T getState() {
        return this.state;
    }

    @Override
    public boolean transform(T newState) {
        T old = this.getState();
        if (old != null) {
            old.destroy();
        }

        this.state = newState;
        newState.construct();
        return true;
    }

    //
    // Instancing
    //

    public static <T extends State> ObjectState defined(T state) {
        return new ObjectState<>(state);
    }

    public static <T extends State> ObjectState<T> undefined() {
        return new ObjectState<>(null);
    }
}