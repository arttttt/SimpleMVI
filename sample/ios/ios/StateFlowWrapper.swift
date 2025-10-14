import Foundation
import Shared

class StateFlowWrapper<T: AnyObject>: ObservableObject {
    @Published var state: T

    private var subscription: CFlowSubscription!

    init(flow: CStateFlow<T>) {
        state = flow.value

        self.subscription = flow.subscribe { [weak self] state in
            self?.state = state
        } onCompletion: { error in }

    }

    deinit {
        subscription.unsubscribe()
    }
}
