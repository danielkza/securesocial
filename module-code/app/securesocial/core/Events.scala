/**
 * Copyright 2013-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package securesocial.core

import play.api.mvc.{ Controller, Session, RequestHeader }

/**
 * A trait to model SecureSocial events
 */
sealed trait Event[U] { val user: U }

/**
 * The event fired when a users logs in
 * @param user
 */
case class LoginEvent[U](user: U) extends Event[U]

/**
 * The event fired when a user logs out
 * @param user
 */
case class LogoutEvent[U](user: U) extends Event[U]

/**
 * The event fired when a user sings up with the Username and Password Provider
 * @param user
 */
case class SignUpEvent[U](user: U) extends Event[U]

/**
 * The event fired when a user changes his password
 * @param user
 */
case class PasswordChangeEvent[U](user: U) extends Event[U]

/**
 * The event fired when a user completes a password reset
 * @param user
 */
case class PasswordResetEvent[U](user: U) extends Event[U]

/**
 * The event listener interface
 */
abstract class EventListener[U] extends Controller {
  /**
   * The method that gets called when an event occurs.
   *
   * @param event the event type
   * @param request the current request
   * @param session the current session (if you need to manipulate it don't use the one in request.session)
   * @return can return an optional Session object.
   */
  def onEvent(event: Event[U], request: RequestHeader, session: Session): Option[Session]
}

/**
 * Helper object to fire events
 */
object Events {

  def doFire[U](listeners: Seq[EventListener[U]], event: Event[U],
    request: RequestHeader, session: Session): Session =
    {
      listeners.foldLeft(session) { (session, listener) =>
        listener.onEvent(event, request, session).getOrElse(session)
      }
    }

  def fire[U](event: Event[U])(implicit request: RequestHeader, env: RuntimeEnvironment[U]): Option[Session] = {
    val result = doFire(env.eventListeners, event, request, request.session)
    if (result == request.session) None else Some(result)
  }
}
