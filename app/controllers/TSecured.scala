package controllers

import play.api.mvc.RequestHeader
import play.api.mvc.Security
import play.api.mvc.Results
import play.api.mvc.Action
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.AnyContent

/**
 * The authentication template to be used in all pages that need authentication.
 */
trait TSecured {
	def username(request: RequestHeader) = {
	  val username = request.session.get(Security.username)
	  username
	}
	
	/**
	 * Redirect the user to the login page if the user is not yet authenticated.
	 * @param request The request header.
	 */
	def onAuthorized(request: RequestHeader) = {
	  println("User not authorized")
	  Results.Redirect(routes.ABLogin.loginPage)
	}
	
	/**
	 * The template used to activate the authentication.
	 * 
	 * @param f The param object used in every method called from the request.
	 */
	def withAuth(f: => String => Request[AnyContent] => Result) = {
	  Security.Authenticated(username, onAuthorized) {user =>
	    println("Entering withAuth")
	    Action(request => f(user)(request))
	  }
	}
}