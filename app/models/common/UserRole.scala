package models.common

case class UserRole(seqNo: Int, name: String) {
  override def toString() = this match {
	    case UserRole.ADMIN => "ADMIN"
	    case UserRole.BORROWER => "BORROWER"
	    case _ => ""
  }
  
}

object UserRole {
  val ADMIN = UserRole(1, "ADMINISTRATOR")
  val BORROWER = UserRole(2, "BORROWER")
  
  private lazy val items = Map(
      "1" -> ADMIN,
      "2" -> BORROWER
  )

  def apply(pID: Int): UserRole = items(pID.toString)
  
  def apply(pID: String): UserRole = UserRole(pID)

  def all = items
  
}