package models.common

import services.DBService

case class DDCountry(code: String, desc: String)

object DDCountry {
  private lazy val items = DBService.getCountryMap
  
  def apply(pCode: String): DDCountry = {
    DDCountry(pCode, items(pCode))
  }
  
  def all = DBService.getCountryMap.toList
}