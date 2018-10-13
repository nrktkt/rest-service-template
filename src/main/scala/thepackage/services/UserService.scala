package thepackage.services

import java.time.Clock
import java.util.UUID

import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import thepackage.models.User
import thepackage.payloads.requests.CreateUserRequest
import thepackage.util.Validatable
import cats.data._
import cats.syntax._
import cats.implicits._
import thepackage.errors._
import thepackage.util._
import thepackage.db.PostgresProfile.api._
import thepackage.db.PostgresProfile.backend.Database
import thepackage.db.tables.UserTable

import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  def createUser(request: CreateUserRequest): Future[Either[UserCreationError, User]]
  def retrieveUser(id: UUID): Future[Option[User]]
  def retrieveUserByCredentials(email: String, password: String): Future[Option[User]]
  def updateUserPassword(userId: UUID, newPassword: String): Future[Any]
}

class DefaultUserService(db: Database, passwordService: PasswordService[String])
  (implicit ec: ExecutionContext, clock: Clock)
  extends UserService {

  implicit object CreateUserValidatable extends Validatable[CreateUserRequest, RequestValidationError, User] {
    def validateEmail(email: String) =
      email.split('@') match {
        case parts if parts.length != 2 => FieldValidationError("email", "Exactly one @ symbol allowed").invalidNec
        case Array(_, domain, _*) if !domain.contains('.') =>
          FieldValidationError("email", "No second level domain present").invalidNec
        case _ => email.toLowerCase.validNec
      }

    // todo come up with a way to move password rule validation to password service
    def validatePassword(plainTextPassword: String) =
      if (plainTextPassword.length < 8)
        FieldValidationError("password", "Password must be at least 8 characters").invalidNec
      else plainTextPassword.validNec

    def validateName(name: String) =
      if (name.isEmpty) FieldValidationError("name", "Must not be empty").invalidNec else name.validNec

    def validate(a: CreateUserRequest) =
      (
        UUID.randomUUID.validNec,
        validateEmail(a.email),
        validateName(a.name),
        validatePassword(a.password).map(pw => passwordService.hashPassword(pw.toCharArray)),
        now.validNec
      )
        .mapN(User)
        .leftMap(error => RequestValidationError(error.toList))
  }

  def createUser(request: CreateUserRequest) = EitherT.fromEither[Future](request.validate match {
    case Valid(user)    => Right(user)
    case Invalid(error) => Left(error)
  })
    .flatMapF(user =>
      db.run(UserTable += user)
        .map(_ => Right(user))
    // todo recover constraint violation exception into UserAlreadyExistsError // .recover()
    )
    .value

  def retrieveUser(id: UUID) = db.run(UserTable.filter(_.id === id).result.headOption)

  def retrieveUserByCredentials(email: String, password: String) =
    OptionT(db.run(
      UserTable.filter(_.email.toLowerCase === email.toLowerCase).result.headOption
    ))
      .filter(user => passwordService.verifyPassword(user.password, password.toCharArray))
      .value

  def updateUserPassword(userId: UUID, newPassword: String) = db.run(
    UserTable
      .filter(_.id === userId)
      .map(_.password)
      .update(passwordService.hashPassword(newPassword.toCharArray))
  )
}
