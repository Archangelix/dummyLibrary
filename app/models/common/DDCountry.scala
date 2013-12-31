package models.common

import services.PSQLService

case class DDCountry(code: String, desc: String)

object DDCountry {
  val dbService = PSQLService
  
  private lazy val items = dbService.getCountryMap
  
  def apply(pCode: String): DDCountry = {
    DDCountry(pCode, items(pCode))
  }
  
  def all = items.toList.sortBy(_._2)
}