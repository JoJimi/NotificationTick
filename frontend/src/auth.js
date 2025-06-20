// src/auth.js
import { reactive } from 'vue'

export const authState = reactive({
    user: null,
    accessToken: null,
    refreshToken: null
})

export function setAuthTokens(accessToken, refreshToken) {
    authState.accessToken  = accessToken
    authState.refreshToken = refreshToken
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
}

export function setUser(userData) {
    authState.user = userData
}

export function logout() {
    authState.user = null
    authState.accessToken = null
    authState.refreshToken = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
}
