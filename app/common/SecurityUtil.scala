package common

import java.security.MessageDigest

import common.CommonUtil.isBlank
import play.api.data.FormError

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

  def validatePassword(pPassword1: String, pPassword2: String): Seq[Option[FormError]] = {
    Seq(
      if (isBlank(pPassword1)) {
        Some(new FormError("password", "Password is required."))
      } else None,
      if (!isBlank(pPassword1) && !pPassword1.equals(pPassword2)) {
        Some(new FormError("password2", "Both passwords must be the same."))
      } else {
        None
      }).filter(_ != None)
  }

}