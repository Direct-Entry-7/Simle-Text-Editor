package util;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FXUtil {

    public static void highlightOnTextArea(TextArea textArea, String regex, Color highlight) {
        Platform.runLater(() -> {

            Text text = (Text) textArea.lookup("Text");
            Parent parent = textArea.lookup("Text").getParent().getParent();
            TextAreaSkin skin = (TextAreaSkin) textArea.getSkin();

            try {
                Field fldChildren = Parent.class.getDeclaredField("children");
                fldChildren.setAccessible(true);
                ObservableList<Node> children = (ObservableList<Node>) fldChildren.get(parent);
                Canvas canvas = getCanvas(children, text);
                List<Range> searchResult = getSearchResult(text.getText(), regex);
                repaint(canvas, skin, searchResult, highlight);

                text.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
                    canvas.setWidth(newValue.getWidth());
                    canvas.setHeight(newValue.getHeight());
                    repaint(canvas, skin, searchResult, highlight);
                });
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // Nothing to do here 😉
            }

        });
    }

    private static void repaint(Canvas canvas, TextAreaSkin skin, List<Range> ranges, Color highlight) {

        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(highlight);

        for (Range range : ranges) {

            for (int i = range.start; i < range.end; i++) {
                double x = skin.getCharacterBounds(i).getMinX();
                double y = skin.getCharacterBounds(i).getMinY();
                double width = skin.getCharacterBounds(i).getMaxX() - x;
                double height = skin.getCharacterBounds(i).getMaxY() - y;

                ctx.fillRect(x, y, width, height);
            }
        }
    }

    private static Canvas getCanvas(ObservableList<Node> children, Text text) {
        for (Node child : children) {

            if (child instanceof Canvas) {
                return (Canvas) child;
            }
        }

        Canvas canvas = new Canvas(text.getLayoutBounds().getWidth(), text.getLayoutBounds().getHeight());
        children.add(canvas);
        canvas.toBack();
        canvas.setMouseTransparent(true);
        return canvas;
    }

    private static List<Range> getSearchResult(String text, String regex) throws Exception {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        List<Range> ranges = new ArrayList<>();

        while (matcher.find()) {
            ranges.add(new Range(matcher.start(), matcher.end()));
        }

        return ranges;
    }

    private static class Range {
        private int start;
        private int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

}
