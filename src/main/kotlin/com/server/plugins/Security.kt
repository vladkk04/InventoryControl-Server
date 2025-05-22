package com.server.plugins

import com.server.features.security.jwtToken.JwtTokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtTokenService by inject<JwtTokenService>()

    val jwtRealm = "jwt-realm"

    authentication {
        jwt("jwt-auth") {
            realm = jwtRealm
            verifier(jwtTokenService.verifier)

            validate { credential ->
                jwtTokenService.validatorAccess(credential)
            }
        }

        jwt("jwt-refresh-auth") {
            realm = jwtRealm
            verifier(jwtTokenService.verifier)

            validate { credential ->
                jwtTokenService.validatorRefresh(credential)
            }
        }
    }
}


