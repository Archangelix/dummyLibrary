package services

import models.TBook
import models.TCatalog
import models.TTxBorrowDT
import models.TTxBorrowHD
import models.TUser
import models.common.CatalogListItem
import models.common.CatalogListItem
import models.common.UserListItem
import models.db.TDBBook
import models.db.TDBCatalog
import models.db.TDBTxBorrowDT
import models.db.TDBTxBorrowHD
import models.db.TDBUser
import models.common.STATUS_BOOK_AVL
import models.exception.BookNotFoundException
import models.exception.BookNotAvailableException
import models.TBook
import models.db.TDBBook
import models.TTxBorrowDT
import models.TTxBorrowHD
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.OBTxBorrowHD
import models.db.TDBCatalog
import models.db.TDBBook
import models.TTxBorrowDT
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.OBTxBorrowDT
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.DBTxBorrowHD
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.db.TDBTxBorrowHD
import models.TUser
import models.db.DBTxBorrowDT
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.db.DBCatalog
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.db.DBUser
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.OBUser
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.db.DBBook
import models.db.TDBTxBorrowHD
import models.TBook
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBBook
import models.db.TDBTxBorrowHD
import models.TBook
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.OBCatalog
import models.db.TDBBook
import models.db.TDBTxBorrowHD
import models.TBook
import models.OBBook
import models.db.TDBTxBorrowDT
import models.TCatalog

/**
 * This object serves as a bridge between the Business layer and Database layer.
 * Within this service object the user will process all the database request. 
 * All the results returned from this layer will already be in forms of 
 * Business Object instead of Database object.
 */
object CommonService extends TCommonService {
  override val dbService = PSQLService
  override val objTxBorrowHD = OBTxBorrowHD
  override val objTxBorrowDT = OBTxBorrowDT
  override val objDBTxBorrowHD = DBTxBorrowHD
  override val objDBTxBorrowDT = DBTxBorrowDT
  override val objDBCatalog = DBCatalog
  override val objDBUser = DBUser
  override val objDBBook = DBBook
  override val objUser = OBUser
  override val objCatalog = OBCatalog
  override val objBook = OBBook
  
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getBorrowTransaction(pTransactionSeqNo: Int, pIncludeDetails: Boolean): TTxBorrowHD = {
	  println("getBorrowTransaction")
	  val dbBorrowTxHeader = dbService.getBorrowTransaction(pTransactionSeqNo)
	  val dbBorrowTxDetails = 
	    if (pIncludeDetails) {
	      dbService.getTransactionDetailsByID(pTransactionSeqNo)
	    } else {
	      List()
	    }
	  val res = objTxBorrowHD(dbBorrowTxHeader, dbBorrowTxDetails)
	  res
	}
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getTransactionDetailsByID(pTransactionSeqNo: Int): List[TTxBorrowDT] = {
	  val header = getBorrowTransaction(pTransactionSeqNo, false)
	  val dbList = dbService.getTransactionDetailsByID(pTransactionSeqNo)
	  val res = dbList.map(objTxBorrowDT(_, header))
	  res
	}

	def startTransaction(pTxBorrowHD: TTxBorrowHD)(implicit pOfficerUserID: String) = 
	  dbService.insertTxBorrowHD(objDBTxBorrowHD(pTxBorrowHD))

	def findPendingTxByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int): TTxBorrowDT = {
	  val dbDetail = dbService.findPendingTxByBookID(pCatalogSeqNo, pBookSeqNo)
	  val header = getBorrowTransaction(dbDetail.hdSeqNo, false)
	  val res = (objTxBorrowDT(dbDetail, header))
	  res
	}
	  
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: TCatalog): Option[List[TDBCatalog]] = 
	  dbService.findDuplicates(objDBCatalog(pCatalog))
	  
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
	  val res = dbService.partialCatalogs(startIdx, endIdx, pWithBooks)
	  res
	}
	
	def searchCatalogs(pStr: String): List[CatalogListItem] = {
	  val res = dbService.searchCatalogs(pStr)
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
	  dbService.getPassword(pUserID)
	}
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createUserAndPassword(pUser: TUser, pPassword: String)(implicit pOfficerUserID: String = "GUEST") = {
	  dbService.createUserAndPassword(objDBUser(pUser), pPassword) 
	}
	
  	/**
  	 * Update an existing user.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateUser(pUser: TUser)(implicit pOfficerUserID: String) = {
	  dbService.updateUser(objDBUser(pUser))
	}
	
	def updatePassword(pUserID: String, pPassword: String)(implicit pOfficerUserID: String) = {
	  dbService.updatePassword(pUserID, pPassword)
	}
	
	def softDeleteUser(pSeqNo: Int)(implicit pOfficerUserID: String) = {
	  dbService.softDeleteUser(pSeqNo)
	}
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of users for a particular page.
	 */ 
	def partialUsers(startIdx: Int, endIdx: Int): (List[UserListItem], Int) = {
	  val res = dbService.partialUsers(startIdx, endIdx)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findByUserID(pUserID: String)(implicit pIncludeDeleted: Boolean = false): TUser = {
	  val dbUser = dbService.findByUserID(pUserID)
	  val res = objUser(dbUser)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserByIDNumber(pIDNumber: String)(implicit pIncludeDeleted: Boolean = false): TUser = {
	  val dbUser = dbService.findUserByIDNumber(pIDNumber)
	  val res = objUser(dbUser)
	  res
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TUser = {
	  val dbUser = dbService.findUserBySeqNo(pSeqNo)
	  val res = objUser(dbUser)
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
	def createNewCatalog(pCatalog: TCatalog)(implicit pOfficerUserID: String) = {
	  dbService.insertCatalog(objDBCatalog(pCatalog))
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
	def updateCatalog(pCatalog: TCatalog)(implicit pOfficerUserID: String) = 
	  dbService.updateCatalog(objDBCatalog(pCatalog))

	def deleteCatalog(pSeqNo: Int) = dbService.deleteCatalog(pSeqNo)
	
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
		    pIncludeDeletedBook: Boolean = false): TCatalog = {
      val books = 
	      if (pWithBooks) {
	    	  dbService.findAllBooksByCatalogID(pCatalogSeqNo)(pIncludeDeletedBook)
	      } else {
	    	  List()
	      }
	  val dbCatalog = dbService.findCatalogByID(pCatalogSeqNo)(pWithBooks)
	  val res = (objCatalog(dbCatalog, books))
	  res
	}
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogSeqNo: Int): Int = {
	  dbService.generateNewBookID(pCatalogSeqNo)
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pIncludeDeleted: Boolean = false) = {
	  dbService.findBook(pCatalogSeqNo, pBookSeqNo)
	}
	
	/**
	 * Insert a new book.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def createNewBook(pBook: TBook)(implicit pOfficerUserID: String) = {
      val newBookSeqNo = CommonService.generateNewBookID(pBook.catalog.seqNo.get)
	  dbService.createBook(objDBBook(pBook.artificialCopy(Some(newBookSeqNo))))
	}
	
	/**
	 * Update a book information.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def updateBook(pBook: TBook)(implicit pOfficerUserID: String) = {
	  dbService.updateBook(objDBBook(pBook))
	}
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String) = {
	  dbService.deleteBook(pCatalogSeqNo, pBookSeqNo)
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findAllBooksByCatalogID(pCatalogSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): List[TBook] = {
	  val list = dbService.findAllBooksByCatalogID(pCatalogSeqNo)
	  val res = list.map(objBook(_))
	  res
	}
	
	def updateTxBorrow(pTxBorrow: TTxBorrowHD, pIncludeDetails: Boolean)(implicit pOfficerUserID: String) = {
	  dbService.updateTxBorrowHD(objDBTxBorrowHD(pTxBorrow))
	  if (pIncludeDetails) {
	    dbService.deleteTxBorrowDetails(pTxBorrow.seqno.get)
	    val details = pTxBorrow.details
	    details.foreach(detail => {
	      dbService.insertTxBorrowDT(objDBTxBorrowDT(detail))
	    })
	  }
	} 

    def activateBorrowTransaction(transactionSeqNo: Int)(implicit pOfficerUserID: String) = {
      val dbTransaction = objTxBorrowHD.find(transactionSeqNo, true)
      val transaction = dbTransaction.activate

      // Update transaction header.
	  dbService.updateTxBorrowHD(objDBTxBorrowHD(transaction))
	  
	  // Update transaction details.
      dbService.deleteTxBorrowDetails(transaction.seqno.get)
	  val details = transaction.details
	  details.foreach(x => dbService.insertTxBorrowDT(objDBTxBorrowDT(x)))
	  
	  // Update books.
	  val books = transaction.details.map(_.book)
	  books.foreach(x => dbService.updateBook(objDBBook(x)))
	  
	  transaction
	}
	
	def addNewBook(pTransactionSeqNo: Int, pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String) = {
      val catalog = objCatalog.find(pCatalogSeqNo)
	  val book = objBook.find(pCatalogSeqNo, pBookSeqNo)
	  book.status match {
        case STATUS_BOOK_AVL => {
        	val transaction = objTxBorrowHD.find(pTransactionSeqNo, true)
        			val updatedTx = transaction.addBook(pCatalogSeqNo, pBookSeqNo)
        			updateTxBorrow(updatedTx, true)
        			updatedTx
        }
        case _ => throw BookNotAvailableException(book.id)
      }
	}
	
	def createNewCategory(pCategoryName: String)(implicit pOfficerUserID: String) = {
		dbService.createCategory(pCategoryName, pOfficerUserID)  
	}
	
    
	def updateCategory(pCategorySeqNo: Int, pCategoryName: String)(implicit pOfficerUserID: String) = {
		dbService.updateCategory(pCategorySeqNo, pCategoryName)
	}

	def returnBook(pTransactionSeqNo: Int, pBookID: String)(implicit pOfficerUserID: String) = {
	  println("returnBook")
      val arr = pBookID.split('.')
      val catalogSeqNo = arr(0).toInt
      val bookSeqNo = arr(1).toInt
      val transaction = objTxBorrowHD.find(pTransactionSeqNo, true)
      val updatedTx = transaction.returnBook(catalogSeqNo, bookSeqNo)
      updateTxBorrow(updatedTx, true)
      
      val books = updatedTx.details.map(_.book)
      books.foreach(updateBook(_))
      updatedTx
	}
	
	def now(): java.util.Date = {
	  dbService.now()
	}
}
