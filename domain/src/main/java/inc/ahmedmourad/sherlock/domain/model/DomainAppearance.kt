package inc.ahmedmourad.sherlock.domain.model

import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin


data class DomainAppearance<T>(
        val gender: Gender,
        val skin: Skin,
        val hair: Hair,
        val age: T,
        val height: T
)
