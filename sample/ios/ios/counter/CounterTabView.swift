import SwiftUICore
import SwiftUI
import Shared
import ComposableArchitecture

struct CounterTabView: View {
    
    let store: StoreOf<CounterFeature>

    init() {
        let kmpStore = CounterStore(
            coroutineContext: Dispatchers.shared.Main.immediate,
        )
        
        let store = Store(
            initialState: CounterFeature.State(
                counter: CounterStoreFeature.State.from(state: kmpStore.state),
                toast: nil,
            ),
        ) {
            CounterFeature()
        } withDependencies: { deps in
            deps.counterStore = kmpStore
        }
        
        kmpStore.bindLifecycle(
            send: { action in
                store.send(.counter(action))
            },
        )
        
        self.store = store
    }

    var body: some View {
        ZStack {
            VStack(spacing: 16) {
                HStack {
                    Button(
                        action: {
                            store.send(.counter(.increment))
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
                            store.send(.counter(.decrement))
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
                            store.send(.counter(.reset))
                        }
                    ) {
                        Text("Reset")
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                }

                Text("counter \(store.counter.counter)")
            }
            
            if let toast = store.toast {
                ToastView(message: toast.text)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, 16)
                    .transition(.asymmetric(
                        insertion: .opacity,
                        removal: .opacity,
                    ))
                    .id(toast.id)
            }
        }
        .animation(.spring(), value: store.toast)
    }
}
