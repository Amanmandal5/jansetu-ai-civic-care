package com.example.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseSetup {
    // TODO: Replace these with your actual Supabase URL and Anon Key
    const val SUPABASE_URL = "https://kbzomndzgyqeatetanma.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_otWlZZRPXQtXDuPvpGSJjA_3flchg-w"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}
