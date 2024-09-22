import SwiftUI

struct ThirdView: View {
    var body: some View {
        Text("Третий экран")
            .font(.largeTitle)
            .padding()
    }
}

struct ContentView: View {
    var body: some View {
        TabView {
            CounterTabView()
                .tabItem {
                    Image(systemName: "plus.circle")
                    Text("Counter")
                }

            TimerTabView()
                .tabItem {
                    Image(systemName: "timer.circle")
                    Text("Timer")
                }

            NotesTabView()
                .tabItem {
                    Image(systemName: "pencil.circle")
                    Text("Notes")
                }
        }
    }
}
