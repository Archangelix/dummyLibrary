package models.common

case class Gender(private val gender: String) {
  override def toString() = if ("M".equals(gender)) "Male" else "Female"
}

object Gender {
  val MALE = Gender("M")
  val FEMALE = Gender("F")
}