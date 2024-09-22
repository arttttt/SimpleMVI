import SwiftUICore
import SwiftUI
import Shared

struct CounterTabView: View {

    private let counterStore = CounterStore(coroutineContext: Dispatchers.shared.Main.immediate)

    @StateObject private var state: StateFlowWrapper<CounterStore.State>

    init() {
        // todo: fix types
        let flow = KmmFlowKt.kmmStates(counterStore) as! KmmFlow<CounterStore.State>

        _state = StateObject(wrappedValue: StateFlowWrapper<CounterStore.State>(flow: flow))
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
