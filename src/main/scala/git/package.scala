package object git {
  //implicit def arrayShort2ArrayByte(array: Array[Short]): Array[Byte] = array.map(_.toByte)
  implicit def listByte2ArrayByte(array: List[Byte]): Array[Byte] = array.toArray
}