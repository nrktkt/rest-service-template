package thepackage.services

import com.typesafe.config.Config
import de.mkammerer.argon2.Argon2Factory

trait PasswordService[Hash] {

  def hashPassword(plainTextPassword: Array[Char]): Hash

  /**
    * @param hash
    * @param plainTextPassword
    * @return true if the plaintext password matches the hashed password
    */
  def verifyPassword(hash: Hash, plainTextPassword: Array[Char]): Boolean
}

class Argon2PasswordService(config: Argon2Config) extends PasswordService[String] {
  val argon2 = Argon2Factory.create
  val parallelism = math.max(1, Runtime.getRuntime.availableProcessors - 1)

  def hashPassword(plainTextPassword: Array[Char]) = argon2.hash(
    config.iterations,
    config.memory,
    parallelism,
    plainTextPassword
  )

  def verifyPassword(hash: String, plainTextPassword: Array[Char]) = argon2.verify(hash, plainTextPassword)
}

case class Argon2Config(iterations: Int, memory: Int)
object Argon2Config {
  def fromConfig(config: Config) = Argon2Config(
    config.getInt("iterations"),
    config.getInt("memory")
  )
}
