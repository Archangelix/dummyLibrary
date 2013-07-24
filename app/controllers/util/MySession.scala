package controllers.util

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

object MySession {
  private val sessionMap: Map[String, Map[String, Object]] = new HashMap()
  
	def put(userID: String, key: String, value: Object) = {
	  val userMap = 
		  if (!sessionMap.contains(userID)) {
		    sessionMap += (userID -> new HashMap[String, Object]())
		    sessionMap(userID)
		  } else {
		    sessionMap(userID)
		  }
	  
	  userMap +=  (key -> value) 
	}
  
  	def get(userID: String, key: String): Object = {
  	  val userMap = sessionMap(userID)
  	  userMap(key)
  	}
}