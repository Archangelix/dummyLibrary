package models.common

/**
 * Borrow Transaction Header status.
 */
sealed abstract class BookStatus {
  val code: String
  val description: String
}

object BookStatus {
  private val items = Map(
	STATUS_BOOK_AVL.code -> STATUS_BOOK_AVL,
	STATUS_BOOK_PEN.code -> STATUS_BOOK_PEN,
	STATUS_BOOK_LST.code -> STATUS_BOOK_LST,
	STATUS_BOOK_BRK.code -> STATUS_BOOK_BRK
  )
  
  def apply(pCode: String): BookStatus = items(pCode)
}

case object STATUS_BOOK_AVL extends BookStatus {
  val code = "AVL"
  val description = "Available"
}

case object STATUS_BOOK_PEN extends BookStatus {
  val code = "PEN"
  val description = "Pending"
}

case object STATUS_BOOK_LST extends BookStatus {
  val code = "LST"
  val description = "Lost"
}

case object STATUS_BOOK_BRK extends BookStatus {
	val code = "BRK"
	val description = "Broken"
}

/**
 * Borrow Transaction Header status.
 */
sealed abstract class BorrowHDStatus {
  val code: String
  val description: String
}

object BorrowHDStatus {
  private val items = Map(
	STATUS_BORROW_HD_DFT.code -> STATUS_BORROW_HD_DFT,
	STATUS_BORROW_HD_PEN.code -> STATUS_BORROW_HD_PEN,
	STATUS_BORROW_HD_COM.code -> STATUS_BORROW_HD_COM
  )
  
  def apply(pCode: String): BorrowHDStatus = items(pCode)
}

case object STATUS_BORROW_HD_DFT extends BorrowHDStatus {
  val code = "DFT"
  val description = "Draft"
}

case object STATUS_BORROW_HD_PEN extends BorrowHDStatus {
  val code = "PEN"
  val description = "Pending"
}

case object STATUS_BORROW_HD_COM extends BorrowHDStatus {
  val code = "COM"
  val description = "Completed"
}

/**
 * Borrow Transaction Detail status.
 */
sealed abstract class BorrowDTStatus {
  val code: String
  val description: String
}

object BorrowDTStatus {
  private val items = Map(
	STATUS_BORROW_DT_DFT.code -> STATUS_BORROW_DT_DFT,
	STATUS_BORROW_DT_PEN.code -> STATUS_BORROW_DT_PEN,
	STATUS_BORROW_DT_RET.code -> STATUS_BORROW_DT_RET,
	STATUS_BORROW_DT_LST.code -> STATUS_BORROW_DT_LST,
	STATUS_BORROW_DT_BRK.code -> STATUS_BORROW_DT_BRK
  )
  
  def apply(pCode: String): BorrowDTStatus = items(pCode)
}

case object STATUS_BORROW_DT_DFT extends BorrowDTStatus {
  val code = "DFT"
  val description = "Draft"
}

case object STATUS_BORROW_DT_PEN extends BorrowDTStatus {
  val code = "PEN"
  val description = "Pending"
}

case object STATUS_BORROW_DT_RET extends BorrowDTStatus {
  val code = "RET"
  val description = "Returned"
}

case object STATUS_BORROW_DT_LST extends BorrowDTStatus {
  val code = "LST"
  val description = "Lost"
}

case object STATUS_BORROW_DT_BRK extends BorrowDTStatus {
  val code = "BRK"
  val description = "Broken"
}
