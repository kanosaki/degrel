package degrel.visualize;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

/**
 * Decorator for javafx.scene.canvas.GraphicsContext
 */
public class GraphicsContextWrapper {
    private GraphicsContext origin;

    public Canvas getCanvas() {
        return origin.getCanvas();
    }

    public boolean isPointInPath(double x, double y) {
        return origin.isPointInPath(x, y);
    }

    public void appendSVGPath(String svgpath) {
        origin.appendSVGPath(svgpath);
    }

    public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh) {
        origin.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public Paint getFill() {
        return origin.getFill();
    }

    public void translate(double x, double y) {
        origin.translate(x, y);
    }

    public void translate(Vec v) {
        origin.translate(v.x(), v.y());
    }

    public void arc(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length) {
        origin.arc(centerX, centerY, radiusX, radiusY, startAngle, length);
    }

    public void closePath() {
        origin.closePath();
    }

    public void fillRect(double x, double y, double w, double h) {
        origin.fillRect(x, y, w, h);
    }

    public void setFillRule(FillRule fillRule) {
        origin.setFillRule(fillRule);
    }

    public void strokeOval(double x, double y, double w, double h) {
        origin.strokeOval(x, y, w, h);
    }

    public void strokeOval(Vec topLeft, double w, double h) {
        origin.strokeOval(topLeft.x(), topLeft.y(), w, h);
    }

    public void strokeOvalCenter(Vec center, double w, double h) {
        origin.strokeOval(center.x() - w / 2, center.y() - h / 2, w, h);
    }

    public void beginPath() {
        origin.beginPath();
    }

    public void setEffect(Effect e) {
        origin.setEffect(e);
    }

    public void drawImage(Image img, double x, double y) {
        origin.drawImage(img, x, y);
    }

    public void fillPolygon(double[] xPoints, double[] yPoints, int nPoints) {
        origin.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillRoundRect(double x, double y, double w, double h, double arcWidth, double arcHeight) {
        origin.fillRoundRect(x, y, w, h, arcWidth, arcHeight);
    }

    public void stroke() {
        origin.stroke();
    }

    public StrokeLineCap getLineCap() {
        return origin.getLineCap();
    }

    public VPos getTextBaseline() {
        return origin.getTextBaseline();
    }

    public void strokePolyline(double[] xPoints, double[] yPoints, int nPoints) {
        origin.strokePolyline(xPoints, yPoints, nPoints);
    }

    public Paint getStroke() {
        return origin.getStroke();
    }

    public void drawImage(Image img, double x, double y, double w, double h) {
        origin.drawImage(img, x, y, w, h);
    }

    public void setTextAlign(TextAlignment align) {
        origin.setTextAlign(align);
    }

    public void lineTo(double x1, double y1) {
        origin.lineTo(x1, y1);
    }

    public void transform(double mxx, double myx, double mxy, double myy, double mxt, double myt) {
        origin.transform(mxx, myx, mxy, myy, mxt, myt);
    }

    public void setTransform(Affine xform) {
        origin.setTransform(xform);
    }

    public void save() {
        origin.save();
    }

    public void setFont(Font f) {
        origin.setFont(f);
    }

    public void rotate(double degrees) {
        origin.rotate(degrees);
    }

    public void moveTo(double x0, double y0) {
        origin.moveTo(x0, y0);
    }

    public void setLineWidth(double lw) {
        origin.setLineWidth(lw);
    }

    public void bezierCurveTo(double xc1, double yc1, double xc2, double yc2, double x1, double y1) {
        origin.bezierCurveTo(xc1, yc1, xc2, yc2, x1, y1);
    }

    public FillRule getFillRule() {
        return origin.getFillRule();
    }

    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        origin.arcTo(x1, y1, x2, y2, radius);
    }

    public Effect getEffect(Effect e) {
        return origin.getEffect(e);
    }

    public double getMiterLimit() {
        return origin.getMiterLimit();
    }

    public void applyEffect(Effect e) {
        origin.applyEffect(e);
    }

    public void setGlobalAlpha(double alpha) {
        origin.setGlobalAlpha(alpha);
    }

    public void restore() {
        origin.restore();
    }

    public void setTextBaseline(VPos baseline) {
        origin.setTextBaseline(baseline);
    }

    public void setFill(Paint p) {
        origin.setFill(p);
    }

    public void clearRect(double x, double y, double w, double h) {
        origin.clearRect(x, y, w, h);
    }

    public void strokeRect(double x, double y, double w, double h) {
        origin.strokeRect(x, y, w, h);
    }

    public void strokeArc(double x, double y, double w, double h, double startAngle, double arcExtent, ArcType closure) {
        origin.strokeArc(x, y, w, h, startAngle, arcExtent, closure);
    }

    public void rect(double x, double y, double w, double h) {
        origin.rect(x, y, w, h);
    }

    public void setStroke(Paint p) {
        origin.setStroke(p);
    }

    public void fill() {
        origin.fill();
    }

    public BlendMode getGlobalBlendMode() {
        return origin.getGlobalBlendMode();
    }

    public void setLineJoin(StrokeLineJoin join) {
        origin.setLineJoin(join);
    }

    public void setGlobalBlendMode(BlendMode op) {
        origin.setGlobalBlendMode(op);
    }

    public void setLineCap(StrokeLineCap cap) {
        origin.setLineCap(cap);
    }

    public void transform(Affine xform) {
        origin.transform(xform);
    }

    public void setMiterLimit(double ml) {
        origin.setMiterLimit(ml);
    }

    public void strokeRoundRect(double x, double y, double w, double h, double arcWidth, double arcHeight) {
        origin.strokeRoundRect(x, y, w, h, arcWidth, arcHeight);
    }

    public void strokeText(String text, double x, double y, double maxWidth) {
        origin.strokeText(text, x, y, maxWidth);
    }

    public void quadraticCurveTo(double xc, double yc, double x1, double y1) {
        origin.quadraticCurveTo(xc, yc, x1, y1);
    }

    public PixelWriter getPixelWriter() {
        return origin.getPixelWriter();
    }

    public Font getFont() {
        return origin.getFont();
    }

    public void strokeLine(double x1, double y1, double x2, double y2) {
        origin.strokeLine(x1, y1, x2, y2);
    }

    public void strokeLine(Vec v1, Vec v2) {
        origin.strokeLine(v1.x(), v1.y(), v2.x(), v2.y());
    }

    public void setTransform(double mxx, double myx, double mxy, double myy, double mxt, double myt) {
        origin.setTransform(mxx, myx, mxy, myy, mxt, myt);
    }

    public void fillText(String text, double x, double y, double maxWidth) {
        origin.fillText(text, x, y, maxWidth);
    }

    public void fillOval(double x, double y, double w, double h) {
        origin.fillOval(x, y, w, h);
    }

    public void fillOval(Vec topLeft, double w, double h) {
        origin.fillOval(topLeft.x(), topLeft.y(), w, h);
    }

    public void fillOvalCenter(Vec center, double w, double h) {
        origin.fillOval(center.x() - w / 2, center.y() - h / 2, w, h);
    }

    public void fillArc(double x, double y, double w, double h, double startAngle, double arcExtent, ArcType closure) {
        origin.fillArc(x, y, w, h, startAngle, arcExtent, closure);
    }

    public TextAlignment getTextAlign() {
        return origin.getTextAlign();
    }

    public Affine getTransform() {
        return origin.getTransform();
    }

    public void strokePolygon(double[] xPoints, double[] yPoints, int nPoints) {
        origin.strokePolygon(xPoints, yPoints, nPoints);
    }

    public double getGlobalAlpha() {
        return origin.getGlobalAlpha();
    }

    public Affine getTransform(Affine xform) {
        return origin.getTransform(xform);
    }

    public StrokeLineJoin getLineJoin() {
        return origin.getLineJoin();
    }

    public double getLineWidth() {
        return origin.getLineWidth();
    }

    public void strokeText(String text, double x, double y) {
        origin.strokeText(text, x, y);
    }

    public void scale(double x, double y) {
        origin.scale(x, y);
    }

    public void clip() {
        origin.clip();
    }

    public void fillText(String text, double x, double y) {
        origin.fillText(text, x, y);
    }

    public GraphicsContextWrapper(GraphicsContext origin) {
        this.origin = origin;
    }
}
