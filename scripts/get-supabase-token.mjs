import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://ayqjbwsqralpwjtarenu.supabase.co'
const supabasePublishableKey = process.env.SUPABASE_PUBLISHABLE_KEY
const email = process.env.SUPABASE_EMAIL
const password = process.env.SUPABASE_PASSWORD

if (!supabasePublishableKey || !email || !password) {
  console.error('Missing SUPABASE_PUBLISHABLE_KEY, SUPABASE_EMAIL, or SUPABASE_PASSWORD')
  process.exit(1)
}

const supabase = createClient(supabaseUrl, supabasePublishableKey)

const { data, error } = await supabase.auth.signInWithPassword({
  email,
  password,
})

if (error) {
  console.error('Sign-in failed:', error.message)
  process.exit(1)
}

console.log(data.session?.access_token)
