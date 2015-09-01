package degrel.core

import java.util.Random

trait Fingerprint {
  def get(v: Vertex): Long

  /**
   * Size for head block
   */
  def blockSize: Int

  val length = 63

  def pack(data: Long, position: Int): Long = data << (length - position)

  def hash(obj: AnyRef): Long = {
    val v = new Random(obj.hashCode()).nextLong()
    if (v < 0) {
      -v
    } else {
      v
    }
  }

  def formatBits(data: Long): String = {
    val chars = new Array[Char](java.lang.Long.SIZE)
    def fillChars(i: Int): Unit = {
      val flag = 1l << (63 - i)
      chars(i) = if ((data & flag) == flag) {
        '1'
      }
      else {
        '0'
      }
      if (i < 63) {
        fillChars(i + 1)
      }
    }
    fillChars(1)
    if (data < 0) {
      chars(0) = '1'
    } else {
      chars(0) = '0'
    }
    new String(chars)
  }

  def printlnBits(data: Long): Unit = {
    println(formatBits(data))
  }
}

class DepthFingerprint(val blockSize: Int, val depth: Int) extends Fingerprint {
  assert(blockSize > 0)
  assert(depth >= 0)

  private def calcFp(v: Vertex, curDepth: Int, targetDepth: Int): Long = {
    if (curDepth < targetDepth) {
      var ret: Long = 0
      v.edges.foreach { e =>
        ret |= calcFp(e.dst, curDepth + 1, targetDepth)
      }
      ret
    } else {
      this.headBlock(v)
    }
  }

  def labelBloom(lbl: Label): Long = {
    if (lbl == Label.V.wildcard) {
      0l
    }
    else {
      val pos = hash(lbl) % blockSize
      pack(1, pos.toInt)
    }
  }

  def headBlock(v: Vertex): Long = {
    val lb: Long = labelBloom(v.label)
    lb
  }

  override def get(v: Vertex): Long = {
    def updateFp(d: Int): Unit = {
      v.fingerprintCache |= (this.calcFp(v, 0, d) >>> (d * blockSize))
      if (d <= depth) {
        updateFp(d + 1)
      }
    }
    if (v.fingerprintCache == Fingerprint.EMPTY) {
      v.fingerprintCache = 0
      updateFp(0)
    }
    v.fingerprintCache
  }
}

class PathFingerprint(_bs: Int, _d: Int) extends DepthFingerprint(_bs, _d) {

  private def calcFp(v: Vertex, curDepth: Int, targetDepth: Int, offset: Int): Long = {
    if (curDepth < targetDepth) {
      var ret: Long = 0
      v.edges.foreach { e =>
        val nextOffset = offset + (hash(e.label) % blockSize).toInt
        ret |= calcFp(e.dst, curDepth + 1, targetDepth, nextOffset)
      }
      ret
    } else {
      this.headBlock(v) >>> offset
    }
  }


  override def get(v: Vertex): Long = {
    def updateFp(d: Int): Unit = {
      v.fingerprintCache |= this.calcFp(v, 0, d, 0)
      if (d <= depth) {
        updateFp(d + 1)
      }
    }
    if (v.fingerprintCache == Fingerprint.EMPTY) {
      v.fingerprintCache = 0
      updateFp(0)
    }
    v.fingerprintCache
  }
}

class HeadTailFingerprint(val blockSize: Int) extends Fingerprint {

  def get(v: Vertex): Long = {
    if (v.fingerprintCache == Fingerprint.EMPTY) {
      v.fingerprintCache = 0
      v.fingerprintCache = this.calcFingerprint(v)
    }
    v.fingerprintCache
  }

  def labelBloom(lbl: Label): Long = {
    if (lbl == Label.V.wildcard) {
      0l
    }
    else {
      val pos = hash(lbl) % (blockSize - 1) // shrink for head bit
      pack(1, pos.toInt)
    }
  }

  def headBlock(v: Vertex): Long = {
    val lb: Long = labelBloom(v.label)
    pack(1, 0) | (lb >>> 1)
  }


  private def calcFingerprint(v: Vertex): Long = {
    var base = headBlock(v)
    v.fingerprintCache = base
    val es = v.edges.toSeq.sortBy(_.label)
    es.foreach { e =>
      val shift = blockSize / 2 + (hash(e.label) % blockSize).toInt
      base |= (this.get(e.dst) >>> shift)
    }
    base
  }

}

object Fingerprint {
  val default = new HeadTailFingerprint(16)

  val EMPTY = Long.MinValue // 1000000....
}
