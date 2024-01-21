package sample

data class UserData(
    val userId: String,
    val email: String,
    val phoneNumber: String,
    val tags: Tags
)

data class Tags (
    val driverId: String,
    val rideId: String
)
