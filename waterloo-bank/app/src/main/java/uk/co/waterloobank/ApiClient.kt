package uk.co.waterloobank

import com.iproov.sdk.model.Claim

interface ApiClient {
    fun getToken(claimType: Claim.ClaimType, userId: String): String
}