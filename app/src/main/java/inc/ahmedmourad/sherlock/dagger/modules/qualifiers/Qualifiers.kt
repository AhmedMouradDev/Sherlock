package inc.ahmedmourad.sherlock.dagger.modules.qualifiers

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class MainActivityViewModelQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class AddChildViewModelQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class FindChildrenViewModelQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class FindChildrenControllerQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class HomeControllerQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class AddChildControllerIntentQualifier

@Qualifier
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
internal annotation class SherlockServiceIntentQualifier
