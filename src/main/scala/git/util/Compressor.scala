/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package git
package util

import java.io.ByteArrayOutputStream
import java.util.zip.{Deflater, Inflater}

object Compressor {
  def decompressData(bytes: Seq[Byte]): Seq[Byte] = {
    val i = new Inflater
    i.setInput(bytes.toArray)

    val output = new ByteArrayOutputStream(bytes.length)
    val buffer = new Array[Byte](1024)

    while (!i.finished()) {
      val count = i.inflate(buffer)
      output.write(buffer, 0, count)
    }

    output.close()
    output.toByteArray
  }

  def compressData(bytes: Seq[Byte]): Seq[Byte] = {
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