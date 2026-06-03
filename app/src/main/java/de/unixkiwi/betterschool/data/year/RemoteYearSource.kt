package de.unixkiwi.betterschool.data.year

import de.unixkiwi.betterschool.data.RemoteBesteSchuleWrapper
import retrofit2.http.GET
import retrofit2.http.Header

interface RemoteYearSource {
    @GET("years")
    suspend fun getYears(
        @Header("Authorization") authHeader: String
    ): RemoteBesteSchuleWrapper<List<BesteSchuleYear>>
}