package models.common

case class Gender(private val gender: String) {
  override def toString() = this.gender
}

object Gender {
  val MALE = Gender("M")
  val FEMALE = Gender("F")
}