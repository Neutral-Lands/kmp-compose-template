// Inject Supabase credentials into the Wasm bundle at build time.
// Values are read from the OS environment (process.env) so they are never
// hardcoded — the build fails with empty strings if a variable is missing.
//
// Local dev:  export SUPABASE_URL=... SUPABASE_ANON_KEY=... before running Gradle.
//             Alternatively source a .env.local file (see .env.example).
// CI:         GitHub Actions sets SUPABASE_URL / SUPABASE_ANON_KEY from repository secrets.
config.plugins = (config.plugins || []).concat([
    new (require("webpack").DefinePlugin)({
        SUPABASE_URL: JSON.stringify(process.env.SUPABASE_URL || ""),
        SUPABASE_ANON_KEY: JSON.stringify(process.env.SUPABASE_ANON_KEY || ""),
    }),
]);
