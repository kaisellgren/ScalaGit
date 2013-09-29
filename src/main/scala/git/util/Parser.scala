package git.util

import java.util.{GregorianCalendar, Date}

object Parser {

  def parseUserFields(tmpData: Array[Short]): (String, String, Date, Array[Short]) = {
    // Name.
    var data = tmpData
    val nameBytes = data.takeWhile(_ != '<')
    val name = new String(nameBytes.map(_.toByte)).trim

    data = data.drop(nameBytes.length + 1)

    // Email.
    val emailBytes = data.takeWhile(_ != '>')
    val email = new String(emailBytes.map(_.toByte)).trim

    data = data.drop(emailBytes.length + 2) // One '>' and one space.

    // Timestamp.
    val timestampBytes = data.takeWhile(_ != 32)

    val timestamp = new String(timestampBytes.map(_.toByte)).trim.toInt

    data = data.drop(timestampBytes.length + 1)

    // TZ offset.
    val tzBytes = data.takeWhile(_ != '\n')
    val timeZoneOffset = new String(tzBytes.map(_.toByte))

    data = data.drop(tzBytes.length + 1)

    // Date.
    val cal = new GregorianCalendar
    cal.setTimeInMillis(timestamp.toLong * 1000)
    val date = cal.getTime

    // Return a tuple containing the data.
    (name, email, date, data)
  }
}
