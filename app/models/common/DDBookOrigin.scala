package models.common

case class DDBookOrigin(code: String, desc: String)

object DDBookOrigin{
  private lazy val items = Map(
      "new" -> "New",
      "old" -> "Old"
  )
  
  def apply(pCode: String): DDBookOrigin = {
    DDBookOrigin(pCode, items(pCode))
  }

  def all = items.toList.sortBy(_._2)
}