// src/api.js
import axios from 'axios'
import { authState } from './auth.js'

const apiClient = axios.create({
    baseURL: '/',  // dev proxy → /api
    headers: { 'Content-Type': 'application/json' }
})

apiClient.interceptors.request.use(config => {
    const token = authState.accessToken
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

export default apiClient
