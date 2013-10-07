package common

import java.security.MessageDigest

/**
 * This object is used as a security utility, mainly used for login authentication.
 */
object SecurityUtil {
  val sha256 = MessageDigest.getInstance("SHA-256")

  def hex_digest(s: String): String = {
    sha256.digest(s.getBytes)
      .foldLeft("")((s: String, b: Byte) => s +
        Character.forDigit((b & 0xf0) >> 4, 16) +
        Character.forDigit(b & 0x0f, 16))
  }       
}