import ComposableArchitecture
import Foundation
import Shared

@Reducer
struct CounterFeature {
    
    @ObservableState
    struct State: Equatable {
        var counter: CounterStoreFeature.State
        var toast: ToastMessage?
        
        struct ToastMessage: Equatable {
            let text: String
            let id: UUID
        }
    }
    
    enum Action {
        case counter(CounterStoreFeature.Action)
        case dismissToast
    }
    
    @Dependency(\.counterStore) var store
    
    var body: some ReducerOf<Self> {
        let childTCA = CounterStoreFeature.from(store: store)
        Scope(state: \.counter, action: \.counter) {
            CounterStoreFeature()
        }
    }
}
