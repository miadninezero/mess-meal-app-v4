package com.example

import com.example.data.calculation.MessCalculationEngine
import com.example.data.model.BazaarExpense
import com.example.data.model.DailyMealRecord
import com.example.data.model.Deposit
import com.example.data.model.MealAttendanceEntry
import com.example.data.model.Member
import com.example.data.model.UtilityBill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessCalculationEngineTest {

    @Test
    fun testNormalDayAttendanceUnits() {
        val entryBoth = MealAttendanceEntry(attendedLunch = true, attendedNight = true)
        val entryLunchOnly = MealAttendanceEntry(attendedLunch = true, attendedNight = false)
        val entryNightOnly = MealAttendanceEntry(attendedLunch = false, attendedNight = true)
        val entryNeither = MealAttendanceEntry(attendedLunch = false, attendedNight = false)

        val unitsBoth = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            attendance = entryBoth
        )
        val unitsLunch = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            attendance = entryLunchOnly
        )
        val unitsNight = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            attendance = entryNightOnly
        )
        val unitsNeither = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            attendance = entryNeither
        )

        assertEquals("Both lunch and night on normal day must count as 2.0 units", 2.0, unitsBoth, 0.001)
        assertEquals("Lunch only on normal day must count as 1.5 units", 1.5, unitsLunch, 0.001)
        assertEquals("Night only on normal day must count as 1.0 unit", 1.0, unitsNight, 0.001)
        assertEquals("Neither meal attended must count as 0.0 units", 0.0, unitsNeither, 0.001)
    }

    @Test
    fun testFridayAttendanceUnits() {
        val entryAttended = MealAttendanceEntry(attendedLunch = true, attendedNight = true)
        val entryNotAttended = MealAttendanceEntry(attendedLunch = false, attendedNight = false)

        val unitsAttended = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = true,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            attendance = entryAttended
        )
        val unitsNotAttended = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = true,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            attendance = entryNotAttended
        )

        assertEquals("Friday lunch must count as 1.0 unit", 1.0, unitsAttended, 0.001)
        assertEquals("Friday not attended must count as 0.0 units", 0.0, unitsNotAttended, 0.001)
    }

    @Test
    fun testSingleMealServedDueToUnexpectedCancellation() {
        val entry = MealAttendanceEntry(attendedLunch = true, attendedNight = true)

        // Lunch served, night cancelled unexpectedly
        val unitsOnlyLunchServed = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            attendance = entry
        )

        // Lunch cancelled, night served unexpectedly
        val unitsOnlyNightServed = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "CANCELLED",
            nightServiceStatus = "ACTIVE",
            attendance = entry
        )

        // Both cancelled
        val unitsBothCancelled = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "CANCELLED",
            nightServiceStatus = "CANCELLED",
            attendance = entry
        )

        assertEquals("If only lunch is served due to cancellation, eating it counts as 1.0 unit", 1.0, unitsOnlyLunchServed, 0.001)
        assertEquals("If only night is served due to cancellation, eating it counts as 1.0 unit", 1.0, unitsOnlyNightServed, 0.001)
        assertEquals("If both meals are cancelled, units must be 0.0", 0.0, unitsBothCancelled, 0.001)
    }

    @Test
    fun testGuestAttendanceCalculation() {
        // Member attends both meals (2 units) + 2 guests eating lunch only (each 1.5 units) on a normal day
        val entry = MealAttendanceEntry(
            attendedLunch = true,
            attendedNight = true,
            guestLunchAttended = 2,
            guestNightAttended = 0
        )

        val units = MessCalculationEngine.calculateAttendanceUnits(
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            attendance = entry
        )

        // Member units = 2.0; 2 guests * 1.5 units = 3.0 units; Total = 5.0 units
        assertEquals("Host member + 2 lunch guests must equal 5.0 units", 5.0, units, 0.001)
    }

    @Test
    fun testFullMonthSettlementWithAttendanceUnits() {
        val members = listOf(
            Member(id = "m1", displayName = "Rahim", isActive = true),
            Member(id = "m2", displayName = "Karim", isActive = true)
        )

        // Day 1: Normal day (Rahim eats both = 2.0, Karim eats lunch only = 1.5)
        // Day 2: Friday (Rahim eats lunch = 1.0, Karim does not eat = 0.0)
        // Day 3: Cancellation day (night cancelled, both eat lunch = 1.0 each)
        // Total Rahim units = 2.0 + 1.0 + 1.0 = 4.0 units
        // Total Karim units = 1.5 + 0.0 + 1.0 = 2.5 units
        // Grand Total Mess Units = 6.5 units
        val dailyMeals = listOf(
            DailyMealRecord(
                id = "2026-11-01",
                date = "2026-11-01",
                isFriday = false,
                lunchServiceStatus = "ACTIVE",
                nightServiceStatus = "ACTIVE",
                attendances = mapOf(
                    "m1" to MealAttendanceEntry(attendedLunch = true, attendedNight = true),
                    "m2" to MealAttendanceEntry(attendedLunch = true, attendedNight = false)
                )
            ),
            DailyMealRecord(
                id = "2026-11-06",
                date = "2026-11-06",
                isFriday = true,
                lunchServiceStatus = "ACTIVE",
                nightServiceStatus = "CANCELLED",
                attendances = mapOf(
                    "m1" to MealAttendanceEntry(attendedLunch = true, attendedNight = false),
                    "m2" to MealAttendanceEntry(attendedLunch = false, attendedNight = false)
                )
            ),
            DailyMealRecord(
                id = "2026-11-07",
                date = "2026-11-07",
                isFriday = false,
                lunchServiceStatus = "ACTIVE",
                nightServiceStatus = "CANCELLED",
                attendances = mapOf(
                    "m1" to MealAttendanceEntry(attendedLunch = true, attendedNight = false),
                    "m2" to MealAttendanceEntry(attendedLunch = true, attendedNight = false)
                )
            )
        )

        // Total Bazaar = 650.0 (Meal rate = 650.0 / 6.5 = 100.0 / unit)
        val bazaarExpenses = listOf(
            BazaarExpense(id = "b1", amount = 650.0)
        )

        // Total Utility = 200.0 (Split between 2 active members = 100.0 each)
        val utilityBills = listOf(
            UtilityBill(id = "u1", title = "Gas & WiFi", amount = 200.0)
        )

        // Deposits
        val deposits = listOf(
            Deposit(id = "d1", memberId = "m1", amount = 600.0), // Rahim pays 600
            Deposit(id = "d2", memberId = "m2", amount = 300.0)  // Karim pays 300
        )

        val summary = MessCalculationEngine.calculateSummary(
            members = members,
            dailyMeals = dailyMeals,
            bazaarExpenses = bazaarExpenses,
            deposits = deposits,
            utilityBills = utilityBills
        )

        // Assertions
        assertEquals("Total mess units must be 6.5", 6.5, summary.totalUnits, 0.001)
        assertEquals("Total bazaar cost must be 650.0", 650.0, summary.totalBazaarCost, 0.001)
        assertEquals("Meal rate must be 100.0 per unit", 100.0, summary.mealRate, 0.001)
        assertEquals("Total utilities must be 200.0", 200.0, summary.totalUtilities, 0.001)
        assertEquals("Total deposits must be 900.0", 900.0, summary.totalDeposits, 0.001)
        assertEquals("Reserve balance = 900 - 850 = 50.0", 50.0, summary.remainingFund, 0.001)

        val rahim = summary.memberSettlements.first { it.memberId == "m1" }
        assertEquals("Rahim total units = 4.0", 4.0, rahim.totalUnits, 0.001)
        assertEquals("Rahim meal cost = 4.0 * 100 = 400.0", 400.0, rahim.mealCost, 0.001)
        assertEquals("Rahim utility share = 100.0", 100.0, rahim.utilityCost, 0.001)
        assertEquals("Rahim total cost = 500.0", 500.0, rahim.totalCost, 0.001)
        assertEquals("Rahim refund = 600 - 500 = +100.0", 100.0, rahim.netBalance, 0.001)
        assertTrue(rahim.isRefund)

        val karim = summary.memberSettlements.first { it.memberId == "m2" }
        assertEquals("Karim total units = 2.5", 2.5, karim.totalUnits, 0.001)
        assertEquals("Karim meal cost = 2.5 * 100 = 250.0", 250.0, karim.mealCost, 0.001)
        assertEquals("Karim utility share = 100.0", 100.0, karim.utilityCost, 0.001)
        assertEquals("Karim total cost = 350.0", 350.0, karim.totalCost, 0.001)
        assertEquals("Karim due = 300 - 350 = -50.0", -50.0, karim.netBalance, 0.001)
        assertFalse(karim.isRefund)
    }

    @Test
    fun testNormalDayConfirmedBookingUnitsWeightingRules() {
        // A normal day (past date so deadline passed):
        // Member 1: Both booked = 2.0 units
        // Member 2: Lunch only booked = 1.5 units
        // Member 3: Night only booked = 1.0 unit
        // Member 4: Cancelled = 0.0 units
        // Member 5: No response = 0.0 units
        val pastDate = "2026-11-01" // past date

        val (m1Units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED")
        )
        val (m2Units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "CANCELLED")
        )
        val (m3Units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "CANCELLED", statusNight = "BOOKED")
        )
        val (m4Units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "CANCELLED", statusNight = "CANCELLED")
        )
        val (m5Units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "NO_RESPONSE", statusNight = "NO_RESPONSE")
        )

        assertEquals("Both meals booked on normal day must count as 2.0 units", 2.0, m1Units, 0.001)
        assertEquals("Lunch only booked on normal day must count as 1.5 units", 1.5, m2Units, 0.001)
        assertEquals("Night only booked on normal day must count as 1.0 unit", 1.0, m3Units, 0.001)
        assertEquals("Cancelled meals must count as 0.0 units", 0.0, m4Units, 0.001)
        assertEquals("No response meals must count as 0.0 units", 0.0, m5Units, 0.001)
    }

    @Test
    fun testMembersWhoBookMealsButDoNotEatThemAreCharged() {
        // Members who book meals but do not eat them must still be charged because billing is on confirmed bookings
        val pastDate = "2026-11-01"
        val booking = com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED")

        val (units, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = booking
        )

        assertEquals("Booked member must still be charged 2.0 units regardless of eating", 2.0, units, 0.001)
    }

    @Test
    fun testFridayAndSingleServiceDayBookingUnits() {
        val pastFriday = "2026-11-06" // Friday
        val (fridayBooked, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastFriday,
            isFriday = true,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "CANCELLED")
        )
        val (fridayCancelled, _) = MessCalculationEngine.calculateBookingUnits(
            date = pastFriday,
            isFriday = true,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "CANCELLED", statusNight = "CANCELLED")
        )

        assertEquals("Friday lunch booking must count as 1.0 unit", 1.0, fridayBooked, 0.001)
        assertEquals("Friday cancelled booking must count as 0.0 units", 0.0, fridayCancelled, 0.001)

        // Single-service day on a normal weekday due to unexpected cancellation:
        val (singleServedLunch, _) = MessCalculationEngine.calculateBookingUnits(
            date = "2026-11-03",
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "CANCELLED",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED")
        )
        assertEquals("Single-service day (night cancelled) must count as 1.0 unit for booked lunch", 1.0, singleServedLunch, 0.001)
    }

    @Test
    fun testGuestBookingsFollowSameWeightingRulesAndAddedToHostAccount() {
        // Host books both meals (2 units) + 2 guests for lunch only (each 1.5 units) on a normal day
        val pastDate = "2026-11-02"
        val booking = com.example.data.model.MealBookingEntry(
            statusLunch = "BOOKED",
            statusNight = "BOOKED",
            guestLunch = 2,
            guestNight = 0
        )

        val (hostUnits, guestUnits) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = booking
        )

        assertEquals("Host member units = 2.0", 2.0, hostUnits, 0.001)
        assertEquals("2 lunch-only guests * 1.5 units = 3.0 units", 3.0, guestUnits, 0.001)

        // Guest with both meals on normal day = 2.0 units per guest
        val bookingBothGuests = com.example.data.model.MealBookingEntry(
            statusLunch = "BOOKED",
            statusNight = "BOOKED",
            guestLunch = 2,
            guestNight = 2
        )
        val (_, guestBothUnits) = MessCalculationEngine.calculateBookingUnits(
            date = pastDate,
            isFriday = false,
            lunchServiceStatus = "ACTIVE",
            nightServiceStatus = "ACTIVE",
            lunchDeadline = "09:00",
            nightDeadline = "17:00",
            booking = bookingBothGuests
        )
        assertEquals("2 guests with both meals = 2 * 2.0 = 4.0 units", 4.0, guestBothUnits, 0.001)
    }

    @Test
    fun testAdminOverrideBookingUpdatesFinancialCalculations() {
        val members = listOf(
            Member(id = "m1", displayName = "Rahim", isActive = true),
            Member(id = "m2", displayName = "Karim", isActive = true)
        )
        val bazaar = listOf(BazaarExpense(id = "b1", amount = 700.0))
        val deposits = listOf(
            Deposit(id = "d1", memberId = "m1", amount = 500.0),
            Deposit(id = "d2", memberId = "m2", amount = 500.0)
        )
        val chefFee = listOf(UtilityBill(id = "u1", title = "Chef Fee", amount = 200.0))

        // Initial: Karim had cancelled on 2026-11-02
        val initialMeals = listOf(
            DailyMealRecord(
                id = "2026-11-02",
                date = "2026-11-02",
                bookings = mapOf(
                    "m1" to com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED"),
                    "m2" to com.example.data.model.MealBookingEntry(statusLunch = "CANCELLED", statusNight = "CANCELLED")
                )
            )
        )
        val initialSummary = MessCalculationEngine.calculateSummary(
            members = members,
            dailyMeals = initialMeals,
            bazaarExpenses = bazaar,
            deposits = deposits,
            utilityBills = chefFee
        )

        assertEquals("Initially Rahim = 2.0 units", 2.0, initialSummary.memberSettlements.first { it.memberId == "m1" }.totalUnits, 0.001)
        assertEquals("Initially Karim = 0.0 units", 0.0, initialSummary.memberSettlements.first { it.memberId == "m2" }.totalUnits, 0.001)
        assertEquals("Initial total mess units = 2.0", 2.0, initialSummary.totalUnits, 0.001)
        assertEquals("Initial meal rate = 700 / 2 = 350.0", 350.0, initialSummary.mealRate, 0.001)

        // Admin overrides Karim's booking to BOOKED for both meals with override reason
        val overriddenMeals = listOf(
            DailyMealRecord(
                id = "2026-11-02",
                date = "2026-11-02",
                bookings = mapOf(
                    "m1" to com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED"),
                    "m2" to com.example.data.model.MealBookingEntry(
                        statusLunch = "BOOKED",
                        statusNight = "BOOKED",
                        managerOverrideReason = "Member informed manager via phone call before cooking"
                    )
                )
            )
        )
        val updatedSummary = MessCalculationEngine.calculateSummary(
            members = members,
            dailyMeals = overriddenMeals,
            bazaarExpenses = bazaar,
            deposits = deposits,
            utilityBills = chefFee
        )

        assertEquals("After override Karim = 2.0 units", 2.0, updatedSummary.memberSettlements.first { it.memberId == "m2" }.totalUnits, 0.001)
        assertEquals("Total mess units increased to 4.0", 4.0, updatedSummary.totalUnits, 0.001)
        assertEquals("Meal rate automatically recalculated to 700 / 4 = 175.0", 175.0, updatedSummary.mealRate, 0.001)

        val karimSettlement = updatedSummary.memberSettlements.first { it.memberId == "m2" }
        assertEquals("Karim meal cost = 2.0 * 175 = 350.0", 350.0, karimSettlement.mealCost, 0.001)
        assertEquals("Chef fee share = 100.0 (kept separate)", 100.0, karimSettlement.utilityCost, 0.001)
        assertEquals("Karim total cost = 350 + 100 = 450.0", 450.0, karimSettlement.totalCost, 0.001)
        assertEquals("Karim net balance = 500 deposit - 450 = +50.0 refund", 50.0, karimSettlement.netBalance, 0.001)
        assertTrue(karimSettlement.isRefund)
    }

    @Test
    fun testChefFeesAndOtherExpenseAllocationsSeparation() {
        val members = listOf(
            Member(id = "m1", displayName = "Rahim", isActive = true),
            Member(id = "m2", displayName = "Karim", isActive = true)
        )
        val bazaar = listOf(BazaarExpense(id = "b1", amount = 800.0))
        val deposits = listOf(
            Deposit(id = "d1", memberId = "m1", amount = 600.0),
            Deposit(id = "d2", memberId = "m2", amount = 600.0)
        )
        val utilities = listOf(
            UtilityBill(id = "u1", title = "Chef Fee", amount = 300.0),
            UtilityBill(id = "u2", title = "Gas Bill", amount = 100.0)
        )
        val dailyMeals = listOf(
            DailyMealRecord(
                id = "2026-11-01",
                date = "2026-11-01",
                bookings = mapOf(
                    "m1" to com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED"), // 2.0
                    "m2" to com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "CANCELLED") // 1.5
                )
            )
        )

        val summary = MessCalculationEngine.calculateSummary(
            members = members,
            dailyMeals = dailyMeals,
            bazaarExpenses = bazaar,
            deposits = deposits,
            utilityBills = utilities
        )

        // Total mess units = 3.5
        // Meal rate = 800 / 3.5 = 228.57
        // Utilities = 400 total / 2 = 200 per head
        assertEquals("Total utilities must be 400.0", 400.0, summary.totalUtilities, 0.001)
        val rahim = summary.memberSettlements.first { it.memberId == "m1" }
        assertEquals("Rahim utility share = 200.0", 200.0, rahim.utilityCost, 0.001)
        assertEquals("Rahim meal cost = 2.0 * 228.57 = 457.14", 457.14, rahim.mealCost, 0.01)
        assertEquals("Rahim total cost = 457.14 + 200.0 = 657.14", 657.14, rahim.totalCost, 0.01)
    }

    @Test
    fun testHistoricalAttendancePreservedAlongsideNewConfirmedBookings() {
        // A month where:
        // Day 1 (Nov 1): Historical record that has attendances confirmed under old workflow
        // Day 2 (Nov 2): New record that has confirmed bookings under new workflow
        val members = listOf(
            Member(id = "m1", displayName = "Rahim", isActive = true)
        )
        val bazaar = listOf(BazaarExpense(id = "b1", amount = 400.0))
        val deposits = listOf(Deposit(id = "d1", memberId = "m1", amount = 500.0))

        val dailyMeals = listOf(
            DailyMealRecord(
                id = "2026-11-01",
                date = "2026-11-01",
                // Has historical attendance
                attendances = mapOf(
                    "m1" to MealAttendanceEntry(attendedLunch = true, attendedNight = false) // 1.5 units
                )
            ),
            DailyMealRecord(
                id = "2026-11-02",
                date = "2026-11-02",
                // Has confirmed booking
                bookings = mapOf(
                    "m1" to com.example.data.model.MealBookingEntry(statusLunch = "BOOKED", statusNight = "BOOKED") // 2.0 units
                )
            )
        )

        val summary = MessCalculationEngine.calculateSummary(
            members = members,
            dailyMeals = dailyMeals,
            bazaarExpenses = bazaar,
            deposits = deposits,
            utilityBills = emptyList()
        )

        // Day 1 attendance (1.5) + Day 2 booking (2.0) = 3.5 units
        assertEquals("Combined historical attendance + confirmed booking units = 3.5", 3.5, summary.totalUnits, 0.001)
        assertEquals("Meal rate = 400 / 3.5 = 114.29", 114.29, summary.mealRate, 0.01)
    }
}
