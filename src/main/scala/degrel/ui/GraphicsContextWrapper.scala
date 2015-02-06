package degrel.ui

import javafx.geometry.VPos
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.effect.{BlendMode, Effect}
import javafx.scene.image.{Image, PixelWriter}
import javafx.scene.paint.Paint
import javafx.scene.shape.{ArcType, FillRule, StrokeLineCap, StrokeLineJoin}
import javafx.scene.text.{Font, TextAlignment}
import javafx.scene.transform.Affine

import degrel.ui.Vec

/**
 * Decorator for javafx.scene.canvas.GraphicsContext
 */
class GraphicsContextWrapper(val origin: GraphicsContext) {
  def getCanvas: Canvas = {
    origin.getCanvas
  }

  def isPointInPath(x: Double, y: Double): Boolean = {
    origin.isPointInPath(x, y)
  }

  def appendSVGPath(svgpath: String) = {
    origin.appendSVGPath(svgpath)
  }

  def drawImage(img: Image,
                sx: Double,
                sy: Double,
                sw: Double,
                sh: Double,
                dx: Double,
                dy: Double,
                dw: Double,
                dh: Double) = {
    origin.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh)
  }

  def getFill: Paint = {
    origin.getFill
  }

  def translate(x: Double, y: Double) = {
    origin.translate(x, y)
  }

  def translate(v: Vec) = {
    origin.translate(v.x, v.y)
  }

  def arc(centerX: Double,
          centerY: Double,
          radiusX: Double,
          radiusY: Double,
          startAngle: Double,
          length: Double) = {
    origin.arc(centerX, centerY, radiusX, radiusY, startAngle, length)
  }

  def closePath() = {
    origin.closePath()
  }

  def fillRect(x: Double, y: Double, w: Double, h: Double) = {
    origin.fillRect(x, y, w, h)
  }

  def setFillRule(fillRule: FillRule) = origin.setFillRule(fillRule)

  def strokeOval(x: Double, y: Double, w: Double, h: Double) = {
    origin.strokeOval(x, y, w, h)
  }

  def strokeOval(topLeft: Vec, w: Double, h: Double) = {
    origin.strokeOval(topLeft.x, topLeft.y, w, h)
  }

  def strokeOvalCenter(center: Vec, w: Double, h: Double) = {
    origin.strokeOval(center.x - w / 2, center.y - h / 2, w, h)
  }

  def beginPath() = {
    origin.beginPath()
  }

  def setEffect(e: Effect) = {
    origin.setEffect(e)
  }

  def drawImage(img: Image, x: Double, y: Double) = {
    origin.drawImage(img, x, y)
  }

  def fillPolygon(xPoints: Array[Double], yPoints: Array[Double], nPoints: Int) = {
    origin.fillPolygon(xPoints, yPoints, nPoints)
  }

  def fillRoundRect(x: Double, y: Double, w: Double, h: Double, arcWidth: Double, arcHeight: Double) = {
    origin.fillRoundRect(x, y, w, h, arcWidth, arcHeight)
  }

  def stroke() = {
    origin.stroke()
  }

  def getLineCap: StrokeLineCap = {
    origin.getLineCap
  }

  def getTextBaseline: VPos = {
    origin.getTextBaseline
  }

  def strokePolyline(xPoints: Array[Double], yPoints: Array[Double], nPoints: Int) = {
    origin.strokePolyline(xPoints, yPoints, nPoints)
  }

  def getStroke: Paint = {
    origin.getStroke
  }

  def drawImage(img: Image, x: Double, y: Double, w: Double, h: Double) = {
    origin.drawImage(img, x, y, w, h)
  }

  def setTextAlign(align: TextAlignment) = {
    origin.setTextAlign(align)
  }

  def lineTo(x1: Double, y1: Double) = {
    origin.lineTo(x1, y1)
  }

  def transform(mxx: Double, myx: Double, mxy: Double, myy: Double, mxt: Double, myt: Double) = {
    origin.transform(mxx, myx, mxy, myy, mxt, myt)
  }

  def setTransform(xform: Affine) = {
    origin.setTransform(xform)
  }

  def save() = {
    origin.save()
  }

  def setFont(f: Font) = {
    origin.setFont(f)
  }

  def rotate(degrees: Double) = {
    origin.rotate(degrees)
  }

  def moveTo(x0: Double, y0: Double) = {
    origin.moveTo(x0, y0)
  }

  def setLineWidth(lw: Double) = {
    origin.setLineWidth(lw)
  }

  def bezierCurveTo(xc1: Double, yc1: Double, xc2: Double, yc2: Double, x1: Double, y1: Double) = {
    origin.bezierCurveTo(xc1, yc1, xc2, yc2, x1, y1)
  }

  def getFillRule: FillRule = {
    origin.getFillRule
  }

  def arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double) = {
    origin.arcTo(x1, y1, x2, y2, radius)
  }

  def getEffect(e: Effect): Effect = {
    origin.getEffect(e)
  }

  def getMiterLimit: Double = {
    origin.getMiterLimit
  }

  def applyEffect(e: Effect) = {
    origin.applyEffect(e)
  }

  def setGlobalAlpha(alpha: Double) = {
    origin.setGlobalAlpha(alpha)
  }

  def restore() = {
    origin.restore()
  }

  def setTextBaseline(baseline: VPos) = {
    origin.setTextBaseline(baseline)
  }

  def setFill(p: Paint) = {
    origin.setFill(p)
  }

  def clearRect(x: Double, y: Double, w: Double, h: Double) = {
    origin.clearRect(x, y, w, h)
  }

  def strokeRect(x: Double, y: Double, w: Double, h: Double) = {
    origin.strokeRect(x, y, w, h)
  }

  def strokeArc(x: Double, y: Double, w: Double, h: Double, startAngle: Double, arcExtent: Double, closure: ArcType) = {
    origin.strokeArc(x, y, w, h, startAngle, arcExtent, closure)
  }

  def rect(x: Double, y: Double, w: Double, h: Double) = {
    origin.rect(x, y, w, h)
  }

  def setStroke(p: Paint) = {
    origin.setStroke(p)
  }

  def fill() = {
    origin.fill()
  }

  def getGlobalBlendMode: BlendMode = {
    origin.getGlobalBlendMode
  }

  def setLineJoin(join: StrokeLineJoin) = {
    origin.setLineJoin(join)
  }

  def setGlobalBlendMode(op: BlendMode) = {
    origin.setGlobalBlendMode(op)
  }

  def setLineCap(cap: StrokeLineCap) = {
    origin.setLineCap(cap)
  }

  def transform(xform: Affine) = {
    origin.transform(xform)
  }

  def setMiterLimit(ml: Double) = {
    origin.setMiterLimit(ml)
  }

  def strokeRoundRect(x: Double, y: Double, w: Double, h: Double, arcWidth: Double, arcHeight: Double) = {
    origin.strokeRoundRect(x, y, w, h, arcWidth, arcHeight)
  }

  def strokeText(text: String, x: Double, y: Double, maxWidth: Double) = {
    origin.strokeText(text, x, y, maxWidth)
  }

  def quadraticCurveTo(xc: Double, yc: Double, x1: Double, y1: Double) = {
    origin.quadraticCurveTo(xc, yc, x1, y1)
  }

  def getPixelWriter: PixelWriter = {
    origin.getPixelWriter
  }

  def getFont: Font = {
    origin.getFont
  }

  def strokeLine(x1: Double, y1: Double, x2: Double, y2: Double) = {
    origin.strokeLine(x1, y1, x2, y2)
  }

  def strokeLine(v1: Vec, v2: Vec) = {
    origin.strokeLine(v1.x, v1.y, v2.x, v2.y)
  }

  def setTransform(mxx: Double, myx: Double, mxy: Double, myy: Double, mxt: Double, myt: Double) = {
    origin.setTransform(mxx, myx, mxy, myy, mxt, myt)
  }

  def fillText(text: String, x: Double, y: Double, maxWidth: Double) = {
    origin.fillText(text, x, y, maxWidth)
  }

  def fillOval(x: Double, y: Double, w: Double, h: Double) = {
    origin.fillOval(x, y, w, h)
  }

  def fillOval(topLeft: Vec, w: Double, h: Double) = {
    origin.fillOval(topLeft.x, topLeft.y, w, h)
  }

  def fillOvalCenter(center: Vec, w: Double, h: Double) = {
    origin.fillOval(center.x - w / 2, center.y - h / 2, w, h)
  }

  def fillOvalCenter(x: Double, y: Double, w: Double, h: Double) = {
    origin.fillOval(x - w / 2, y - h / 2, w, h)
  }

  def fillArc(x: Double, y: Double, w: Double, h: Double, startAngle: Double, arcExtent: Double, closure: ArcType) = {
    origin.fillArc(x, y, w, h, startAngle, arcExtent, closure)
  }

  def getTextAlign: TextAlignment = {
    origin.getTextAlign
  }

  def getTransform: Affine = {
    origin.getTransform
  }

  def strokePolygon(xPoints: Array[Double], yPoints: Array[Double], nPoints: Int) = {
    origin.strokePolygon(xPoints, yPoints, nPoints)
  }

  def getGlobalAlpha: Double = {
    origin.getGlobalAlpha
  }

  def getTransform(xform: Affine): Affine = {
    origin.getTransform(xform)
  }

  def getLineJoin: StrokeLineJoin = {
    origin.getLineJoin
  }

  def getLineWidth: Double = {
    origin.getLineWidth
  }

  def strokeText(text: String, x: Double, y: Double) = {
    origin.strokeText(text, x, y)
  }

  def scale(x: Double, y: Double) = {
    origin.scale(x, y)
  }

  def clip() = {
    origin.clip()
  }

  def fillText(text: String, x: Double, y: Double) = {
    origin.fillText(text, x, y)
  }

  /**
   * 矢印を描画します．
   * @param from 矢印の根元の座標
   * @param to 矢印の先の座標．鏃が描画(fill)されます
   * @param arrowHeight 鏃の長さ(高さ) 矢の方向の鏃の大きさです
   * @param arrowWidth 鏃の幅 矢の方向に垂直な方向の大きさです．この値は矢の中心からの半径に相当する長さで，鏃自体の幅はこれの2倍になります．
   */
  def fillArrow(from: Vec, to: Vec, arrowHeight: Double = 10, arrowWidth: Double = 5) = {
    origin.strokeLine(from.x, from.y, to.x, to.y)
    // 筈から鏃までのベクトル
    val delta = to - from
    // 鏃とシャフトの部分の交点の座標
    val base = to - delta.normalize(arrowHeight)
    // 鏃の三角形の筈側2点を求めます．シャフト方向のベクトルをPI/2回転させて，arrowWidthで長さを調整します
    val side = delta.rotate(Math.PI / 2).normalize(arrowWidth)
    val wing1 = base + side
    val wing2 = base - side
    origin.beginPath()
    origin.moveTo(to.x, to.y)
    origin.lineTo(wing1.x, wing1.y)
    origin.lineTo(wing2.x, wing2.y)
    origin.closePath()
    origin.fill()
  }
}