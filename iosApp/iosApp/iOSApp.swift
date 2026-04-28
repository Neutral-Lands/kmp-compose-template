import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        let url = Bundle.main.infoDictionary?["SUPABASE_URL"] as? String ?? ""
        let key = Bundle.main.infoDictionary?["SUPABASE_ANON_KEY"] as? String ?? ""
        KoinInitializerKt.initKoin(supabaseUrl: url, supabaseAnonKey: key)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
