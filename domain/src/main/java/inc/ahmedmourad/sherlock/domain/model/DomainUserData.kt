package inc.ahmedmourad.sherlock.domain.model

data class DomainUserData(
        val id: String,
        val email: String,
        val name: String,
        val phoneNumber: String,
        val picture: ByteArray
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as DomainUserData

        if (id != other.id)
            return false

        if (email != other.email)
            return false

        if (name != other.name)
            return false

        if (phoneNumber != other.phoneNumber)
            return false

        if (!picture.contentEquals(other.picture))
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + phoneNumber.hashCode()
        result = 31 * result + picture.contentHashCode()
        return result
    }
}
