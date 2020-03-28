package inc.ahmedmourad.sherlock.domain.model.auth.submodel

import arrow.core.Either
import arrow.core.left
import arrow.core.right

class Password private constructor(val value: String) {

    fun component1() = value

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Password

        if (value != other.value)
            return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "Password(value='$value')"
    }

    companion object {

        private const val MIN_LENGTH = 7
        private const val MIN_DISTINCT_CHARACTERS = 4

        fun of(value: String): Either<Exception, Password> {
            val chars = value.toCharArray()
            return when {

                value.isBlank() -> Exception.BlankPasswordException.left()

                value.length < MIN_LENGTH -> Exception.PasswordTooShortException(value.length, MIN_LENGTH).left()

                chars.none(Char::isUpperCase) -> Exception.NoCapitalLettersException.left()

                chars.none(Char::isLowerCase) -> Exception.NoSmallLettersException.left()

                chars.none(Char::isDigit) -> Exception.NoDigitsException.left()

                chars.all(Char::isLetterOrDigit) -> Exception.NoSymbolsException.left()

                chars.distinct().size < MIN_DISTINCT_CHARACTERS ->
                    Exception.FewDistinctCharactersException(chars.distinct().size, MIN_DISTINCT_CHARACTERS).left()

                else -> Password(value).right()
            }
        }
    }

    sealed class Exception {
        object BlankPasswordException : Exception()
        data class PasswordTooShortException(val length: Int, val minLength: Int) : Exception()
        object NoCapitalLettersException : Exception()
        object NoSmallLettersException : Exception()
        object NoDigitsException : Exception()
        object NoSymbolsException : Exception()
        data class FewDistinctCharactersException(val count: Int, val min: Int) : Exception()
    }
}
