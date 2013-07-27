package controllers

import play.api.mvc.RequestHeader
import play.api.mvc.Security
import play.api.mvc.Results
import play.api.mvc.Action
import play.api.mvc.Result
import play.api.mvc.Request
import play.api.mvc.AnyContent


trait TSecured {
	def username(request: RequestHeader) = {
	  val s = request.session.get(Security.username)
	  println("Entering function username = "+s)
	  s
	}
	
	def onAuthorized(request: RequestHeader) = {
	  println("User not authorized")
	  Results.Redirect(routes.LoginAction.loginPage)
	}
	
	def withAuth(f: => String => Request[AnyContent] => Result) = {
	  Security.Authenticated(username, onAuthorized) {user =>
	    println("Entering withAuth")
	    Action(request => f(user)(request))
	  }
	}
}