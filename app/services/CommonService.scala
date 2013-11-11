package services

import models.OBBook
import models.OBCatalog
import models.OBTxBorrowDT
import models.OBTxBorrowHD
import models.OBUser
import models.common.CatalogListItem
import models.common.CatalogListItem
import models.common.UserListItem
import models.db.DBBook
import models.db.DBCatalog
import models.db.DBTxBorrowDT
import models.db.DBTxBorrowHD
import models.db.DBUser
import models.common.STATUS_BOOK_AVL
import models.exception.BookNotFoundException
import models.exception.BookNotAvailableException

/**
 * This object serves as a bridge between the Business layer and Database layer.
 * Within this service object the user will process all the database request. 
 * All the results returned from this layer will already be in forms of 
 * Business Object instead of Database object.
 */
object CommonService {
  
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getBorrowTransaction(pTransactionSeqNo: Int, pIncludeDetails: Boolean): OBTxBorrowHD = {
	  println("getBorrowTransaction")
	  val dbBorrowTxHeader = DBService.getBorrowTransaction(pTransactionSeqNo)
	  val dbBorrowTxDetails = 
	    if (pIncludeDetails) {
	      DBService.getTransactionDetailsByID(pTransactionSeqNo)
	    } else {
	      List()
	    }
	  val res = OBTxBorrowHD(dbBorrowTxHeader, dbBorrowTxDetails)
	  res
	}
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getTransactionDetailsByID(pTransactionSeqNo: Int): List[OBTxBorrowDT] = {
	  val header = getBorrowTransaction(pTransactionSeqNo, false)
	  val dbList = DBService.getTransactionDetailsByID(pTransactionSeqNo)
	  val res = dbList.map(OBTxBorrowDT(_, header))
	  res
	}

	def startTransaction(pTxBorrowHD: OBTxBorrowHD)(implicit pOfficerUserID: String) = 
	  DBService.insertTxBorrowHD(DBTxBorrowHD(pTxBorrowHD))

	def findPendingTxByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int): OBTxBorrowDT = {
	  val dbDetail = DBService.findPendingTxByBookID(pCatalogSeqNo, pBookSeqNo)
	  val header = getBorrowTransaction(dbDetail.hdSeqNo, false)
	  val res = (OBTxBorrowDT(dbDetail, header))
	  res
	}
	  
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: OBCatalog) = 
	  DBService.findDuplicates(DBCatalog(pCatalog))
	  
	/**
	 * Fetches the list of catalogs for a particular page without the books information.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of catalogs for a particular page without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int): (List[CatalogListItem], Int) = 
	  partialCatalogs(startIdx, endIdx, false)
	
	/**
	 * Fetches the list of catalogs for a particular page with / without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of catalogs for a particular page with / without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int, pWithBooks: Boolean): (List[CatalogListItem], Int) = { 
	  val res = DBService.partialCatalogs(startIdx, endIdx, pWithBooks)
	  res
	}
	
	def searchCatalogs(pStr: String): List[CatalogListItem] = {
	  val res = DBService.searchCatalogs(pStr)
	  res
	}

	/**
	 * Getting the password for a particular ID. The password information is stored 
	 * in a different table for security reasons.
	 * 
	 * In the future the password should be hashed.
	 * 
	 * @param pUserID The User ID.
	 * @return the password stored in the database.
	 */
	def getPassword(pUserID: String): String = {
	  DBService.getPassword(pUserID)
	}
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createUserAndPassword(pUser: OBUser, pPassword: String)(implicit pOfficerUserID: String = "GUEST") = {
	  DBService.createUserAndPassword(DBUser(pUser), pPassword) 
	}
	
  	/**
  	 * Update an existing user.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateUser(pUser: OBUser)(implicit pOfficerUserID: String) = {
	  DBService.updateUser(DBUser(pUser))
	}
	
	def updatePassword(pUserID: String, pPassword: String)(implicit pOfficerUserID: String) = {
	  DBService.updatePassword(pUserID, pPassword)
	}
	
	def softDeleteUser(pSeqNo: Int)(implicit pOfficerUserID: String) = {
	  DBService.softDeleteUser(pSeqNo)
	}
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of users for a particular page.
	 */ 
	def partialUsers(startIdx: Int, endIdx: Int): (List[UserListItem], Int) = {
	  val res = DBService.partialUsers(startIdx, endIdx)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findByUserID(pUserID: String)(implicit pIncludeDeleted: Boolean = false): OBUser = {
	  val dbUser = DBService.findByUserID(pUserID)
	  val res = OBUser(dbUser)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserByIDNumber(pIDNumber: String)(implicit pIncludeDeleted: Boolean = false): OBUser = {
	  val dbUser = DBService.findUserByIDNumber(pIDNumber)
	  val res = OBUser(dbUser)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): OBUser = {
	  val dbUser = DBService.findUserBySeqNo(pSeqNo)
	  val res = OBUser(dbUser)
	  res
	}
	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def createNewCatalog(pCatalog: OBCatalog)(implicit pOfficerUserID: String) = {
	  DBService.insertCatalog(DBCatalog(pCatalog))
	}
	
  	/**
  	 * Update an existing catalog.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def updateCatalog(pCatalog: OBCatalog)(implicit pOfficerUserID: String) = 
	  DBService.updateCatalog(DBCatalog(pCatalog))

	def deleteCatalog(pSeqNo: Int) = DBService.deleteCatalog(pSeqNo)
	
	/**
	 * Fetch a catalog based on the ID. The details may or may not include books information
	 * depending on the parameter. 
	 * 
	 * @param pID The catalog ID to be fetched.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The queried catalog.
	 */
	def findCatalogByID(pCatalogSeqNo: Int)
		(implicit pWithBooks: Boolean = false, 
		    pIncludeDeletedBook: Boolean = false): OBCatalog = {
      val books = 
	      if (pWithBooks) {
	    	  DBService.findAllBooksByCatalogID(pCatalogSeqNo)(pIncludeDeletedBook)
	      } else {
	    	  List()
	      }
	  val dbCatalog = DBService.findCatalogByID(pCatalogSeqNo)(pWithBooks)
	  val res = (OBCatalog(dbCatalog, books))
	  res
	}
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogSeqNo: Int): Int = {
	  DBService.generateNewBookID(pCatalogSeqNo)
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pIncludeDeleted: Boolean = false) = {
	  DBService.findBook(pCatalogSeqNo, pBookSeqNo)
	}
	
	/**
	 * Insert a new book.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def createNewBook(pBook: OBBook)(implicit pOfficerUserID: String) = {
      val newBookSeqNo = CommonService.generateNewBookID(pBook.catalog.seqNo.get)
	  DBService.createBook(DBBook(pBook.copy(seqNo=Some(newBookSeqNo))))
	}
	
	/**
	 * Update a book information.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def updateBook(pBook: OBBook)(implicit pOfficerUserID: String) = {
	  DBService.updateBook(DBBook(pBook))
	}
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String) = {
	  DBService.deleteBook(pCatalogSeqNo, pBookSeqNo)
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findAllBooksByCatalogID(pCatalogSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): List[OBBook] = {
	  val list = DBService.findAllBooksByCatalogID(pCatalogSeqNo)
	  val res = list.map(OBBook(_))
	  res
	}
	
	def updateTxBorrow(pTxBorrow: OBTxBorrowHD, pIncludeDetails: Boolean)(implicit pOfficerUserID: String) = {
	  DBService.updateTxBorrowHD(DBTxBorrowHD(pTxBorrow))
	  if (pIncludeDetails) {
	    DBService.deleteTxBorrowDetails(pTxBorrow.seqno.get)
	    val details = pTxBorrow.details
	    details.foreach(detail => {
	      DBService.insertTxBorrowDT(DBTxBorrowDT(detail))
	    })
	  }
	} 

    def activateBorrowTransaction(transactionSeqNo: Int)(implicit pOfficerUserID: String) = {
      val dbTransaction = OBTxBorrowHD.find(transactionSeqNo, true)
      val transaction = dbTransaction.activate

      // Update transaction header.
	  DBService.updateTxBorrowHD(DBTxBorrowHD(transaction))
	  
	  // Update transaction details.
      DBService.deleteTxBorrowDetails(transaction.seqno.get)
	  val details = transaction.details
	  details.foreach(x => DBService.insertTxBorrowDT(DBTxBorrowDT(x)))
	  
	  // Update books.
	  val books = transaction.details.map(_.book)
	  books.foreach(x => DBService.updateBook(DBBook(x)))
	  
	  transaction
	}
	
	def addNewBook(pTransactionSeqNo: Int, pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String) = {
      val catalog = OBCatalog.find(pCatalogSeqNo)
	  val book = OBBook.find(pCatalogSeqNo, pBookSeqNo)
	  book.status match {
        case STATUS_BOOK_AVL => {
        	val transaction = OBTxBorrowHD.find(pTransactionSeqNo, true)
        			val updatedTx = transaction.addBook(pCatalogSeqNo, pBookSeqNo)
        			updateTxBorrow(updatedTx, true)
        			updatedTx
        }
        case _ => throw BookNotAvailableException(book.id)
      }
	}
	
	def createNewCategory(pCategoryName: String)(implicit pOfficerUserID: String) = {
		DBService.createCategory(pCategoryName, pOfficerUserID)  
	}
	
    
	def updateCategory(pCategorySeqNo: Int, pCategoryName: String)(implicit pOfficerUserID: String) = {
		DBService.updateCategory(pCategorySeqNo, pCategoryName)
	}

	def returnBook(pTransactionSeqNo: Int, pBookID: String)(implicit pOfficerUserID: String) = {
      val arr = pBookID.split('.')
      val catalogSeqNo = arr(0).toInt
      val bookSeqNo = arr(1).toInt
      val transaction = OBTxBorrowHD.find(pTransactionSeqNo, true)
      val updatedTx = transaction.returnBook(catalogSeqNo, bookSeqNo)
      updateTxBorrow(updatedTx, true)
      updatedTx
	}
	
	def now(): java.util.Date = {
	  DBService.now()
	}
}
