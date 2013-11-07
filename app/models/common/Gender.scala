package models.common

case class Gender(code: String) {
  override def toString() = if ("M".equals(code)) "Male" else "Female"
}

object Gender {
  val MALE = Gender("M")
  val FEMALE = Gender("F")
  
  def apply(pGender: Boolean): Gender = Gender(
      if (pGender) "M" else "F"
  )
}