package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BirthdayContact
import com.example.data.model.GreetingPost
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BirthdayRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveBirthday_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BirthdayRepository(firestore, auth)

        val sampleContact = BirthdayContact(
            userId = aliceUid,
            name = "Kamalesh Vishwakarma",
            birthDate = "01-01-1980",
            birthDay = 1L,
            birthMonth = 1L,
            birthYear = 1980L,
            monthName = "January",
            relationship = "Colleague",
            phone = "+919830110001",
            autoMessageTemplate = "Happy Birthday Kamalesh!",
            autoSendEnabled = true,
            notifyAllUsers = true,
            lastWishedYear = 0L
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBirthday(sampleContact)
        }
        assertTrue(createResult.isSuccess)
        val createdId = createResult.getOrThrow()
        assertTrue(createdId.isNotEmpty())

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBirthdays(aliceUid).first { list -> list.any { it.id == createdId } }
        }
        val fetched = emittedList.first { it.id == createdId }
        assertEquals("Kamalesh Vishwakarma", fetched.name)
        assertNotNull(fetched.createdAt)
    }

    @Test
    fun createAndCheerGreeting_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BirthdayRepository(firestore, auth)

        val greeting = GreetingPost(
            userId = aliceUid,
            birthdayId = "bday_test_1",
            recipientName = "Sayan Banerjee",
            senderName = "Team Pulse",
            message = "Happy Birthday Sayan! Have a wonderful year ahead!",
            cardTheme = "confetti_gold",
            deliveryChannel = "personalized_wall",
            likesCount = 1L
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createGreeting(greeting)
        }
        assertTrue(createResult.isSuccess)
        val greetingId = createResult.getOrThrow()

        val emittedGreetings = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeGreetings(aliceUid).first { list -> list.any { it.id == greetingId } }
        }
        val savedGreeting = emittedGreetings.first { it.id == greetingId }

        val cheerResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.cheerGreeting(savedGreeting)
        }
        assertTrue(cheerResult.isSuccess)
    }

    @Test
    fun getBirthdayById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BirthdayRepository(firestore, auth)
        val bdayId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createBirthday(
                BirthdayContact(
                    userId = aliceUid,
                    name = "Sanju Ghosh",
                    birthDate = "04-02-1998",
                    birthDay = 4L,
                    birthMonth = 2L,
                    birthYear = 1998L,
                    monthName = "February",
                    relationship = "Colleague",
                    phone = "+919830120004",
                    autoMessageTemplate = "Happy Birthday Sanju!",
                    autoSendEnabled = true,
                    notifyAllUsers = true
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = BirthdayRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBirthdayById(bdayId)
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(exception)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun observeBirthdays_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = BirthdayRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeBirthdays("unauthenticated_uid").first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (t: Throwable) {
            val firestoreException = generateSequence(t) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertNotNull("Expected FirebaseFirestoreException in cause chain, got $t", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@birthdaypulse.test"
        const val BOB_EMAIL = "bob@birthdaypulse.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
