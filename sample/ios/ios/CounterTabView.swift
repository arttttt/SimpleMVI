import SwiftUICore
import SwiftUI
import Shared
import ComposableArchitecture

struct CounterTabView: View {
    
    let store: StoreOf<CounterFeature>

    init() {
        store = Store(initialState: CounterFeature.State(counter: 0)) {
            CounterFeature()
        } withDependencies: {
            $0.counterStore = CounterStore(coroutineContext: Dispatchers.shared.Main.immediate)
            $0.counterStoreSideEffectHandler = DefaultCounterStoreSideEffectHandler()
        }

    }

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button(
                    action: {
                        store.send(.increment)
                    }
                ) {
                    Text("Increment")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }

                Button(
                    action: {
                        store.send(.decrement)
                    }
                ) {
                    Text("Decrement")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }

                Button(
                    action: {
                        store.send(.reset)
                    }
                ) {
                    Text("Reset")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }

            WithPerceptionTracking {
                Text("counter \(store.counter)")
            }
        }
        .onAppear { store.send(._bridge(.startObserving)) }
        .onDisappear { store.send(._bridge(.stopObserving)) }
    }
}
