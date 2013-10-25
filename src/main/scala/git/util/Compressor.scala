package git
package util

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.util.zip.{Deflater, Inflater}
import scala.collection.mutable.ListBuffer

object Compressor {
  def decompressData(bytes: List[Byte]): List[Byte] = {
    val i = new Inflater
    i.setInput(bytes.toArray)

    val output = new ByteArrayOutputStream(bytes.length)
    val buffer = new Array[Byte](1024)

    while (!i.finished()) {
      val count = i.inflate(buffer)
      output.write(buffer, 0, count)
    }

    output.close()
    output.toByteArray.toList
  }

  def compressData(bytes: List[Byte]): List[Byte] = {
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
    output.toByteArray.toList
  }
}