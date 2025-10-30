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
    
    private enum ToastTimerID { case timer }
    
    var body: some ReducerOf<Self> {
        Scope(state: \.counter, action: \.counter) {
            CounterStoreFeature()
        }
        
        Reduce { state, action in
            switch action {
            case let .counter(._sideEffect(sideEffect)):
                return handleSideEffect(sideEffect, state: &state)
            case .counter:
                return .none
            case .dismissToast:
                state.toast = nil
                return .none
            }
            
        }
    }
    
    private func handleSideEffect(
            _ sideEffect: CounterStoreSideEffect,
            state: inout State
        ) -> Effect<Action> {
            let message: String
            
            switch sideEffect {
            case let effect as CounterStoreSideEffectCounterChanged:
                message = "Counter changed to \(effect.counter)"
            case is CounterStoreSideEffectCantResetCounter:
                message = "Can't reset counter"
            case is CounterStoreSideEffectCounterReset:
                message = "Counter reset"
            default:
                return .none
            }
            
            state.toast = State.ToastMessage(text: message, id: UUID())
            
            return .run { send in
                try await Task.sleep(for: .seconds(2))
                await send(.dismissToast)
            }
            .cancellable(id: ToastTimerID.timer, cancelInFlight: true)
        }
}
