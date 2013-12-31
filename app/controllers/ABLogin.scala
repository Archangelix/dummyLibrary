package controllers

import play.api.mvc.Controller

trait ABLogin { this: Controller with TLogin => 
}

/**
 * Action to handle the logging section.
 */
object ABLogin extends Controller with ABLogin with TLogin {
	// Handled in trait TLogin
}