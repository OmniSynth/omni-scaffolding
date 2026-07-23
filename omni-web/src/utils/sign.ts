import { LoginSignHeaders } from '@/utils/signHeaders'

/** 与后端 omni.security.sign.secret / OMNI_SIGN_SECRET 保持一致 */
const SIGN_SECRET = import.meta.env.VITE_OMNI_SIGN_SECRET || ''

function toHex(bytes: ArrayBuffer | Uint8Array): string {
  const arr = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes)
  return [...arr].map((b) => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 局域网 HTTP（非 localhost）不是 Secure Context，randomUUID 不可用。
 */
function randomNonce(): string {
  const c = globalThis.crypto
  if (c && typeof c.randomUUID === 'function') {
    return c.randomUUID().replace(/-/g, '')
  }
  const bytes = new Uint8Array(16)
  if (!c?.getRandomValues) {
    throw new Error('当前环境不支持 Web Crypto，无法生成登录 nonce')
  }
  c.getRandomValues(bytes)
  return toHex(bytes)
}

async function hmacSha256HexNative(secret: string, payload: string): Promise<string> {
  const enc = new TextEncoder()
  const key = await crypto.subtle.importKey(
    'raw',
    enc.encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign'],
  )
  const sig = await crypto.subtle.sign('HMAC', key, enc.encode(payload))
  return toHex(sig)
}

/** 非 Secure Context 下 crypto.subtle 不可用时的 HMAC-SHA256 回退 */
function hmacSha256HexFallback(secret: string, payload: string): string {
  const enc = new TextEncoder()
  return toHex(hmacSha256(enc.encode(secret), enc.encode(payload)))
}

async function hmacSha256Hex(secret: string, payload: string): Promise<string> {
  if (globalThis.crypto?.subtle) {
    return hmacSha256HexNative(secret, payload)
  }
  return hmacSha256HexFallback(secret, payload)
}

// ---- 精简 SHA-256 / HMAC（仅作 HTTP 局域网开发回退）----

function rotr(n: number, x: number): number {
  return (x >>> n) | (x << (32 - n))
}

function sha256(message: Uint8Array): Uint8Array {
  const K = [
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
  ]
  let h0 = 0x6a09e667
  let h1 = 0xbb67ae85
  let h2 = 0x3c6ef372
  let h3 = 0xa54ff53a
  let h4 = 0x510e527f
  let h5 = 0x9b05688c
  let h6 = 0x1f83d9ab
  let h7 = 0x5be0cd19

  const bitLen = message.length * 8
  const withOne = new Uint8Array(((message.length + 9 + 63) >> 6) << 6)
  withOne.set(message)
  withOne[message.length] = 0x80
  const view = new DataView(withOne.buffer)
  view.setUint32(withOne.length - 4, bitLen >>> 0)
  // 长度高 32 位对短消息为 0，已是默认值

  const w = new Uint32Array(64)
  for (let i = 0; i < withOne.length; i += 64) {
    for (let j = 0; j < 16; j++) {
      w[j] = view.getUint32(i + j * 4)
    }
    for (let j = 16; j < 64; j++) {
      const s0 = rotr(7, w[j - 15]) ^ rotr(18, w[j - 15]) ^ (w[j - 15] >>> 3)
      const s1 = rotr(17, w[j - 2]) ^ rotr(19, w[j - 2]) ^ (w[j - 2] >>> 10)
      w[j] = (w[j - 16] + s0 + w[j - 7] + s1) >>> 0
    }
    let a = h0
    let b = h1
    let c = h2
    let d = h3
    let e = h4
    let f = h5
    let g = h6
    let h = h7
    for (let j = 0; j < 64; j++) {
      const S1 = rotr(6, e) ^ rotr(11, e) ^ rotr(25, e)
      const ch = (e & f) ^ (~e & g)
      const t1 = (h + S1 + ch + K[j] + w[j]) >>> 0
      const S0 = rotr(2, a) ^ rotr(13, a) ^ rotr(22, a)
      const maj = (a & b) ^ (a & c) ^ (b & c)
      const t2 = (S0 + maj) >>> 0
      h = g
      g = f
      f = e
      e = (d + t1) >>> 0
      d = c
      c = b
      b = a
      a = (t1 + t2) >>> 0
    }
    h0 = (h0 + a) >>> 0
    h1 = (h1 + b) >>> 0
    h2 = (h2 + c) >>> 0
    h3 = (h3 + d) >>> 0
    h4 = (h4 + e) >>> 0
    h5 = (h5 + f) >>> 0
    h6 = (h6 + g) >>> 0
    h7 = (h7 + h) >>> 0
  }

  const out = new Uint8Array(32)
  const outView = new DataView(out.buffer)
  outView.setUint32(0, h0)
  outView.setUint32(4, h1)
  outView.setUint32(8, h2)
  outView.setUint32(12, h3)
  outView.setUint32(16, h4)
  outView.setUint32(20, h5)
  outView.setUint32(24, h6)
  outView.setUint32(28, h7)
  return out
}

function hmacSha256(key: Uint8Array, data: Uint8Array): Uint8Array {
  const block = 64
  let k = key
  if (k.length > block) {
    k = sha256(k)
  }
  if (k.length < block) {
    const padded = new Uint8Array(block)
    padded.set(k)
    k = padded
  }
  const oKey = new Uint8Array(block)
  const iKey = new Uint8Array(block)
  for (let i = 0; i < block; i++) {
    oKey[i] = k[i] ^ 0x5c
    iKey[i] = k[i] ^ 0x36
  }
  const inner = new Uint8Array(block + data.length)
  inner.set(iKey)
  inner.set(data, block)
  const innerHash = sha256(inner)
  const outer = new Uint8Array(block + innerHash.length)
  outer.set(oKey)
  outer.set(innerHash, block)
  return sha256(outer)
}

/**
 * 构造登录加签头。未配置密钥时返回空对象（后端关闭加签时可联调）。
 */
export async function buildLoginSignHeaders(
  username: string,
  password: string,
): Promise<Record<string, string>> {
  if (!SIGN_SECRET) {
    return {}
  }
  const timestamp = String(Date.now())
  const nonce = randomNonce()
  const payload = `${timestamp}\n${nonce}\n${username}\n${password}`
  const sign = await hmacSha256Hex(SIGN_SECRET, payload)
  return {
    [LoginSignHeaders.TIMESTAMP]: timestamp,
    [LoginSignHeaders.NONCE]: nonce,
    [LoginSignHeaders.SIGN]: sign,
  }
}
