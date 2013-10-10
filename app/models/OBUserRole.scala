package models

import services.DBService

case class OBUserRole(id: Long, name: String) {
  override def toString() = this match {
	    case OBUserRole.ADMIN => "ADMIN"
	    case OBUserRole.BORROWER => "BORROWER"
	    case _ => ""
  }
  
}

object OBUserRole {
  val ADMIN = OBUserRole(1)
  val BORROWER = OBUserRole(2)
  
  private lazy val items = DBService.getUserRolesMap

  def apply(pID: Long): OBUserRole = OBUserRole(pID, items(pID.toString))
  
  def all = items
  
}