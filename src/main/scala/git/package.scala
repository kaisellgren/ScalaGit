/**
 * Created by home on 10/5/13.
 */
package object git {

  implicit def arrayShort2ArrayByte(array: Array[Short]): Array[Byte] = array.map(_.toByte)
}
