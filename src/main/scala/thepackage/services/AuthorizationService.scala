package thepackage.services

import java.security.SecureRandom
import java.time.{Clock, Duration}
import java.util.{Base64, UUID}

import com.typesafe.config.Config
import thepackage.models.Token

import scala.concurrent.{ExecutionContext, Future}
import thepackage.db.PostgresProfile.api._
import thepackage.db.PostgresProfile.backend.Database
import thepackage.db.tables.TokenTable
import thepackage.util.now

trait AuthorizationService {
  // returns some non-expired non-revoked token
  def retrieveValidToken(token: String): Future[Option[Token]] // todo with either if token exists but is invalid
  def createToken(userId: UUID): Future[Token] //todo return error if user doesn't exist
}

class DefaultAuthorizationService(db: Database)(config: DefaultAuthorizationServiceConfig)(implicit ex: ExecutionContext, clock: Clock) extends AuthorizationService {
  private val random: ThreadLocal[SecureRandom] = ThreadLocal.withInitial(() => new SecureRandom)

  def retrieveValidToken(token: String) = db.run(
    TokenTable
      .filter(_.deletedAt.isEmpty)
      .filter(_.token === token)
      .filter(_.expiresAt > now)
      .result.headOption
  )

  def createToken(userId: UUID) = {
    val tokenBytes = new Array[Byte](16)
    random.get.nextBytes(tokenBytes)
    val token = Token(
      Base64.getUrlEncoder.withoutPadding.encodeToString(tokenBytes),
      userId,
      now.plus(config.tokenExpiration),
      now
    )
    db.run(TokenTable += token)
      .map(_ => token)
  }
}

case class DefaultAuthorizationServiceConfig(tokenExpiration: Duration)

object DefaultAuthorizationServiceConfig {
  def fromConfig(config: Config) = DefaultAuthorizationServiceConfig(
    config.getDuration("tokenExpiration")
  )
}
