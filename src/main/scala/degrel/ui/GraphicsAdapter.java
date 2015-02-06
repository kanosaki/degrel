package degrel.ui;

import degrel.ui.Vec;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class GraphicsAdapter extends Graphics2D {
    private Graphics2D origin;

    public GraphicsAdapter(Graphics2D origin) {
        origin.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        origin.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        this.origin = origin;
    }

    public GraphicsAdapter(Graphics origin) {
        this.origin = (Graphics2D) origin;
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        origin.draw3DRect(x, y, width, height, raised);
    }

    public void draw3DRect(double x, double y, double width, double height, boolean raised) {
        origin.draw3DRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height), raised);
    }

    public void draw3DRect(Vec v, double width, double height, boolean raised) {
        this.draw3DRect(v.x(), v.y(), width, height, raised);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        origin.fill3DRect(x, y, width, height, raised);
    }

    public void fill3DRect(double x, double y, double width, double height, boolean raised) {
        origin.fill3DRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height), raised);
    }

    public void fill3DRect(Vec v, double width, double height, boolean raised) {
        this.fill3DRect(v.x(), v.y(), width, height, raised);
    }

    @Override
    public void draw(Shape s) {
        origin.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return origin.drawImage(img, xform, obs);
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        origin.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        origin.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        origin.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        origin.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        origin.drawString(str, x, y);
    }

    public void drawString(String str, double x, double y) {
        origin.drawString(str, (float) x, (float) y);
    }

    public void drawString(String str, Vec v) {
        this.drawString(str, v.x(), v.y());
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        origin.drawString(iterator, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        origin.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        origin.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        origin.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return origin.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return origin.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite comp) {
        origin.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        origin.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        origin.setStroke(s);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        origin.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return origin.getRenderingHint(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        origin.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        origin.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return origin.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        origin.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        origin.translate(tx, ty);
    }

    public void translate(Vec v) {
        origin.translate(v.x(), v.y());
    }

    @Override
    public void rotate(double theta) {
        origin.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        origin.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        origin.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        origin.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        origin.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        origin.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return origin.getTransform();
    }

    @Override
    public Paint getPaint() {
        return origin.getPaint();
    }

    @Override
    public Composite getComposite() {
        return origin.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        origin.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return origin.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return origin.getStroke();
    }

    @Override
    public void clip(Shape s) {
        origin.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return origin.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return origin.create();
    }

    @Override
    public Graphics create(int x, int y, int width, int height) {
        return origin.create(x, y, width, height);
    }

    @Override
    public Color getColor() {
        return origin.getColor();
    }

    @Override
    public void setColor(Color c) {
        origin.setColor(c);
    }

    @Override
    public void setPaintMode() {
        origin.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        origin.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return origin.getFont();
    }

    @Override
    public void setFont(Font font) {
        origin.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics() {
        return origin.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return origin.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return origin.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        origin.clipRect(x, y, width, height);
    }

    public void clipRect(double x, double y, double width, double height) {
        origin.clipRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        origin.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return origin.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        origin.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        origin.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        origin.drawLine(x1, y1, x2, y2);
    }

    public void drawLine(double x1, double y1, double x2, double y2) {
        origin.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
    }

    public void drawLine(Vec from, Vec to) {
        this.drawLine(from.x(), from.y(), to.x(), to.y());
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        origin.fillRect(x, y, width, height);
    }

    public void fillRect(double x, double y, double width, double height) {
        origin.fillRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        origin.drawRect(x, y, width, height);
    }

    public void drawRect(double x, double y, double width, double height) {
        origin.drawRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        origin.clearRect(x, y, width, height);
    }

    public void clearRect(double x, double y, double width, double height) {
        origin.clearRect((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        origin.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        origin.drawRoundRect(
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(width),
                (int) Math.round(height),
                (int) Math.round(arcWidth),
                (int) Math.round(arcHeight));
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        origin.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void fillRoundRect(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        origin.fillRoundRect(
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(width),
                (int) Math.round(height),
                (int) Math.round(arcWidth),
                (int) Math.round(arcHeight));
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        origin.drawOval(x, y, width, height);
    }

    public void drawOval(double x, double y, double width, double height) {
        origin.drawOval((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    public void drawOval(Vec v, double width, double height) {
        this.drawOval(v.x(), v.y(), width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        origin.fillOval(x, y, width, height);
    }

    public void fillOval(double x, double y, double width, double height) {
        origin.fillOval((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
    }

    public void fillOval(Vec v, double width, double height) {
        this.fillOval(v.x(), v.y(), width, height);
    }

    public void fillOvalCenter(double x, double y, double width, double height) {
        this.fillOval(
                x - width / 2,
                y - height / 2,
                width,
                height);
    }

    public void fillOvalCenter(Vec center, double width, double height) {
        this.fillOval(
                center.x() - width / 2,
                center.y() - height / 2,
                width,
                height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        origin.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawArc(double x, double y, double width, double height, double startAngle, double arcAngle) {
        origin.drawArc(
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(width),
                (int) Math.round(height),
                (int) Math.round(startAngle),
                (int) Math.round(arcAngle));
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        origin.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillArc(double x, double y, double width, double height, double startAngle, double arcAngle) {
        origin.fillArc(
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(width),
                (int) Math.round(height),
                (int) Math.round(startAngle),
                (int) Math.round(arcAngle));
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        origin.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        origin.drawPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(Polygon p) {
        origin.drawPolygon(p);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        origin.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(Polygon p) {
        origin.fillPolygon(p);
    }

    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        origin.drawChars(data, offset, length, x, y);
    }

    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        origin.drawBytes(data, offset, length, x, y);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return origin.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return origin.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return origin.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return origin.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return origin.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return origin.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    @Override
    public void dispose() {
        origin.dispose();
    }

    @Override
    public void finalize() {
        origin.finalize();
    }

    @Override
    public String toString() {
        return origin.toString();
    }

    @Override
    @Deprecated
    public Rectangle getClipRect() {
        return origin.getClipRect();
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        return origin.hitClip(x, y, width, height);
    }

    @Override
    public Rectangle getClipBounds(Rectangle r) {
        return origin.getClipBounds(r);
    }
}
