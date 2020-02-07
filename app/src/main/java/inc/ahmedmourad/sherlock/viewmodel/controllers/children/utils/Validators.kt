package inc.ahmedmourad.sherlock.viewmodel.controllers.children.utils

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.*
import inc.ahmedmourad.sherlock.domain.model.children.submodel.*
import inc.ahmedmourad.sherlock.model.children.AppPublishedChild
import splitties.init.appCtx

internal fun validateFullName(
        firstName: Name,
        lastName: Name
): Either<String, FullName> {
    return FullName.of(firstName, lastName).mapLeft {
        appCtx.getString(R.string.invalid_first_and_last_name)
    }
}

internal fun validateNameEitherNullable(
        firstName: Name?,
        lastName: Name?
): Either<String, Either<Name, FullName>?> {

    if (firstName == null && lastName != null) {
        return appCtx.getString(R.string.first_name_missing).left()
    }

    return when {
        firstName == null -> null.right()
        lastName == null -> firstName.left().right()
        else -> validateFullName(firstName, lastName).map(FullName::right)
    }
}

internal fun validateName(value: String?): Either<String, Name> {

    if (value == null) {
        return appCtx.getString(R.string.invalid_name).left()
    }

    return Name.of(value).mapLeft {
        when (it) {
            Name.Exception.BlankNameException -> appCtx.getString(R.string.name_empty_or_blank)
            Name.Exception.NameContainsWhiteSpacesException -> appCtx.getString(R.string.name_contains_white_spaces)
            is Name.Exception.NameTooShortException -> appCtx.getString(R.string.name_too_short, it.minLength)
            is Name.Exception.NameTooLongException -> appCtx.getString(R.string.name_too_long, it.maxLength)
            Name.Exception.NameContainsNumbersOrSymbols -> appCtx.getString(R.string.name_contains_numbers_or_symbols)
        }
    }
}

internal fun validateNameNullable(value: String?): Either<String, Name?> {
    return value?.let { validateName(it) } ?: null.right()
}

internal fun validateAgeRange(
        minAge: Age?,
        maxAge: Age?
): Either<String, AgeRange?> {

    if ((minAge == null) != (maxAge == null)) {
        return appCtx.getString(R.string.invalid_age_range).left()
    }

    if (minAge != null && maxAge != null) {
        return AgeRange.of(minAge, maxAge).mapLeft {
            when (it) {
                AgeRange.Exception.MinExceedsMaxException -> appCtx.getString(R.string.age_min_exceeds_max)
            }
        }
    }

    return null.right()
}

internal fun validateAge(value: Int?): Either<String, Age> {

    if (value == null) {
        return appCtx.getString(R.string.invalid_age).left()
    }

    return Age.of(value).mapLeft {
        when (it) {
            is Age.Exception.AgeOutOfRangeException ->
                appCtx.getString(R.string.age_out_of_range, it.min, it.max)
        }
    }
}

internal fun validateAgeNullable(value: Int?): Either<String, Age?> {
    return if (value != null) {
        validateAge(value)
    } else {
        null.right()
    }
}

internal fun validateHeightRange(
        minHeight: Height?,
        maxHeight: Height?
): Either<String, HeightRange?> {

    if ((minHeight == null) != (maxHeight == null)) {
        return appCtx.getString(R.string.invalid_height_range).left()
    }

    if (minHeight != null && maxHeight != null) {
        return HeightRange.of(minHeight, maxHeight).mapLeft {
            when (it) {
                HeightRange.Exception.MinExceedsMaxException -> appCtx.getString(R.string.height_min_exceeds_max)
            }
        }
    }

    return null.right()
}

internal fun validateHeight(value: Int?): Either<String, Height> {

    if (value == null) {
        return appCtx.getString(R.string.invalid_height).left()
    }

    return Height.of(value).mapLeft {
        when (it) {
            is Height.Exception.HeightOutOfRangeException ->
                appCtx.getString(R.string.height_out_of_range, it.min, it.max)
        }
    }
}

internal fun validateHeightNullable(value: Int?): Either<String, Height?> {
    return if (value != null) {
        validateHeight(value)
    } else {
        null.right()
    }
}

internal fun validateGender(gender: Gender?): Either<String, Gender> {
    return gender?.right() ?: appCtx.getString(R.string.gender_missing).left()
}

internal fun validateSkin(skin: Skin?): Either<String, Skin> {
    return skin?.right() ?: appCtx.getString(R.string.skin_color_missing).left()
}

internal fun validateHair(hair: Hair?): Either<String, Hair> {
    return hair?.right() ?: appCtx.getString(R.string.hair_color_missing).left()
}

internal fun validateCoordinates(latitude: Double, longitude: Double): Either<String, Coordinates> {
    return Coordinates.of(latitude, longitude).mapLeft {
        when (it) {
            Coordinates.Exception.InvalidLatitudeException ->
                appCtx.getString(R.string.invalid_latitude)
            Coordinates.Exception.InvalidLongitudeException ->
                appCtx.getString(R.string.invalid_longitude)
            Coordinates.Exception.InvalidCoordinatesException ->
                appCtx.getString(R.string.invalid_coordinates)
        }
    }
}

internal fun validateLocation(location: Location?): Either<String, Location> {
    return location?.right() ?: appCtx.getString(R.string.invalid_last_known_location).left()
}

internal fun validateLocation(
        id: String,
        name: String,
        address: String,
        coordinates: Coordinates
): Either<String, Location> {
    return Location.of(id,
            name.trim(),
            address.trim(),
            coordinates
    ).mapLeft {
        appCtx.getString(R.string.invalid_location)
    }
}

internal fun validateApproximateAppearance(
        ageRange: AgeRange?,
        heightRange: HeightRange?,
        gender: Gender?,
        skin: Skin?,
        hair: Hair?
): Either<String, ApproximateAppearance> {
    return ApproximateAppearance.of(
            gender,
            skin,
            hair,
            ageRange,
            heightRange
    ).mapLeft {
        when (it) {
            ApproximateAppearance.Exception.NotEnoughDetailsException ->
                appCtx.getString(R.string.few_appearance_details)
        }
    }
}

internal fun validateExactAppearance(
        age: Age,
        height: Height,
        gender: Gender,
        skin: Skin,
        hair: Hair
): Either<String, ExactAppearance> {
    return ExactAppearance.of(
            gender,
            skin,
            hair,
            age,
            height
    ).mapLeft {
        appCtx.getString(R.string.incomplete_child_appearance)
    }
}

internal fun validatePicturePath(value: String): Either<String, PicturePath> {
    return PicturePath.of(value).mapLeft {
        when (it) {
            PicturePath.Exception.BlankPathException ->
                appCtx.getString(R.string.blank_picture_path)
            PicturePath.Exception.NonExistentFileException ->
                appCtx.getString(R.string.file_doesnt_exists_at_path)
            PicturePath.Exception.NonFilePathException ->
                appCtx.getString(R.string.path_not_a_file)
            PicturePath.Exception.NonPicturePathException ->
                appCtx.getString(R.string.path_not_a_picture)
            PicturePath.Exception.GifPathException ->
                appCtx.getString(R.string.gif_files_not_supported)
            PicturePath.Exception.UnreadableFileException ->
                appCtx.getString(R.string.unreadable_file)
            PicturePath.Exception.SecurityException ->
                appCtx.getString(R.string.no_permission_to_read_file)
        }
    }
}

internal fun validateAppPublishedChild(
        name: Either<Name, FullName>?,
        notes: String?,
        location: Location?,
        appearance: ApproximateAppearance,
        picturePath: PicturePath?
): Either<String, AppPublishedChild> {
    return AppPublishedChild.of(
            name,
            notes?.trim(),
            location,
            appearance,
            picturePath
    ).mapLeft {
        when (it) {
            AppPublishedChild.Exception.NotEnoughDetailsException ->
                appCtx.getString(R.string.child_not_enough_details)
        }
    }
}

internal fun validateChildQuery(
        fullName: FullName,
        location: Location,
        appearance: ExactAppearance
): Either<String, ChildQuery> {
    return ChildQuery.of(
            fullName,
            location,
            appearance
    ).mapLeft {
        appCtx.getString(R.string.incomplete_child_query)
    }
}
