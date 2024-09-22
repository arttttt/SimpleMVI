import Foundation
import Shared

class StateFlowWrapper<T: AnyObject>: ObservableObject {
    @Published var state: T

    private var subscription: KmmSubscription!

    init(flow: KmmFlow<T>) {
        state = flow.value!

        self.subscription = flow.subscribe(
            onEach: { [weak self] state in
                self?.state = state!
            },
            onCompletion: { error in }
        )
    }

    deinit {
        subscription.unsubscribe()
    }
}