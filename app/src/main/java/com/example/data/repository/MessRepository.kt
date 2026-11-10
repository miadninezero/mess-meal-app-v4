package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.calculation.MessCalculationEngine
import com.example.data.model.AuditLog
import com.example.data.model.BazaarExpense
import com.example.data.model.DailyMealRecord
import com.example.data.model.Deposit
import com.example.data.model.FinalOrderSummary
import com.example.data.model.MealAttendanceEntry
import com.example.data.model.MealBookingEntry
import com.example.data.model.Member
import com.example.data.model.Mess
import com.example.data.model.UserProfile
import com.example.data.model.UtilityBill
import com.example.data.model.BillAllocationType
import com.example.data.model.BillPaymentRecord
import com.example.data.model.BillPaymentStatus
import com.example.data.model.InAppNotification
import com.example.data.model.SharedBill
import com.example.util.DhakaTimeHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MessRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "displayName" to profile.displayName,
            "email" to profile.email,
            "currentMessId" to profile.currentMessId,
            "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- Mess Observation ---
    fun observeMess(messId: String): Flow<Mess?> = callbackFlow {
        if (messId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("messes").document(messId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Mess::class.java))
        }
        awaitClose { listener.remove() }
    }

    fun observeMembers(messId: String): Flow<List<Member>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/members"
        emitAll(
            db.collection("messes").document(messId).collection("members")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Member::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeDailyMeals(messId: String): Flow<List<DailyMealRecord>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/daily_meals"
        emitAll(
            db.collection("messes").document(messId).collection("daily_meals")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(DailyMealRecord::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeBazaarExpenses(messId: String): Flow<List<BazaarExpense>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/bazaar_expenses"
        emitAll(
            db.collection("messes").document(messId).collection("bazaar_expenses")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(BazaarExpense::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeDeposits(messId: String): Flow<List<Deposit>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/deposits"
        emitAll(
            db.collection("messes").document(messId).collection("deposits")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Deposit::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeUtilityBills(messId: String): Flow<List<UtilityBill>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/utility_bills"
        emitAll(
            db.collection("messes").document(messId).collection("utility_bills")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UtilityBill::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeAuditLogs(messId: String): Flow<List<AuditLog>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/audit_logs"
        emitAll(
            db.collection("messes").document(messId).collection("audit_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(AuditLog::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun recordAuditLog(messId: String, log: AuditLog) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("audit_logs").document()
        val payload = mapOf(
            "action" to log.action,
            "performedByUid" to uid,
            "performedByName" to log.performedByName,
            "targetMemberId" to log.targetMemberId,
            "targetMemberName" to log.targetMemberName,
            "details" to log.details,
            "reason" to log.reason,
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            Log.w("MessRepository", "Audit log write failure: ${e.message}")
        }
    }

    // --- Mess Setup & Deadlines ---
    suspend fun createMess(messName: String, managerName: String, defaultMonth: String): String {
        val uid = requireUserId()
        val messDocRef = db.collection("messes").document()
        val messId = messDocRef.id

        val messPayload = mapOf(
            "name" to messName,
            "managerUid" to uid,
            "adminUids" to listOf(uid),
            "memberUids" to listOf(uid),
            "activeMonth" to defaultMonth,
            "lunchBookingDeadline" to "09:00",
            "nightBookingDeadline" to "17:00",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val memberDocRef = messDocRef.collection("members").document(uid)
        val memberPayload = mapOf(
            "userId" to uid,
            "displayName" to managerName,
            "room" to "Manager",
            "role" to "MANAGER",
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val userDocRef = db.collection("users").document(uid)
        val userPayload = mapOf(
            "currentMessId" to messId,
            "displayName" to (auth.currentUser?.displayName?.ifBlank { managerName } ?: managerName),
            "email" to (auth.currentUser?.email ?: ""),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            val batch = db.batch()
            batch.set(messDocRef, messPayload)
            batch.set(memberDocRef, memberPayload)
            batch.set(userDocRef, userPayload, SetOptions.merge())
            batch.commit().await()
            return messId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, messDocRef.path)
            throw e
        }
    }

    suspend fun joinMess(messId: String, memberName: String, room: String) {
        val uid = requireUserId()
        val messDocRef = db.collection("messes").document(messId)

        val memberDocRef = messDocRef.collection("members").document(uid)
        val memberPayload = mapOf(
            "userId" to uid,
            "displayName" to memberName,
            "room" to room,
            "role" to "MEMBER",
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val userDocRef = db.collection("users").document(uid)
        val userPayload = mapOf(
            "currentMessId" to messId,
            "displayName" to (auth.currentUser?.displayName?.ifBlank { memberName } ?: memberName),
            "email" to (auth.currentUser?.email ?: ""),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            val batch = db.batch()
            batch.update(messDocRef, "memberUids", FieldValue.arrayUnion(uid))
            batch.set(memberDocRef, memberPayload)
            batch.set(userDocRef, userPayload, SetOptions.merge())
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, messDocRef.path)
            throw e
        }
    }

    suspend fun updateBookingDeadlines(messId: String, lunchDeadline: String, nightDeadline: String) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId)
        try {
            docRef.update(
                mapOf(
                    "lunchBookingDeadline" to lunchDeadline,
                    "nightBookingDeadline" to nightDeadline,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "DEADLINE_CONFIGURED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Lunch cutoff: $lunchDeadline, Night cutoff: $nightDeadline (Asia/Dhaka)"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Member Role Management (Promotion & Resignation) ---
    suspend fun promoteMemberToManager(messId: String, targetMember: Member) {
        val uid = requireUserId()
        val targetUid = targetMember.userId.ifBlank { targetMember.id }
        val memberDocId = targetMember.id.ifBlank { targetUid }

        val messDocRef = db.collection("messes").document(messId)
        val memberDocRef = messDocRef.collection("members").document(memberDocId)

        try {
            db.runBatch { batch ->
                batch.update(memberDocRef, "role", "MANAGER")
                batch.update(messDocRef, "adminUids", FieldValue.arrayUnion(targetUid))
                batch.update(messDocRef, "updatedAt", FieldValue.serverTimestamp())
            }.await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "MEMBER_PROMOTED_TO_MANAGER",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = targetMember.id,
                    targetMemberName = targetMember.displayName,
                    details = "Promoted ${targetMember.displayName} to Manager"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, memberDocRef.path)
            throw e
        }
    }

    suspend fun demoteManagerToMember(messId: String, targetMember: Member) {
        val uid = requireUserId()
        val targetUid = targetMember.userId.ifBlank { targetMember.id }
        val memberDocId = targetMember.id.ifBlank { targetUid }

        val messDocRef = db.collection("messes").document(messId)
        val memberDocRef = messDocRef.collection("members").document(memberDocId)

        try {
            db.runBatch { batch ->
                batch.update(memberDocRef, "role", "MEMBER")
                batch.update(messDocRef, "adminUids", FieldValue.arrayRemove(targetUid))
                batch.update(messDocRef, "updatedAt", FieldValue.serverTimestamp())
            }.await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "MANAGER_DEMOTED_TO_MEMBER",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = targetMember.id,
                    targetMemberName = targetMember.displayName,
                    details = "Changed ${targetMember.displayName}'s role to Member"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, memberDocRef.path)
            throw e
        }
    }

    suspend fun resignAsManager(messId: String, currentMemberId: String, successor: Member?) {
        val uid = requireUserId()
        val messDocRef = db.collection("messes").document(messId)
        val currentMemberDocRef = messDocRef.collection("members").document(currentMemberId.ifBlank { uid })

        try {
            val messSnap = messDocRef.get().await()
            val mess = messSnap.toObject(Mess::class.java) ?: Mess()
            val currentAdmins = mess.adminUids.toMutableList()

            db.runBatch { batch ->
                batch.update(currentMemberDocRef, "role", "MEMBER")
                batch.update(messDocRef, "adminUids", FieldValue.arrayRemove(uid))

                if (successor != null) {
                    val successorUid = successor.userId.ifBlank { successor.id }
                    val successorDocRef = messDocRef.collection("members").document(successor.id.ifBlank { successorUid })
                    batch.update(successorDocRef, "role", "MANAGER")
                    batch.update(messDocRef, "adminUids", FieldValue.arrayUnion(successorUid))
                    if (mess.managerUid == uid) {
                        batch.update(messDocRef, "managerUid", successorUid)
                    }
                } else {
                    val remainingAdmins = currentAdmins.filter { it != uid }
                    if (mess.managerUid == uid && remainingAdmins.isNotEmpty()) {
                        batch.update(messDocRef, "managerUid", remainingAdmins.first())
                    }
                }
                batch.update(messDocRef, "updatedAt", FieldValue.serverTimestamp())
            }.await()

            val detailsText = if (successor != null) {
                "${auth.currentUser?.displayName ?: "Manager"} resigned and transferred Manager role to ${successor.displayName}"
            } else {
                "${auth.currentUser?.displayName ?: "Manager"} resigned from Manager role"
            }

            recordAuditLog(
                messId,
                AuditLog(
                    action = "MANAGER_RESIGNED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = successor?.id ?: currentMemberId,
                    targetMemberName = successor?.displayName ?: "",
                    details = detailsText
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, messDocRef.path)
            throw e
        }
    }

    // --- Service Status & Unexpected Cancellation ---
    suspend fun updateMealServiceStatus(
        messId: String,
        date: String,
        lunchStatus: String,
        nightStatus: String,
        cancellationReason: String
    ) {
        val uid = requireUserId()
        val isFriday = DhakaTimeHelper.isFriday(date)
        val docRef = db.collection("messes").document(messId).collection("daily_meals").document(date)

        val payload = mapOf(
            "date" to date,
            "isFriday" to isFriday,
            "lunchServiceStatus" to lunchStatus,
            "nightServiceStatus" to (if (isFriday) "CANCELLED" else nightStatus),
            "cancellationReason" to cancellationReason,
            "updatedBy" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload, SetOptions.merge()).await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "SERVICE_STATUS_UPDATED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Date: $date | Lunch: $lunchStatus, Night: $nightStatus | Reason: $cancellationReason",
                    reason = cancellationReason
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Member Meal Booking (Before Deadline) ---
    suspend fun saveMemberBooking(
        messId: String,
        date: String,
        memberId: String,
        booking: MealBookingEntry,
        lunchDeadline: String = "09:00",
        nightDeadline: String = "17:00",
        isManager: Boolean = false
    ) {
        val uid = requireUserId()
        val isFriday = DhakaTimeHelper.isFriday(date)
        val docRef = db.collection("messes").document(messId).collection("daily_meals").document(date)

        val isLunchPassed = DhakaTimeHelper.isDeadlinePassed(date, lunchDeadline)
        val isNightPassed = if (isFriday) true else DhakaTimeHelper.isDeadlinePassed(date, nightDeadline)

        if (!isManager && isLunchPassed && isNightPassed) {
            throw IllegalStateException("Booking deadlines for both Lunch and Night have passed. Bookings are locked.")
        }

        val effectiveBooking = if (isFriday) {
            booking.copy(statusNight = "CANCELLED", guestNight = 0, updatedBy = uid)
        } else {
            booking.copy(updatedBy = uid)
        }

        val bookingPayload = mapOf(
            "statusLunch" to effectiveBooking.statusLunch,
            "statusNight" to effectiveBooking.statusNight,
            "guestLunch" to effectiveBooking.guestLunch,
            "guestNight" to effectiveBooking.guestNight,
            "updatedBy" to uid,
            "managerOverrideReason" to effectiveBooking.managerOverrideReason
        )

        val payload = mapOf(
            "date" to date,
            "isFriday" to isFriday,
            "bookings.$memberId" to bookingPayload,
            "updatedBy" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(
                mapOf(
                    "date" to date,
                    "isFriday" to isFriday,
                    "lunchServiceStatus" to "ACTIVE",
                    "nightServiceStatus" to (if (isFriday) "CANCELLED" else "ACTIVE")
                ),
                SetOptions.merge()
            ).await()

            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Save Final Order Summary at Deadline & Notify Manager ---
    suspend fun saveFinalMealOrderSummary(
        messId: String,
        date: String,
        mealType: String,
        summary: FinalOrderSummary
    ) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("daily_meals").document(date)
        val fieldName = if (mealType.equals("LUNCH", ignoreCase = true)) "lunchFinalOrder" else "nightFinalOrder"
        val orderMap = mapOf(
            "mealType" to summary.mealType,
            "date" to summary.date,
            "totalPortions" to summary.totalPortions,
            "memberCount" to summary.memberCount,
            "guestCount" to summary.guestCount,
            "deadline" to summary.deadline,
            "isLocked" to true,
            "lockedAtDhakaTime" to summary.lockedAtDhakaTime,
            "savedAt" to FieldValue.serverTimestamp()
        )
        val payload = mapOf(
            fieldName to orderMap,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(mapOf("date" to date), SetOptions.merge()).await()
            docRef.update(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "FINAL_ORDER_LOCKED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Final ${summary.mealType} order locked for $date: ${summary.totalPortions} total portions (${summary.memberCount} members, ${summary.guestCount} guests). Kitchen preparation summary saved."
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Manager Booking Override (With Audit History) ---
    suspend fun overrideMemberBooking(
        messId: String,
        date: String,
        memberId: String,
        memberName: String,
        booking: MealBookingEntry,
        reason: String
    ) {
        val uid = requireUserId()
        val isFriday = DhakaTimeHelper.isFriday(date)
        val docRef = db.collection("messes").document(messId).collection("daily_meals").document(date)

        val bookingPayload = mapOf(
            "statusLunch" to booking.statusLunch,
            "statusNight" to (if (isFriday) "CANCELLED" else booking.statusNight),
            "guestLunch" to booking.guestLunch,
            "guestNight" to (if (isFriday) 0 else booking.guestNight),
            "updatedBy" to uid,
            "managerOverrideReason" to reason
        )

        val payload = mapOf(
            "date" to date,
            "isFriday" to isFriday,
            "bookings.$memberId" to bookingPayload,
            "updatedBy" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(
                mapOf("date" to date, "isFriday" to isFriday),
                SetOptions.merge()
            ).await()

            docRef.update(payload).await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "BOOKING_OVERRIDE",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = memberId,
                    targetMemberName = memberName,
                    details = "Date: $date | Lunch: ${booking.statusLunch}, Night: ${booking.statusNight}, Guests: L${booking.guestLunch}/N${booking.guestNight}",
                    reason = reason
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Confirmed Actual Attendance (Manager only) ---
    suspend fun saveAttendance(
        messId: String,
        date: String,
        memberId: String,
        memberName: String,
        attendance: MealAttendanceEntry,
        lunchServiceStatus: String,
        nightServiceStatus: String
    ) {
        val uid = requireUserId()
        val isFriday = DhakaTimeHelper.isFriday(date)
        val docRef = db.collection("messes").document(messId).collection("daily_meals").document(date)

        val units = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = isFriday,
            lunchServiceStatus = lunchServiceStatus,
            nightServiceStatus = nightServiceStatus,
            attendance = attendance
        )

        val attendancePayload = mapOf(
            "attendedLunch" to attendance.attendedLunch,
            "attendedNight" to attendance.attendedNight,
            "guestLunchAttended" to attendance.guestLunchAttended,
            "guestNightAttended" to attendance.guestNightAttended,
            "calculatedUnits" to units
        )

        val payload = mapOf(
            "date" to date,
            "isFriday" to isFriday,
            "attendances.$memberId" to attendancePayload,
            "updatedBy" to uid,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(
                mapOf(
                    "date" to date,
                    "isFriday" to isFriday,
                    "lunchServiceStatus" to lunchServiceStatus,
                    "nightServiceStatus" to nightServiceStatus
                ),
                SetOptions.merge()
            ).await()

            docRef.update(payload).await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "ATTENDANCE_CONFIRMED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = memberId,
                    targetMemberName = memberName,
                    details = "Date: $date | Lunch: ${attendance.attendedLunch}, Night: ${attendance.attendedNight}, Guests: L${attendance.guestLunchAttended}/N${attendance.guestNightAttended} -> $units units"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Financial Records (Deposits & Utilities) ---
    suspend fun addDeposit(messId: String, deposit: Deposit) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("deposits").document()
        val payload = mapOf(
            "memberId" to deposit.memberId,
            "memberName" to deposit.memberName,
            "amount" to deposit.amount,
            "date" to deposit.date,
            "paymentMethod" to deposit.paymentMethod,
            "notes" to deposit.notes,
            "recordedBy" to uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "DEPOSIT_RECORDED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = deposit.memberId,
                    targetMemberName = deposit.memberName,
                    details = "Amount: ${deposit.amount} | Method: ${deposit.paymentMethod} | Notes: ${deposit.notes}"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateDeposit(messId: String, deposit: Deposit) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("deposits").document(deposit.id)
        val payload = mapOf(
            "memberId" to deposit.memberId,
            "memberName" to deposit.memberName,
            "amount" to deposit.amount,
            "date" to deposit.date,
            "paymentMethod" to deposit.paymentMethod,
            "notes" to deposit.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "DEPOSIT_UPDATED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = deposit.memberId,
                    targetMemberName = deposit.memberName,
                    details = "Updated deposit to: ${deposit.amount} (${deposit.paymentMethod}) | Notes: ${deposit.notes}"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteDeposit(messId: String, deposit: Deposit) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("deposits").document(deposit.id)
        try {
            docRef.delete().await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "DEPOSIT_DELETED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = deposit.memberId,
                    targetMemberName = deposit.memberName,
                    details = "Deleted deposit of ${deposit.amount} (${deposit.paymentMethod})"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun addUtilityBill(messId: String, bill: UtilityBill) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("utility_bills").document()
        val payload = mapOf(
            "title" to bill.title,
            "amount" to bill.amount,
            "month" to bill.month,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "UTILITY_BILL_ADDED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Title: ${bill.title} | Amount: ${bill.amount} | Month: ${bill.month}"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUtilityBill(messId: String, bill: UtilityBill) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("utility_bills").document(bill.id)
        val payload = mapOf(
            "title" to bill.title,
            "amount" to bill.amount,
            "month" to bill.month,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "UTILITY_BILL_UPDATED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Updated bill: ${bill.title} | Amount: ${bill.amount} | Month: ${bill.month}"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteUtilityBill(messId: String, billId: String, billTitle: String, amount: Double) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("utility_bills").document(billId)
        try {
            docRef.delete().await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "UTILITY_BILL_DELETED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Deleted bill: $billTitle | Amount: $amount"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Bazaar Expenses ---
    suspend fun addBazaarExpense(messId: String, expense: BazaarExpense) {
        requireUserId()
        val docRef = db.collection("messes").document(messId).collection("bazaar_expenses").document()
        val payload = mapOf(
            "buyerMemberId" to expense.buyerMemberId,
            "buyerName" to expense.buyerName,
            "amount" to expense.amount,
            "itemsDescription" to expense.itemsDescription,
            "category" to expense.category,
            "date" to expense.date,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateBazaarExpense(messId: String, expense: BazaarExpense) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("bazaar_expenses").document(expense.id)
        val payload = mapOf(
            "buyerMemberId" to expense.buyerMemberId,
            "buyerName" to expense.buyerName,
            "amount" to expense.amount,
            "itemsDescription" to expense.itemsDescription,
            "category" to expense.category,
            "date" to expense.date,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "BAZAAR_UPDATED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Updated expense: ${expense.itemsDescription} | Amount: ${expense.amount} | Date: ${expense.date}"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBazaarExpense(messId: String, expenseId: String) {
        val docRef = db.collection("messes").document(messId).collection("bazaar_expenses").document(expenseId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun updateMemberStatus(messId: String, memberId: String, isActive: Boolean) {
        val docRef = db.collection("messes").document(messId).collection("members").document(memberId)
        try {
            docRef.update("isActive", isActive).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Shared Bills & Fixed Costs ---
    fun observeSharedBills(messId: String): Flow<List<SharedBill>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/shared_bills"
        emitAll(
            db.collection("messes").document(messId).collection("shared_bills")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(SharedBill::class.java) }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun createSharedBill(
        messId: String,
        title: String,
        allocationType: String,
        amountInput: Double,
        month: String,
        deadline: String,
        notes: String,
        activeMembers: List<Member>,
        customShares: Map<String, Double> = emptyMap()
    ): String {
        val uid = requireUserId()
        if (activeMembers.isEmpty()) {
            throw IllegalArgumentException("Cannot create a shared bill with 0 active members.")
        }
        val memberCount = activeMembers.size
        val isCustom = allocationType == BillAllocationType.CUSTOM_PER_MEMBER && customShares.isNotEmpty()

        val (totalAmount, perMemberAmount) = when {
            isCustom -> {
                val total = MessCalculationEngine.roundTwoDecimals(customShares.values.sum())
                val avg = if (memberCount > 0) MessCalculationEngine.roundTwoDecimals(total / memberCount) else 0.0
                Pair(total, avg)
            }
            allocationType == BillAllocationType.FIXED_PER_MEMBER -> {
                val total = MessCalculationEngine.roundTwoDecimals(amountInput * memberCount)
                val perMem = MessCalculationEngine.roundTwoDecimals(amountInput)
                Pair(total, perMem)
            }
            else -> {
                val total = MessCalculationEngine.roundTwoDecimals(amountInput)
                val perMem = MessCalculationEngine.roundTwoDecimals(total / memberCount)
                Pair(total, perMem)
            }
        }

        val assignedIds = activeMembers.map { it.id }
        val memberShares = mutableMapOf<String, Double>()
        val memberNames = mutableMapOf<String, String>()
        val initialPayments = mutableMapOf<String, Map<String, Any?>>()

        activeMembers.forEach { m ->
            val share = if (isCustom) {
                customShares[m.id] ?: 0.0
            } else {
                perMemberAmount
            }
            memberShares[m.id] = share
            memberNames[m.id] = m.displayName.ifBlank { "Member" }
            initialPayments[m.id] = mapOf(
                "memberId" to m.id,
                "memberName" to m.displayName.ifBlank { "Member" },
                "amountPaid" to 0.0,
                "status" to BillPaymentStatus.UNPAID,
                "paymentDate" to "",
                "paymentMethod" to "Cash",
                "notes" to ""
            )
        }

        val billDocRef = db.collection("messes").document(messId).collection("shared_bills").document()
        val billId = billDocRef.id
        val creatorName = auth.currentUser?.displayName ?: "Manager"

        val billPayload = mapOf(
            "title" to title,
            "allocationType" to allocationType,
            "totalAmount" to totalAmount,
            "perMemberAmount" to perMemberAmount,
            "month" to month,
            "deadline" to deadline,
            "notes" to notes,
            "assignedMemberIds" to assignedIds,
            "memberShares" to memberShares,
            "memberNames" to memberNames,
            "payments" to initialPayments,
            "createdByUid" to uid,
            "createdByName" to creatorName,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            val batch = db.batch()
            batch.set(billDocRef, billPayload)

            // Send in-app notification to all assigned members showing individual amount & deadline
            activeMembers.forEach { member ->
                val memShare = memberShares[member.id] ?: perMemberAmount
                val notifDocRef = db.collection("messes").document(messId).collection("notifications").document()
                val notifPayload = mapOf(
                    "messId" to messId,
                    "targetMemberId" to member.id,
                    "targetMemberName" to member.displayName,
                    "title" to "New Shared Bill: $title",
                    "message" to "You have been assigned ৳$memShare for '$title'. Payment deadline: $deadline.",
                    "billId" to billId,
                    "amount" to memShare,
                    "deadline" to deadline,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                batch.set(notifDocRef, notifPayload)
            }

            batch.commit().await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "SHARED_BILL_PUBLISHED",
                    performedByUid = uid,
                    performedByName = creatorName,
                    details = "Published '$title': ৳$totalAmount total ($allocationType) for $memberCount members. Deadline: $deadline"
                )
            )

            return billId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, billDocRef.path)
            throw e
        }
    }

    suspend fun updateSharedBill(
        messId: String,
        bill: SharedBill,
        updatedShares: Map<String, Double>? = null
    ) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("shared_bills").document(bill.id)

        val finalShares = updatedShares ?: bill.memberShares
        val finalTotal = if (updatedShares != null) {
            MessCalculationEngine.roundTwoDecimals(finalShares.values.sum())
        } else {
            bill.totalAmount
        }
        val finalPerMem = if (bill.assignedMemberIds.isNotEmpty()) {
            MessCalculationEngine.roundTwoDecimals(finalTotal / bill.assignedMemberIds.size)
        } else {
            bill.perMemberAmount
        }

        // Recalculate payment statuses based on any updated shares
        val updatedPayments = bill.payments.mapValues { (memId, record) ->
            val share = finalShares[memId] ?: finalPerMem
            val status = when {
                record.amountPaid >= share && share > 0.0 -> BillPaymentStatus.PAID
                record.amountPaid > 0.0 -> BillPaymentStatus.PARTIALLY_PAID
                else -> BillPaymentStatus.UNPAID
            }
            mapOf(
                "memberId" to record.memberId,
                "memberName" to record.memberName,
                "amountPaid" to record.amountPaid,
                "status" to status,
                "paymentDate" to record.paymentDate,
                "paymentMethod" to record.paymentMethod,
                "notes" to record.notes,
                "recordedByUid" to record.recordedByUid,
                "recordedByName" to record.recordedByName,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        val payload = mapOf(
            "title" to bill.title,
            "deadline" to bill.deadline,
            "notes" to bill.notes,
            "totalAmount" to finalTotal,
            "perMemberAmount" to finalPerMem,
            "memberShares" to finalShares,
            "payments" to updatedPayments,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "SHARED_BILL_UPDATED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Updated bill details for '${bill.title}' (Total: ৳$finalTotal, Deadline: ${bill.deadline})"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSharedBill(messId: String, billId: String, billTitle: String, totalAmount: Double) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("shared_bills").document(billId)
        try {
            docRef.delete().await()
            recordAuditLog(
                messId,
                AuditLog(
                    action = "SHARED_BILL_DELETED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    details = "Deleted shared bill '$billTitle' (Total: ৳$totalAmount)"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun recordBillPayment(
        messId: String,
        billId: String,
        memberId: String,
        memberName: String,
        assignedShare: Double,
        amountPaid: Double,
        paymentMethod: String,
        paymentDate: String,
        notes: String
    ) {
        val uid = requireUserId()
        val docRef = db.collection("messes").document(messId).collection("shared_bills").document(billId)

        val status = when {
            amountPaid >= assignedShare && assignedShare > 0.0 -> BillPaymentStatus.PAID
            amountPaid > 0.0 -> BillPaymentStatus.PARTIALLY_PAID
            else -> BillPaymentStatus.UNPAID
        }

        val paymentPayload = mapOf(
            "memberId" to memberId,
            "memberName" to memberName,
            "amountPaid" to amountPaid,
            "status" to status,
            "paymentDate" to paymentDate,
            "paymentMethod" to paymentMethod,
            "notes" to notes,
            "recordedByUid" to uid,
            "recordedByName" to (auth.currentUser?.displayName ?: "Manager"),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(
                mapOf(
                    "payments.$memberId" to paymentPayload,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            recordAuditLog(
                messId,
                AuditLog(
                    action = "BILL_PAYMENT_RECORDED",
                    performedByUid = uid,
                    performedByName = auth.currentUser?.displayName ?: "Manager",
                    targetMemberId = memberId,
                    targetMemberName = memberName,
                    details = "Recorded payment of ৳$amountPaid for $memberName (Status: $status, Method: $paymentMethod)"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- In-App Notifications ---
    fun observeInAppNotifications(messId: String, memberId: String): Flow<List<InAppNotification>> = flow {
        if (messId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "messes/$messId/notifications"
        emitAll(
            db.collection("messes").document(messId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(InAppNotification::class.java).filter { notif ->
                        notif.targetMemberId == memberId || notif.targetMemberId == "ALL" || notif.targetMemberId.isBlank()
                    }
                }
                .catch { error ->
                    handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun markNotificationAsRead(messId: String, notificationId: String) {
        val docRef = db.collection("messes").document(messId).collection("notifications").document(notificationId)
        try {
            docRef.update("isRead", true).await()
        } catch (e: Exception) {
            Log.w("MessRepository", "Failed to mark notification as read: ${e.message}")
        }
    }

    suspend fun markAllNotificationsAsRead(messId: String, notificationIds: List<String>) {
        if (notificationIds.isEmpty()) return
        try {
            val batch = db.batch()
            notificationIds.forEach { id ->
                val docRef = db.collection("messes").document(messId).collection("notifications").document(id)
                batch.update(docRef, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.w("MessRepository", "Failed to mark all notifications as read: ${e.message}")
        }
    }
}
