package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val currentMessId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Mess(
    @DocumentId val id: String = "",
    val name: String = "",
    val managerUid: String = "",
    val adminUids: List<String> = emptyList(),
    val memberUids: List<String> = emptyList(),
    val activeMonth: String = "", // e.g. "2026-11"
    val lunchBookingDeadline: String = "09:00", // HH:mm (24-hr) in Asia/Dhaka
    val nightBookingDeadline: String = "17:00", // HH:mm (24-hr) in Asia/Dhaka
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isManager(userId: String): Boolean {
        return managerUid == userId || adminUids.contains(userId)
    }
}

@IgnoreExtraProperties
data class Member(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val room: String = "",
    val role: String = "MEMBER", // "MANAGER" or "MEMBER"
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

// Meal Booking state per meal: "NO_RESPONSE", "BOOKED", "CANCELLED"
@IgnoreExtraProperties
data class MealBookingEntry(
    val statusLunch: String = "NO_RESPONSE", // "BOOKED", "CANCELLED", "NO_RESPONSE"
    val statusNight: String = "NO_RESPONSE", // "BOOKED", "CANCELLED", "NO_RESPONSE"
    val guestLunch: Int = 0,
    val guestNight: Int = 0,
    val updatedBy: String = "",
    val managerOverrideReason: String = ""
)

// Confirmed Actual Attendance Entry
@IgnoreExtraProperties
data class MealAttendanceEntry(
    val attendedLunch: Boolean = false,
    val attendedNight: Boolean = false,
    val guestLunchAttended: Int = 0,
    val guestNightAttended: Int = 0,
    val calculatedUnits: Double = 0.0
)

@IgnoreExtraProperties
data class FinalOrderSummary(
    val mealType: String = "", // "LUNCH" or "NIGHT"
    val date: String = "",
    val totalPortions: Int = 0,
    val memberCount: Int = 0,
    val guestCount: Int = 0,
    val deadline: String = "",
    val isLocked: Boolean = true,
    val lockedAtDhakaTime: String = "",
    @ServerTimestamp val savedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DailyMealRecord(
    @DocumentId val id: String = "", // date format YYYY-MM-DD
    val date: String = "",
    val isFriday: Boolean = false,
    val lunchServiceStatus: String = "ACTIVE", // "ACTIVE" or "CANCELLED"
    val nightServiceStatus: String = "ACTIVE", // "ACTIVE" or "CANCELLED"
    val cancellationReason: String = "",
    val bookings: Map<String, MealBookingEntry> = emptyMap(),
    val attendances: Map<String, MealAttendanceEntry> = emptyMap(),
    val lunchFinalOrder: FinalOrderSummary? = null,
    val nightFinalOrder: FinalOrderSummary? = null,
    val totalDayUnits: Double = 0.0,
    val totalDayMeals: Double = 0.0, // backward compatibility
    val updatedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val action: String = "", // e.g. "DEPOSIT_ADDED", "DEPOSIT_DELETED", "BOOKING_OVERRIDE", "ATTENDANCE_CONFIRMED", "MEAL_SERVICE_CANCELLED"
    val performedByUid: String = "",
    val performedByName: String = "",
    val targetMemberId: String = "",
    val targetMemberName: String = "",
    val details: String = "",
    val reason: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val timestampFormatted: String
        get() {
            val t = timestamp ?: return "Just now"
            val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Dhaka")
            }
            return sdf.format(t.toDate())
        }
}

@IgnoreExtraProperties
data class BazaarExpense(
    @DocumentId val id: String = "",
    val buyerMemberId: String = "",
    val buyerName: String = "",
    val amount: Double = 0.0,
    val itemsDescription: String = "",
    val category: String = "Groceries", // Groceries, Meat & Fish, Vegetables, Spices, Misc
    val date: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Deposit(
    @DocumentId val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val paymentMethod: String = "Cash", // Cash, bKash/Nagad, Bank, UPI
    val notes: String = "",
    val recordedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UtilityBill(
    @DocumentId val id: String = "",
    val title: String = "", // e.g. Cook Salary, Gas, WiFi, Electricity
    val amount: Double = 0.0,
    val month: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

object BillAllocationType {
    const val FIXED_PER_MEMBER = "FIXED_PER_MEMBER"
    const val DIVIDED_EQUALLY = "DIVIDED_EQUALLY"
    const val CUSTOM_PER_MEMBER = "CUSTOM_PER_MEMBER"
}

object BillPaymentStatus {
    const val UNPAID = "UNPAID"
    const val PARTIALLY_PAID = "PARTIALLY_PAID"
    const val PAID = "PAID"
}

@IgnoreExtraProperties
data class BillPaymentRecord(
    val memberId: String = "",
    val memberName: String = "",
    val amountPaid: Double = 0.0,
    val status: String = BillPaymentStatus.UNPAID,
    val paymentDate: String = "",
    val paymentMethod: String = "Cash", // Cash, bKash/Nagad, Bank, etc.
    val notes: String = "",
    val recordedByUid: String = "",
    val recordedByName: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SharedBill(
    @DocumentId val id: String = "",
    val title: String = "", // e.g. "Chef Bill", "Gas Bill", "WiFi", "Housemaid"
    val allocationType: String = BillAllocationType.FIXED_PER_MEMBER, // FIXED_PER_MEMBER or DIVIDED_EQUALLY
    val totalAmount: Double = 0.0,
    val perMemberAmount: Double = 0.0,
    val month: String = "", // e.g. "2026-11"
    val deadline: String = "", // YYYY-MM-DD
    val notes: String = "",
    val assignedMemberIds: List<String> = emptyList(),
    val memberShares: Map<String, Double> = emptyMap(), // memberId -> assigned share amount (frozen at creation)
    val memberNames: Map<String, String> = emptyMap(), // memberId -> member display name (frozen snapshot)
    val payments: Map<String, BillPaymentRecord> = emptyMap(), // memberId -> payment record
    val createdByUid: String = "",
    val createdByName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val totalCollected: Double
        get() = payments.values.sumOf { it.amountPaid }

    @get:com.google.firebase.firestore.Exclude
    val outstandingBalance: Double
        get() = (totalAmount - totalCollected).coerceAtLeast(0.0)

    @get:com.google.firebase.firestore.Exclude
    val paidMembersCount: Int
        get() = assignedMemberIds.count { id ->
            val p = payments[id]
            val share = memberShares[id] ?: perMemberAmount
            (p?.amountPaid ?: 0.0) >= share && share > 0.0
        }

    @get:com.google.firebase.firestore.Exclude
    val partiallyPaidMembersCount: Int
        get() = assignedMemberIds.count { id ->
            val p = payments[id]
            val share = memberShares[id] ?: perMemberAmount
            val paid = p?.amountPaid ?: 0.0
            paid > 0.0 && paid < share
        }

    @get:com.google.firebase.firestore.Exclude
    val unpaidMembersCount: Int
        get() = assignedMemberIds.count { id ->
            val p = payments[id]
            (p?.amountPaid ?: 0.0) == 0.0
        }

    fun getMemberPaymentStatus(memberId: String): String {
        val share = getMemberShare(memberId)
        val paid = getMemberPaid(memberId)
        return when {
            share <= 0.0 -> BillPaymentStatus.PAID
            paid >= share && share > 0.0 -> BillPaymentStatus.PAID
            paid > 0.0 -> BillPaymentStatus.PARTIALLY_PAID
            else -> BillPaymentStatus.UNPAID
        }
    }

    fun getMemberShare(memberId: String): Double {
        if (assignedMemberIds.isNotEmpty() && !assignedMemberIds.contains(memberId)) {
            return 0.0
        }
        return memberShares[memberId] ?: perMemberAmount
    }

    fun getMemberPaid(memberId: String): Double {
        return payments[memberId]?.amountPaid ?: 0.0
    }

    fun getMemberDue(memberId: String): Double {
        val share = getMemberShare(memberId)
        val paid = getMemberPaid(memberId)
        return (share - paid).coerceAtLeast(0.0)
    }
}

@IgnoreExtraProperties
data class InAppNotification(
    @DocumentId val id: String = "",
    val messId: String = "",
    val targetMemberId: String = "", // user ID or member ID or "ALL"
    val targetMemberName: String = "",
    val title: String = "",
    val message: String = "",
    val billId: String = "",
    val amount: Double = 0.0,
    val deadline: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    @get:com.google.firebase.firestore.Exclude
    val timestampFormatted: String
        get() {
            val t = createdAt ?: return "Just now"
            val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Dhaka")
            }
            return sdf.format(t.toDate())
        }
}

