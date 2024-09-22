import SwiftUICore
import SwiftUI
import Shared

struct TimerTabView: View {

    private let timerStore = TimerStore(coroutineContext: Dispatchers.shared.Main.immediate)

    @StateObject private var state: StateFlowWrapper<TimerStore.State>

    init() {
        // todo: fix types
        let flow = KmmFlowKt.kmmStates(timerStore) as! KmmFlow<TimerStore.State>

        _state = StateObject(wrappedValue: StateFlowWrapper<TimerStore.State>(flow: flow))
    }

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button(
                    action: {
                        timerStore.accept(intent: TimerStoreIntentStartTimer())
                    }
                ) {
                    let isTimerStarted = state.state.value != 0 && !state.state.isTimerRunning

                    Text(isTimerStarted ? "Resume timer" : "Start timer")
                        .padding()
                        .background(state.state.isTimerRunning ? Color.gray : Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                    .disabled(state.state.isTimerRunning)

                Button(
                    action: {
                        timerStore.accept(intent: TimerStoreIntentStopTimer())
                    }
                ) {
                    Text("Stop timer")
                        .padding()
                        .background(!state.state.isTimerRunning ? Color.gray : Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                    .disabled(!state.state.isTimerRunning)

                Button(
                    action: {
                        timerStore.accept(intent: TimerStoreIntentResetTimer())
                    }
                ) {
                    Text("Reset timer")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }

            Text("timer: " + String(state.state.value))
        }
    }
}