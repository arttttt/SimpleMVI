import SwiftUICore
import SwiftUI
import Shared
import ComposableArchitecture

struct CounterTabView: View {
    
    let store: StoreOf<CounterFeature>

    init() {
        store = CounterFeature.from(
            store: CounterStore(coroutineContext: Dispatchers.shared.Main.immediate),
            withDependencies: { deps in
                deps.counterStoreSideEffectHandler = DefaultCounterStoreSideEffectHandler()
            },
        )

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

            Text("counter \(store.counter)")
        }
    }
}
