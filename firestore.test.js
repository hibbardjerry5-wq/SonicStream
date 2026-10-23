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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

// --- SECURITY TESTS ---

test("Unauthenticated user: cannot read playlists", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("playlists").get());
});

test("Authenticated user: cannot read another user's playlists", async () => {
  // Seed Alice playlist via admin context
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1").set({
      userId: ALICE_UID,
      name: "Alice Beats",
      trackIds: ["track_1"],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("playlists").get());
});

test("Authenticated owner: can create and read own playlist", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const playlistRef = aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_1");

  await assertSucceeds(
    playlistRef.set({
      userId: ALICE_UID,
      name: "Cyber Chill",
      description: "Atmospheric synth tracks",
      colorHex: "#00E5FF",
      trackIds: ["track_1", "track_2"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(playlistRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("playlists").get());
});

test("Authenticated owner: cannot create playlist with spoofed userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const playlistRef = aliceDb.collection("users").doc(ALICE_UID).collection("playlists").doc("pl_spoof");

  await assertFails(
    playlistRef.set({
      userId: BOB_UID, // Spoofed UID
      name: "Spoofed",
      trackIds: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated owner: can update user settings with valid fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const settingsRef = aliceDb.collection("users").doc(ALICE_UID).collection("user_settings").doc("audio_settings");

  await assertSucceeds(
    settingsRef.set({
      userId: ALICE_UID,
      equalizerBands: [3, 1, 0, 2, 4],
      bassBoost: 600,
      virtualizer: 400,
      activePreset: "Bass Boost",
      visualizerMode: "Spectrum Bars",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    settingsRef.update({
      bassBoost: 800,
      activePreset: "Custom",
      updatedAt: new Date(),
    })
  );
});
