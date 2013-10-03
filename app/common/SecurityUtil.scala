package common

import java.security.MessageDigest

object SecurityUtil {
  val sha = MessageDigest.getInstance("SHA-256")
	
  def hex_digest(s: String): String = {
    sha.digest(s.getBytes)
    .foldLeft("")((s: String, b: Byte) => s +
                  Character.forDigit((b & 0xf0) >> 4, 16) +
                  Character.forDigit(b & 0x0f, 16))
  }       
}