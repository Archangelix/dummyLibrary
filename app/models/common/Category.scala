package models.common

import services.PSQLService


case class Category(code: Int, name: String) {
  override def toString() = name
}

object Category {
  val dbService = PSQLService
  
  private def items = dbService.getCategoriesMap

  def apply(pID: Int): Category = Category(pID, items(pID.toString))
  
  def all = items
  
  def alls = dbService.listAllCategories()
  
  def findByName(pCategoryName: String) = dbService.findCategoryByName(pCategoryName)
  
}