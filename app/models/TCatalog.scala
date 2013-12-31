package models

import java.util.Date
import models.common.Category

trait TCatalog {
  def seqNo: Option[Int]
  def title: String
  def author: String 
  def publishedYear: Int 
  def category: Category
  def books: List[TBook]
  def isDeleted: Boolean
  def createUserCode: String 
  def createTimestamp: Date
  def auditUserCode: String 
  def auditTimestamp: Date
  def auditReason: Option[String]
}