package git
package util

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.util.zip.{Deflater, Inflater}

object Compressor {
  def decompressData(bytes: Array[Short]): Array[Short] = {
    val i = new Inflater
    i.setInput(bytes)

    val output = new ByteArrayOutputStream(bytes.length)
    val buffer = new Array[Byte](1024)

    while (!i.finished()) {
      val count = i.inflate(buffer)
      output.write(buffer, 0, count)
    }

    output.close()
    output.toByteArray.map(_.toShort)
  }

  def compressData(bytes: List[Byte]): Array[Byte] = {
    val i = new Deflater
    i.setInput(bytes.toArray)

    val output = new ByteArrayOutputStream(bytes.length)

    i.finish()

    val buffer = new Array[Byte](1024)
    while (!i.finished()) {
      val count = i.deflate(buffer)
      output.write(buffer, 0, count)
    }

    output.close()
    output.toByteArray
  }
}