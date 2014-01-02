package services

import models.TTxBorrowDT
import models.TTxBorrowHD
import models.common.STATUS_BOOK_AVL
import models.common.CatalogListItem
import models.TUser
import models.TBook
import models.common.UserListItem
import models.exception.BookNotAvailableException
import models.TCatalog
import models.db.TDBBook
import models.TTxBorrowHD
import models.TCatalog
import models.TBook
import models.TTxBorrowHD
import models.TCatalog
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
import models.db.TDBCatalog
import models.db.TDBBook
import models.TTxBorrowDT
import models.TTxBorrowHD
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.TTxBorrowDT
import models.TTxBorrowHD
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.db.TDBBook
import models.TTxBorrowDT
import models.TTxBorrowHD
import models.db.TDBTxBorrowHD
import models.TUser
import models.TBook
import models.db.TDBUser
import models.db.TDBTxBorrowDT
import models.TCatalog
import models.db.TDBCatalog
import models.OBUser
import models.db.DBUser
import models.db.DBCatalog
import models.db.DBBook
import models.db.DBTxBorrowDT
import models.OBBook
import models.OBTxBorrowDT
import models.db.DBTxBorrowHD
import models.OBTxBorrowHD
import models.OBCatalog
import utils.CommonUtil._

trait TCommonService {
  val logger = generateLogger(this)
  
  val dbService = PSQLService
  val objTxBorrowHD = OBTxBorrowHD
  val objTxBorrowDT = OBTxBorrowDT
  val objDBTxBorrowHD = DBTxBorrowHD
  val objDBTxBorrowDT = DBTxBorrowDT
  val objDBCatalog = DBCatalog
  val objDBUser = DBUser
  val objDBBook = DBBook
  val objUser = OBUser
  val objCatalog = OBCatalog
  val objBook = OBBook
  
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getBorrowTransaction(pTransactionSeqNo: Int, pIncludeDetails: Boolean): TTxBorrowHD
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getTransactionDetailsByID(pTransactionSeqNo: Int): List[TTxBorrowDT]

	def startTransaction(pTxBorrowHD: TTxBorrowHD)(implicit pOfficerUserID: String): Int

	def findPendingTxByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int): TTxBorrowDT
	  
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: TCatalog): Option[List[TDBCatalog]]
	  
	/**
	 * Fetches the list of catalogs for a particular page without the books information.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of catalogs for a particular page without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int): (List[CatalogListItem], Int)
	
	/**
	 * Fetches the list of catalogs for a particular page with / without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of catalogs for a particular page with / without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int, pWithBooks: Boolean): (List[CatalogListItem], Int)
	
	def searchCatalogs(pStr: String): List[CatalogListItem]

	/**
	 * Getting the password for a particular ID. The password information is stored 
	 * in a different table for security reasons.
	 * 
	 * In the future the password should be hashed.
	 * 
	 * @param pUserID The User ID.
	 * @return the password stored in the database.
	 */
	def getPassword(pUserID: String): String
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createUserAndPassword(pUser: TUser, pPassword: String)(implicit pOfficerUserID: String = "GUEST"): Unit
	
  	/**
  	 * Update an existing user.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateUser(pUser: TUser)(implicit pOfficerUserID: String): Unit
	
	def updatePassword(pUserID: String, pPassword: String)(implicit pOfficerUserID: String): Unit
	
	def softDeleteUser(pSeqNo: Int)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of users for a particular page.
	 */ 
	def partialUsers(startIdx: Int, endIdx: Int): (List[UserListItem], Int)
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findByUserID(pUserID: String)(implicit pIncludeDeleted: Boolean = false): TUser
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserByIDNumber(pIDNumber: String)(implicit pIncludeDeleted: Boolean = false): TUser
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TUser
	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def createNewCatalog(pCatalog: TCatalog)(implicit pOfficerUserID: String)
	
  	/**
  	 * Update an existing catalog.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def updateCatalog(pCatalog: TCatalog)(implicit pOfficerUserID: String): Unit

	def deleteCatalog(pSeqNo: Int): Unit
	
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
		    pIncludeDeletedBook: Boolean = false): TCatalog
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogSeqNo: Int): Int
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TDBBook
	
	/**
	 * Insert a new book.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def createNewBook(pBook: TBook)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Update a book information.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def updateBook(pBook: TBook)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findAllBooksByCatalogID(pCatalogSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): List[TBook]
	
	def updateTxBorrow(pTxBorrow: TTxBorrowHD, pIncludeDetails: Boolean)
		(implicit pOfficerUserID: String): Unit

    def activateBorrowTransaction(transactionSeqNo: Int)
		(implicit pOfficerUserID: String): TTxBorrowHD
	
	def addNewBook(pTransactionSeqNo: Int, pCatalogSeqNo: Int, pBookSeqNo: Int)
		(implicit pOfficerUserID: String): TTxBorrowHD 
	
	def createNewCategory(pCategoryName: String)
		(implicit pOfficerUserID: String): Unit
	
    
	def updateCategory(pCategorySeqNo: Int, pCategoryName: String)
		(implicit pOfficerUserID: String): Unit

	def returnBook(pTransactionSeqNo: Int, pBookID: String)
		(implicit pOfficerUserID: String): TTxBorrowHD
	
	def now(): java.util.Date
}