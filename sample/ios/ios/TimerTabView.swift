import SwiftUICore
import SwiftUI
import Shared
import ComposableArchitecture

struct TimerTabView: View {

    private let store: StoreOf<TimerFeature>

    init() {
        store = TimerFeature.from(
            store: TimerStore(coroutineContext: Dispatchers.shared.Main.immediate),
            withDependencies: {
                $0.timerStoreSideEffectHandler = DefaultTimerStoreSideEffectHandler()
            },
        )
    }

    var body: some View {
        WithPerceptionTracking {
            VStack(spacing: 16) {
                HStack {
                    Button(
                        action: {
                            store.send(.startTimer)
                        }
                    ) {
                        let isTimerStarted = store.state.value != 0 && !store.state.isTimerRunning
                        
                        Text(isTimerStarted ? "Resume timer" : "Start timer")
                            .padding()
                            .background(store.state.isTimerRunning ? Color.gray : Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    .disabled(store.state.isTimerRunning)
                    
                    Button(
                        action: {
                            store.send(.stopTimer)
                        }
                    ) {
                        Text("Stop timer")
                            .padding()
                            .background(!store.state.isTimerRunning ? Color.gray : Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                    .disabled(!store.state.isTimerRunning)
                    
                    Button(
                        action: {
                            store.send(.resetTimer)
                        }
                    ) {
                        Text("Reset timer")
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                }
                
                Text("timer: " + String(store.state.value))
            }
            .onAppear { store.send(._bridge(.startObserving)) }
            .onDisappear { store.send(._bridge(.stopObserving)) }
        }
    }
}
