const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_master_1";
const ADMIN_EMAIL = "tddas111@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validBirthdayPayload(userId) {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    name: "Kamalesh Vishwakarma",
    birthDate: "01-01-1980",
    birthDay: 1,
    birthMonth: 1,
    birthYear: 1980,
    monthName: "January",
    relationship: "Colleague",
    phone: "+919876543210",
    autoMessageTemplate: "Wishing you a very Happy Birthday, Kamalesh!",
    autoSendEnabled: true,
    notifyAllUsers: true,
    lastWishedYear: 0,
    createdAt: now,
    updatedAt: now,
  };
}

function validGreetingPayload(userId) {
  const now = new Date(Date.now() - 1000);
  return {
    userId,
    birthdayId: "bday_1",
    recipientName: "Kamalesh Vishwakarma",
    senderName: "Sayan Banerjee",
    message: "Happy Birthday Kamalesh! Have a wonderful year ahead!",
    cardTheme: "confetti_gold",
    deliveryChannel: "personalized_wall",
    likesCount: 1,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or create birthdays", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("birthdays").get());
  await assertFails(
    unauthDb.collection("birthdays").doc("bday_1").set(validBirthdayPayload(ALICE_UID))
  );
});

test("Authenticated user: can create and query own birthdays", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("birthdays").doc("bday_1").set(validBirthdayPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("birthdays").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated non-admin user: cannot read or update another user's birthday", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("birthdays").doc("bday_alice").set(validBirthdayPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("birthdays").doc("bday_alice").get());
  await assertFails(
    bobDb.collection("birthdays").doc("bday_alice").update({
      name: "Hacked Name",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Bootstrapped Admin (verified email): can manage master records and /admins", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("birthdays").doc("bday_alice").set(validBirthdayPayload(ALICE_UID))
  );

  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true })
    .firestore();

  await assertSucceeds(adminDb.collection("birthdays").doc("bday_alice").get());
  await assertSucceeds(
    adminDb.collection("birthdays").doc("bday_alice").update({
      name: "Kamalesh V. (Verified by Admin)",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    adminDb.collection("admins").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      email: ADMIN_EMAIL,
      role: "master_admin",
      createdAt: new Date(Date.now() - 1000),
    })
  );
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("birthdays").doc("bday_alice").set(validBirthdayPayload(ALICE_UID))
  );

  const spoofAdminDb = testEnv
    .authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false })
    .firestore();

  await assertFails(spoofAdminDb.collection("birthdays").doc("bday_alice").get());
  await assertFails(
    spoofAdminDb.collection("admins").doc("spoof_uid").set({
      userId: "spoof_uid",
      email: ADMIN_EMAIL,
      role: "master_admin",
      createdAt: new Date(Date.now() - 1000),
    })
  );
});

test("Shadow update test: rejects ghost fields on birthday and greeting", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("birthdays").doc("bday_ghost").set({
      ...validBirthdayPayload(ALICE_UID),
      isAdmin: true,
    })
  );
  await assertFails(
    aliceDb.collection("greetings").doc("greet_ghost").set({
      ...validGreetingPayload(ALICE_UID),
      isVerified: true,
    })
  );
});

test("Authenticated user: can create, update likesCount, and query own greetings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("greetings").doc("greet_1").set(validGreetingPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("greetings").doc("greet_1").update({
      likesCount: 5,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    aliceDb.collection("greetings").where("userId", "==", ALICE_UID).get()
  );
});

test("Query enforcer: non-admin fails query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("birthdays").get());
  await assertFails(aliceDb.collection("greetings").get());
});
