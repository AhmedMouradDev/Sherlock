package inc.ahmedmourad.sherlock.viewmodel.controllers.children.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.ahmedmourad.sherlock.dagger.modules.factories.SherlockServiceIntentFactory
import inc.ahmedmourad.sherlock.domain.interactors.core.CheckChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.core.CheckInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.domain.interactors.core.ObserveChildPublishingStateInteractor
import inc.ahmedmourad.sherlock.domain.interactors.core.ObserveInternetConnectivityInteractor
import inc.ahmedmourad.sherlock.viewmodel.controllers.children.AddChildViewModel

internal class AddChildViewModelFactory(
        private val serviceFactory: SherlockServiceIntentFactory,
        private val observeInternetConnectivityInteractor: ObserveInternetConnectivityInteractor,
        private val checkInternetConnectivityInteractor: CheckInternetConnectivityInteractor,
        private val observeChildPublishingStateInteractor: ObserveChildPublishingStateInteractor,
        private val checkChildPublishingStateInteractor: CheckChildPublishingStateInteractor
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = AddChildViewModel(
            serviceFactory,
            observeInternetConnectivityInteractor,
            checkInternetConnectivityInteractor,
            observeChildPublishingStateInteractor,
            checkChildPublishingStateInteractor
    ) as T
}
