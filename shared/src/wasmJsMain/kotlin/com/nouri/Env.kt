package com.nouri

// webpack DefinePlugin replaces these identifiers with string literals at bundle time.
// The @JsFun lambdas are evaluated in JS; returning "" is the fallback if the build
// was run without the env vars set (local dev without credentials).

@JsFun("() => (typeof SUPABASE_URL !== 'undefined' ? SUPABASE_URL : '')")
external fun getSupabaseUrl(): String

@JsFun("() => (typeof SUPABASE_ANON_KEY !== 'undefined' ? SUPABASE_ANON_KEY : '')")
external fun getSupabaseAnonKey(): String
