package git.util

import java.util.{GregorianCalendar, Date}
import scala.Predef.String

object Parser {

  def parseUserFields(tmpData: Array[Byte]): (String, String, Date, Array[Byte]) = {
    // Name.
    var data = tmpData
    val nameBytes = data.takeWhile(_ != '<')
    val name = new String(nameBytes).trim

    data = data.drop(nameBytes.length + 1)

    // Email.
    val emailBytes = data.takeWhile(_ != '>')
    val email = new String(emailBytes).trim

    data = data.drop(emailBytes.length + 2) // One '>' and one space.

    // Timestamp.
    val timestampBytes = data.takeWhile(_ != 32)

    val timestamp = new String(timestampBytes).trim.toInt

    data = data.drop(timestampBytes.length + 1)

    // TZ offset.
    val tzBytes = data.takeWhile(_ != '\n')
    val timeZoneOffset = new String(tzBytes)

    data = data.drop(tzBytes.length + 1)

    // Date.
    val cal = new GregorianCalendar
    cal.setTimeInMillis(timestamp.toLong * 1000)
    val date = cal.getTime

    // Return a tuple containing the data.
    (name, email, date, data)
  }
}
