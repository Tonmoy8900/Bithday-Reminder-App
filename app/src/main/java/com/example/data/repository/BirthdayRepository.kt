package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BirthdayContact
import com.example.data.model.GreetingPost
import com.example.data.model.PreloadedRoster
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BirthdayRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeBirthdays(userId: String): Flow<List<BirthdayContact>> = callbackFlow {
        val path = COLLECTION_BIRTHDAYS
        val registration = db.collection(COLLECTION_BIRTHDAYS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(
                            BirthdayContact::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    fun observeGreetings(userId: String): Flow<List<GreetingPost>> = callbackFlow {
        val path = COLLECTION_GREETINGS
        val registration = db.collection(COLLECTION_GREETINGS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(
                            GreetingPost::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                    trySend(items)
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun getBirthdayById(birthdayId: String): Result<BirthdayContact?> = runCatching {
        val docRef = db.collection(COLLECTION_BIRTHDAYS).document(birthdayId)
        try {
            val snap = docRef.get().await()
            snap.toObject(
                BirthdayContact::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createBirthday(contact: BirthdayContact): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = contact.id.ifBlank { "bday_${UUID.randomUUID().toString().replace("-", "")}" }
        val docRef = db.collection(COLLECTION_BIRTHDAYS).document(docId)
        val payload = contact.copy(
            id = docId,
            userId = uid,
            createdAt = null,
            updatedAt = null
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateBirthday(contact: BirthdayContact): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(contact.id.isNotBlank()) { "Birthday ID must not be blank for update" }
        requireNotNull(contact.createdAt) { "Birthday createdAt must not be null for update" }
        val docRef = db.collection(COLLECTION_BIRTHDAYS).document(contact.id)
        try {
            docRef.set(
                contact.copy(
                    userId = uid,
                    updatedAt = null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBirthday(birthdayId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection(COLLECTION_BIRTHDAYS).document(birthdayId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createGreeting(greeting: GreetingPost): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = greeting.id.ifBlank { "greet_${UUID.randomUUID().toString().replace("-", "")}" }
        val docRef = db.collection(COLLECTION_GREETINGS).document(docId)
        val payload = greeting.copy(
            id = docId,
            userId = uid,
            createdAt = null,
            updatedAt = null
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun cheerGreeting(greeting: GreetingPost): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(greeting.id.isNotBlank()) { "Greeting ID must not be blank for update" }
        requireNotNull(greeting.createdAt) { "Greeting createdAt must not be null for update" }
        val docRef = db.collection(COLLECTION_GREETINGS).document(greeting.id)
        try {
            docRef.set(
                greeting.copy(
                    userId = uid,
                    likesCount = (greeting.likesCount + 1L).coerceAtMost(100000L),
                    updatedAt = null
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteGreeting(greetingId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection(COLLECTION_GREETINGS).document(greetingId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun seedEmployeeRosterIfEmpty(): Result<Int> = runCatching {
        val uid = requireUserId()
        val existingSnap = try {
            db.collection(COLLECTION_BIRTHDAYS)
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, COLLECTION_BIRTHDAYS)
            throw e
        }

        if (!existingSnap.isEmpty) {
            return@runCatching 0
        }

        seedEmployeeRosterForce().getOrThrow()
    }

    suspend fun seedEmployeeRosterForce(): Result<Int> = runCatching {
        val uid = requireUserId()
        val existingSnap = try {
            db.collection(COLLECTION_BIRTHDAYS)
                .whereEqualTo("userId", uid)
                .get()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, COLLECTION_BIRTHDAYS)
            throw e
        }

        val existingNames = existingSnap.documents.mapNotNull { it.getString("name")?.lowercase() }.toSet()
        val defaults = PreloadedRoster.defaultEmployeeRoster(uid)
        var addedCount = 0

        for (contact in defaults) {
            if (contact.name.lowercase() !in existingNames) {
                createBirthday(contact).getOrThrow()
                addedCount++
            }
        }
        addedCount
    }

    companion object {
        const val COLLECTION_BIRTHDAYS = "birthdays"
        const val COLLECTION_GREETINGS = "greetings"
    }
}
