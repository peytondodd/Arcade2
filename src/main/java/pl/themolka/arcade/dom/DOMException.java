package pl.themolka.arcade.dom;

public class DOMException extends Exception {
    private final Element element;

    public DOMException(Element element) {
        this.element = element;
    }

    public DOMException(Element element, String message) {
        super(message);
        this.element = element;
    }

    public DOMException(Element element, String message, Throwable cause) {
        super(message, cause);
        this.element = element;
    }

    public DOMException(Element element, Throwable cause) {
        super(cause);
        this.element = element;
    }

    public Element getElement() {
        return this.element;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        String message = this.getMessage();
        if (message != null) {
            builder.append(message);
        } else {
            builder.append("Unknown error");
        }

        Element element = this.getElement();
        if (element != null) {
            if (element.isSelectable()) {
                Selection selection = element.select();
                if (selection != null) {
                    builder.append(", at line ").append(selection);
                }
            }

            String near = element.toShortString();
            if (near != null) {
                builder.append(" in '").append(near).append("'");
            } else {
                builder.append(".");
            }
        }

        return builder.toString();
    }
}