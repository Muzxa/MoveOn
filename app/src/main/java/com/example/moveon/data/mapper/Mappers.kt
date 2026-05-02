package com.example.moveon.data.mapper

import com.example.moveon.data.local.entities.BoxEntity
import com.example.moveon.data.local.entities.ItemEntity
import com.example.moveon.data.local.entities.UserSessionEntity
import com.example.moveon.domain.model.*
import com.moveon.app.data.remote.dto.*


// User Mappings
fun UserDto.toDomainModel(): User = User(
    id = this.user_id,
    firstName = this.first_name,
    lastName = this.last_name,
    email = this.email,
    phoneNumber = this.phone_number,
    role = when (this.role) {
        "Provider" -> UserRole.PROVIDER
        "Driver" -> UserRole.DRIVER
        else -> UserRole.USER
    },
    createdAt = this.created_at,
    lastLoginTime = this.last_login_time
)

fun User.toDto(): UserDto = UserDto(
    user_id = this.id,
    first_name = this.firstName,
    last_name = this.lastName,
    email = this.email,
    phone_number = this.phoneNumber,
    role = when (this.role) {
        UserRole.PROVIDER -> "Provider"
        UserRole.DRIVER -> "Driver"
        UserRole.USER -> "User"
    },
    created_at = this.createdAt,
    last_login_time = this.lastLoginTime
)

fun UserSessionEntity.toDomainModel(): User = User(
    id = this.user_id,
    firstName = this.first_name,
    lastName = this.last_name,
    email = this.email,
    phoneNumber = this.phone_number,
    role = when (this.role) {
        "Provider" -> UserRole.PROVIDER
        "Driver" -> UserRole.DRIVER
        else -> UserRole.USER
    },
    createdAt = this.created_at,
    lastLoginTime = this.last_login_time
)

fun User.toSessionEntity(): UserSessionEntity = UserSessionEntity(
    user_id = this.id,
    first_name = this.firstName,
    last_name = this.lastName,
    email = this.email,
    phone_number = this.phoneNumber,
    role = when (this.role) {
        UserRole.PROVIDER -> "Provider"
        UserRole.DRIVER -> "Driver"
        UserRole.USER -> "User"
    },
    created_at = this.createdAt,
    last_login_time = this.lastLoginTime,
    last_synced_at = System.currentTimeMillis()
)

// Booking Mappings
fun BookingDto.toDomainModel(): Booking = Booking(
    id = this.booking_id,
    userId = this.user_id,
    providerId = this.provider_id,
    status = when (this.status) {
        "Confirmed" -> BookingStatus.CONFIRMED
        "Active" -> BookingStatus.ACTIVE
        "Completed" -> BookingStatus.COMPLETED
        else -> BookingStatus.SEARCHING
    },
    pickupAddress = this.pickup_address,
    dropOffAddress = this.dropoff_address,
    pickupLat = this.pickup_lat,
    pickupLng = this.pickup_lng,
    dropOffLat = this.dropoff_lat,
    dropOffLng = this.dropoff_lng,
    totalFare = this.total_fare,
    otp = this.otp,
    isOtpVerified = this.otp_verified,
    createdAt = this.created_at,
    scheduledTime = this.scheduled_time,
    rating = this.rating,
    vehicles = emptyList() // Populated separately via BookingVehicle query
)

fun Booking.toDto(): BookingDto = BookingDto(
    booking_id = this.id,
    user_id = this.userId,
    provider_id = this.providerId,
    status = when (this.status) {
        BookingStatus.CONFIRMED -> "Confirmed"
        BookingStatus.ACTIVE -> "Active"
        BookingStatus.COMPLETED -> "Completed"
        BookingStatus.SEARCHING -> "Searching"
    },
    pickup_address = this.pickupAddress,
    dropoff_address = this.dropOffAddress,
    pickup_lat = this.pickupLat,
    pickup_lng = this.pickupLng,
    dropoff_lat = this.dropOffLat,
    dropoff_lng = this.dropOffLng,
    total_fare = this.totalFare,
    otp = this.otp,
    otp_verified = this.isOtpVerified,
    created_at = this.createdAt,
    scheduled_time = this.scheduledTime,
    rating = this.rating
)

// Vehicle Mappings
fun VehicleDto.toDomainModel(): Vehicle = Vehicle(
    id = this.vehicle_id,
    providerId = this.provider_id,
    type = this.type,
    make = this.make,
    model = this.model,
    plateNumber = this.plate_number,
    maxCapacityKg = this.max_capacity,
    maxVolumeKg = this.max_volume,
    currentLat = this.current_lat,
    currentLng = this.current_lng,
    isAvailable = this.is_available
)

fun Vehicle.toDto(): VehicleDto = VehicleDto(
    vehicle_id = this.id,
    provider_id = this.providerId,
    type = this.type,
    make = this.make,
    model = this.model,
    plate_number = this.plateNumber,
    max_capacity = this.maxCapacityKg,
    max_volume = this.maxVolumeKg,
    current_lat = this.currentLat,
    current_lng = this.currentLng,
    is_available = this.isAvailable
)

// Provider Mappings
fun ProviderDto.toDomainModel(): Provider = Provider(
    id = this.provider_id,
    establishmentName = this.establishment_name,
    isVerified = this.is_verified,
    rating = this.rating,
    baseRate = this.base_rate,
    ratePerKm = this.rate_per_km,
    businessLat = this.business_lat,
    businessLng = this.business_lng,
    phoneNumber = ""
)

fun Provider.toDto(): ProviderDto = ProviderDto(
    provider_id = this.id,
    establishment_name = this.establishmentName,
    is_verified = this.isVerified,
    rating = this.rating,
    base_rate = this.baseRate,
    rate_per_km = this.ratePerKm,
    business_lat = this.businessLat,
    business_lng = this.businessLng
)

// Driver Mappings
fun DriverDto.toDomainModel(): Driver = Driver(
    id = this.driver_id,
    providerId = this.provider_id,
    vehicleId = this.vehicle_id,
    licenseNo = this.license_no
)

fun Driver.toDto(): DriverDto = DriverDto(
    driver_id = this.id,
    provider_id = this.providerId,
    vehicle_id = this.vehicleId,
    license_no = this.licenseNo
)

// BookingVehicle Mappings
fun BookingVehicleDto.toDomainModel(): BookingVehicle = BookingVehicle(
    bookingId = this.booking_id,
    vehicleId = this.vehicle_id,
    driverId = this.driver_id
)

fun BookingVehicle.toDto(): BookingVehicleDto = BookingVehicleDto(
    booking_id = this.bookingId,
    vehicle_id = this.vehicleId,
    driver_id = this.driverId
)

// Box Mappings (Local Storage)
fun BoxEntity.toDomainModel(): Box = Box(
    boxUuid = this.box_uuid,
    boxId = this.box_id,
    bookingId = this.booking_id,
    vehicleId = this.vehicle_id,
    category = this.category,
    label = this.label,
    volume = this.volume,
    packed = this.packed,
    items = emptyList() // Populated separately
)

fun Box.toEntity(): BoxEntity = BoxEntity(
    box_uuid = this.boxUuid,
    box_id = this.boxId,
    booking_id = this.bookingId,
    vehicle_id = this.vehicleId,
    category = this.category,
    label = this.label,
    volume = this.volume,
    packed = this.packed
)

fun BoxDto.toDomainModel(): Box = Box(
    boxUuid = this.box_uuid,
    boxId = this.box_id,
    bookingId = this.booking_id,
    vehicleId = this.vehicle_id,
    category = this.category,
    label = this.label,
    volume = this.volume,
    packed = this.packed,
    items = emptyList()
)

fun Box.toDto(): BoxDto = BoxDto(
    box_uuid = this.boxUuid,
    box_id = this.boxId,
    booking_id = this.bookingId,
    vehicle_id = this.vehicleId,
    category = this.category,
    label = this.label,
    volume = this.volume,
    packed = this.packed
)

// Item Mappings (Local Storage)
fun ItemEntity.toDomainModel(): Item = Item(
    id = this.item_id.toString(),
    boxId = this.box_id,
    name = this.name,
    quantity = this.quantity,
    description = this.description ?: "",
    imageUrl = this.image_url ?: "",
    isFragile = this.is_fragile
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    item_id = this.id.toIntOrNull() ?: 0,
    box_id = this.boxId,
    name = this.name,
    quantity = this.quantity,
    description = this.description,
    image_url = this.imageUrl,
    is_fragile = this.isFragile
)