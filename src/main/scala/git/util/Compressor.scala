package git.util

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.util.zip.Inflater

object Compressor {
  def decompressData(bytes: Array[Short]): Array[Short] = {
    val i = new Inflater
    i.setInput(bytes.map(_.toByte))

    val output = new ByteArrayOutputStream(bytes.length)
    val buffer = new Array[Byte](1024)

    while (!i.finished()) {
      val count = i.inflate(buffer)
      output.write(buffer, 0, count)
    }

    output.close()
    output.toByteArray.map(_.toShort)
  }
}