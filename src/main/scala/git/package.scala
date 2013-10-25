package object git {
  implicit def listByte2ArrayByte(array: List[Byte]): Array[Byte] = array.toArray
}