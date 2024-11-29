package me.clarius.sdk.cast.example.overlay

import me.clarius.sdk.cast.example.CastService

class CastWrapper() {
    companion object {
        private lateinit var castService: CastService

        fun isInitialized(): Boolean {
            return ::castService.isInitialized
        }

        fun getCastService(): CastService {
            return castService
        }
        fun setCastService(service: CastService) {
            castService = service
        }

    }
}