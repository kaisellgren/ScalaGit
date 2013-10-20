package git
package util

import java.util.{Calendar, GregorianCalendar, Date}
import java.text.SimpleDateFormat

object Parser {
  /**
   * Parses author fields from the given bytes.
   */
  def parseUserFields(tmpData: Array[Short]): (String, String, Date, Array[Short]) = { // TODO: Return case class
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
    val timestamp = new String(timestampBytes).trim.toLong

    data = data.drop(timestampBytes.length + 1)

    // TZ offset.
    val tzBytes = data.takeWhile(_ != '\n')
    val timeZoneOffset = new String(tzBytes) // TODO: We aren't using these

    data = data.drop(tzBytes.length + 1)

    // Date.
    val cal = new GregorianCalendar // TODO:
    cal.setTimeInMillis(timestamp * 1000)
    val date = cal.getTime

    // Return a tuple containing the data.
    (name, email, date, data)
  }

  /**
   * Converts a Date object into Git date format.
   */
  def dateToGitFormat(date: Date): String = {
    //val cal = new Calendar // TODO: Get timezone offsets to work.
    //new SimpleDateFormat("").parse()
    s"${date.getTime} +0000"
  }

  /**
   * Tells whether the given string is a valid Git safe string.
   *
   * A sequence of bytes not containing the ASCII character byte
	 * values NUL (0x00), LF (0x0a), '<' (0c3c), or '>' (0x3e).
	 *
   * The sequence may not begin or end with any bytes with the
   * following ASCII character byte values: SPACE (0x20),
   * '.' (0x2e), ',' (0x2c), ':' (0x3a), ';' (0x3b), '<' (0x3c),
	 * '>' (0x3e), '"' (0x22), "'" (0x27).
   */
  def isValidGitSafeString(input: String): Boolean = {
    val onlyInMiddle = List(0x20, 0x2e, 0x2c, 0x3a, 0x3b, 0x3c, 0x3e, 0x22, 0x27)

    if (List(0x00, 0x0a, 0x3c, 0x3e).exists(input.contains(_))) false
    else List(input(0), input(input.length -1)).exists(onlyInMiddle.contains(_))
  }
}