package inc.ahmedmourad.sherlock.auth.manager

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.toT
import dagger.Lazy
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthAuthenticator
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthImageRepository
import inc.ahmedmourad.sherlock.auth.manager.dependencies.AuthRemoteRepository
import inc.ahmedmourad.sherlock.auth.mapper.toAuthCompletedUser
import inc.ahmedmourad.sherlock.auth.mapper.toAuthSignUpUser
import inc.ahmedmourad.sherlock.auth.mapper.toAuthStoredUserDetails
import inc.ahmedmourad.sherlock.auth.model.AuthCompletedUser
import inc.ahmedmourad.sherlock.auth.model.AuthIncompleteUser
import inc.ahmedmourad.sherlock.auth.model.AuthSignedInUser
import inc.ahmedmourad.sherlock.domain.data.AuthManager
import inc.ahmedmourad.sherlock.domain.model.auth.DomainCompletedUser
import inc.ahmedmourad.sherlock.domain.model.auth.DomainIncompleteUser
import inc.ahmedmourad.sherlock.domain.model.auth.DomainSignUpUser
import inc.ahmedmourad.sherlock.domain.model.auth.DomainSignedInUser
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

internal typealias IsUserSignedIn = () -> @JvmSuppressWildcards Single<Boolean>

internal class SherlockAuthManager(
        private val authenticator: Lazy<AuthAuthenticator>,
        private val remoteRepository: Lazy<AuthRemoteRepository>,
        private val imageRepository: Lazy<AuthImageRepository>,
        private val isUserSignedIn: IsUserSignedIn
) : AuthManager {

    override fun isUserSignedIn(): Single<Boolean> {
        return isUserSignedIn.invoke()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun findSignedInUser(): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return authenticator.get()
                .getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { incompleteUserEither ->
                    incompleteUserEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userEither ->
                        userEither.fold(ifLeft = { incompleteUser ->
                            Single.just(incompleteUser.left().right())
                        }, ifRight = { completedUser ->
                            remoteRepository.get()
                                    .findUser(completedUser.id)
                                    .map { either ->
                                        either.map { userOption ->
                                            userOption.fold(ifEmpty = {
                                                completedUser.incomplete().left()
                                            }, ifSome = { retrievedDetails ->
                                                completedUser.toAuthSignedInUser(retrievedDetails).right()
                                            })
                                        }
                                    }
                        })
                    })
                }.flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userEither ->
                        userEither.fold(ifLeft = { incompleteUser ->
                            Single.just(incompleteUser.left().right())
                        }, ifRight = { signedInUser ->
                            remoteRepository.get()
                                    .updateUserLastLoginDate(signedInUser.id)
                                    .map { signedInUser.right().right() }
                        })
                    })
                }.map { either ->
                    either.map { userEither ->
                        userEither.bimap(
                                AuthIncompleteUser::toDomainIncompleteUser,
                                AuthSignedInUser::toDomainSignedInUser
                        )
                    }
                }
    }

    override fun signIn(email: String, password: String): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return authenticator.get()
                .signIn(email, password).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userEither ->
                        userEither.fold(ifLeft = {
                            Single.just(it.left().right())
                        }, ifRight = { completedUser ->
                            remoteRepository.get()
                                    .findUser(completedUser.id)
                                    .map { userEither ->
                                        userEither.map { userOption ->
                                            userOption.fold(ifEmpty = {
                                                completedUser.incomplete().left()
                                            }, ifSome = { retrievedDetails ->
                                                completedUser.toAuthSignedInUser(retrievedDetails).right()
                                            })
                                        }
                                    }
                        })
                    })
                }.map { either ->
                    either.map { userEither ->
                        userEither.bimap(
                                AuthIncompleteUser::toDomainIncompleteUser,
                                AuthSignedInUser::toDomainSignedInUser
                        )
                    }
                }
    }

    override fun signUp(user: DomainSignUpUser): Single<Either<Throwable, DomainSignedInUser>> {
        val picture = user.picture
        val signUpUser = user.toAuthSignUpUser()
        return authenticator.get()
                .signUp(signUpUser.email, signUpUser.password)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { domainUserDetailsEither ->
                    domainUserDetailsEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userId ->
                        remoteRepository.get()
                                .storeUserDetails(signUpUser.toAuthUserDetails(userId))
                    })
                }.flatMap { detailsEither ->
                    detailsEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userDetails ->
                        imageRepository.get()
                                .storeUserPicture(userDetails.id, picture)
                                .map { urlEither ->
                                    urlEither.map { url ->
                                        signUpUser.toAuthCompletedUser(userDetails.id, url) toT userDetails
                                    }
                                }
                    })
                }.flatMap { signedInUserEither ->
                    signedInUserEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { (completedUser, userDetails) ->
                        authenticator.get()
                                .completeUserData(completedUser)
                                .map { either ->
                                    either.map {
                                        it.toAuthSignedInUser(userDetails).toDomainSignedInUser()
                                    }
                                }
                    })
                }
    }

    override fun completeSignUp(completedUser: DomainCompletedUser): Single<Either<Throwable, DomainSignedInUser>> {
        return imageRepository.get()
                .storeUserPicture(completedUser.id, completedUser.picture)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { urlEither ->
                    urlEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { url ->
                        authenticator.get()
                                .completeUserData(completedUser.toAuthCompletedUser(url))
                    })
                }.flatMap { completedUserEither ->
                    completedUserEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { authCompletedUser ->
                        remoteRepository.get()
                                .storeUserDetails(completedUser.toAuthStoredUserDetails())
                                .map { detailsEither ->
                                    detailsEither.map {
                                        authCompletedUser.toAuthSignedInUser(it).toDomainSignedInUser()
                                    }
                                }
                    })
                }
    }

    override fun signInWithGoogle(): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithGoogle)
    }

    override fun signInWithFacebook(): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithFacebook)
    }

    override fun signInWithTwitter(): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return createSignInWithProvider(AuthAuthenticator::signInWithTwitter)
    }

    private fun createSignInWithProvider(
            signInWithProvider: AuthAuthenticator.() -> Single<Either<Throwable, Either<AuthIncompleteUser, AuthCompletedUser>>>
    ): Single<Either<Throwable, Either<DomainIncompleteUser, DomainSignedInUser>>> {
        return authenticator.get()
                .signInWithProvider()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { either ->
                    either.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userEither ->
                        userEither.fold(ifLeft = {
                            Single.just(it.left().right())
                        }, ifRight = { completedUser ->
                            remoteRepository.get()
                                    .findUser(completedUser.id)
                                    .map { userEither ->
                                        userEither.map { userOption ->
                                            userOption.fold(ifEmpty = {
                                                completedUser.incomplete().left()
                                            }, ifSome = { retrievedDetails ->
                                                completedUser.toAuthSignedInUser(retrievedDetails).right()
                                            })
                                        }
                                    }
                        })
                    })
                }.map { either ->
                    either.map { userEither ->
                        userEither.bimap(
                                AuthIncompleteUser::toDomainIncompleteUser,
                                AuthSignedInUser::toDomainSignedInUser
                        )
                    }
                }
    }

    override fun sendPasswordResetEmail(email: String): Single<Either<Throwable, Unit>> {
        return authenticator.get()
                .sendPasswordResetEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }

    override fun signOut(): Single<Either<Throwable, Unit>> {
        return authenticator.get()
                .signOut()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { userUidEither ->
                    userUidEither.fold(ifLeft = {
                        Single.just(it.left())
                    }, ifRight = { userUidOption ->
                        userUidOption.fold(ifEmpty = {
                            Single.just(Unit.right())
                        }, ifSome = {
                            remoteRepository.get()
                                    .updateUserLastLoginDate(it)
                                    .map { Unit.right() }
                        })
                    })
                }
    }
}

internal fun isUserSignedIn(
        authenticator: Lazy<AuthAuthenticator>
): Single<Boolean> {
    return authenticator.get()
            .isUserSignedIn()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
}
