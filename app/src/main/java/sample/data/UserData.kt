package sample.data

data class UserData(
    val userId: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val tags: Tags? = null
)

data class Tags (
    val driverId: String,
    val rideId: String
)
