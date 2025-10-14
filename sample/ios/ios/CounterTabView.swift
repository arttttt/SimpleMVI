import SwiftUICore
import SwiftUI
import Shared

struct CounterTabView: View {

    private let counterStore = CounterStore(coroutineContext: Dispatchers.shared.Main.immediate).asIosStore()

    @StateObject private var state: StateFlowWrapper<CounterStore.State>

    init() {
        let states = counterStore.states
        
        _state = StateObject(wrappedValue: StateFlowWrapper<CounterStore.State>(flow: states))
        
        let sideEffects = counterStore.sideEffects
        
        sideEffects.subscribe { sideEffect in
            print("received side effect: \(sideEffect)")
        } onCompletion: { error in }

    }

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button(
                    action: {
                        counterStore.accept(intent: CounterStoreIntentIncrement())
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
                        counterStore.accept(intent: CounterStoreIntentDecrement())
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
                        counterStore.accept(intent: CounterStoreIntentReset())
                    }
                ) {
                    Text("Reset")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }

            Text("counter " + String(state.state.counter))
        }
    }
}
