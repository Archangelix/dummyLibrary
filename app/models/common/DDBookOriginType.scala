package models.common

import services.DBService

case class DDBookOriginType(code: String, desc: String)

object DDBookOriginType {
  private lazy val items = DBService.getBookOriginTypeMap
  
  def apply(pCode: String): DDBookOriginType = {
    DDBookOriginType(pCode, items(pCode))
  }
}