package git
package util

import java.util.{TimeZone, Calendar, GregorianCalendar, Date}
import java.text.SimpleDateFormat

case class UserFields(name: String, email: String, date: Date)

object Parser {
  /**
   * Parses author fields from the given bytes.
   */
  def parseUserFields(reader: DataReader): UserFields = {
    val name = reader.takeStringWhile(_ != '<').trim

    reader ++ 1 // <

    val email = reader.takeStringWhile(_ != '>').trim

    reader ++ 2 // One '>' and one space.

    // Timestamp.
    val timestamp = reader.takeStringWhile(_ != ' ').trim.toLong

    reader ++ 1 // Space.

    // TZ offset.
    val timeZoneOffset = reader.takeStringWhile(_ != '\n')

    reader ++ 1 // LF.

    // Date.
    val cal = new GregorianCalendar(TimeZone.getTimeZone(s"GMT$timeZoneOffset"))
    cal.setTimeInMillis(timestamp * 1000)

    UserFields(name = name, email = email, date = cal.getTime)
  }

  /**
   * Converts a Date object into Git date format.
   */
  def dateToGitFormat(date: Date): String = {
    //val cal = new Calendar // TODO: Get timezone offsets to work.
    //new SimpleDateFormat("").parse()
    s"${date.getTime / 1000} +0000"
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