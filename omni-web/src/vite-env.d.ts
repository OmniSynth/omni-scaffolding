/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_API_BASE: string
  readonly VITE_OMNI_SIGN_SECRET: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
