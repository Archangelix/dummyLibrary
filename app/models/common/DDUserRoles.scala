package models.common

import services.DBService

case class DDUserRoles(code: String, desc: String)

object DDUserRoles {
  def apply(pCode: String): DDUserRoles = {
    DDUserRoles(pCode, UserRole(pCode).toString)
  }
  
  def all: List[(String, String)] = UserRole.all.toList.map(a => (a._1, a._2.toString))
}