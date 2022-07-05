package cwlib.enums;

public class Revisions {
    // Base revisions

    public static final int DEPENDENCIES = 0x109; // Added self describing depenency table to resources
    public static final int SLOT_GROUPS = 0x134; // Added primaryLinkGroup to Slot
    public static final int SLOT_AUTHOR_NAME = 0x13b; // Added authorName to Slot
    public static final int REF_STRIP_CHILDREN = 0x13d; // Added stripChildren to PRef
    public static final int WORLD_IS_PAUSED = 0x14a; // Added isPaused to PWorld
    public static final int WORLD_COLOR_CORRECTION = 0x152; // Added lighting and color correction factor to PWorld
    public static final int EGGLINK_DETAILS = 0x160; // Reference CInventoryItemDetails in CEggLink for item details
    public static final int DETAILS_SOUND_GUID = 0x14f; // highlightSound uses GUID instead of string in InventoryItemDetails
    public static final int DETAILS_COLOR = 0x157; // color added to InventoryItemDetails
    public static final int DECAL_METADATA = 0x158; // added type, metadataIdx, numMetadata to decal
    public static final int GFXMATERIAL_SOUND_ENUM = 0x15b; // Added soundEnum to RGfxMaterial
    public static final int ADD_EYETOY_DATA = 0x15e; // Added EyetoyData
    public static final int DETAILS_EYETOY_DATA = 0x162; // eyetoyData added to InventoryItemDetails
    public static final int AUDIOWORLD_IS_LOCAL = 0x165; // Added isLocal to PAudioWorld
    public static final int MATERIAL_EXPLOSIONS = 0x167; // Added explosion and breakable fields to RMaterial
    public static final int MATERIAL_ANGULAR_EXPLOSIONS = 0x168; // Added explosionMaxAngVel to RMaterial
    public static final int UNLOCKS_REWARDS = 0x16f; // Added unlocks and rewards to PWorld
    public static final int DETAILS_SINGLE_TRANSLATION_TAG = 0x174; // replace nameTranslationTag/descTranslationTag with translationTag
    public static final int DETAILS_LEVEL_UNLOCK = 0x177; // add levelUnlockSlotID to InventoryItemDetails
    public static final int MESH_TEXTURE_ALTERNATIVES = 0x179; // textureAlternatives added to CPrimitive
    public static final int DETAILS_PHOTO_DATA = 0x182; // photoData, copyright, and creator added to InventoryItemDetails
    public static final int SLOT_TRANSLATION_TAG = 0x183; // Added translationTag to slot
    public static final int COMPRESSED_RESOURCES = 0x189; // Added optional compressed field to resources
    public static final int YELLOWHEAD_ON_SCREEN_COUNTER = 0x194; // Added onScreenCounter to PYellowHead
    public static final int DETAILS_PRIMARY_INDEX = 0x195; // Added primaryIndex to InventoryItemDetails
    public static final int WORLD_FOG_FACTOR = 0x196; // Added fog/darkness settings to PWorld
    public static final int PLAN_DETAILS = 0x197; // Plan details added to RPlan, removed CInventoryItemDetails from EggLink, reference RPlan instead of CGlobalThingPtr, toolType added to InventoryItemDetails
    public static final int AUDIOWORLD_VISIBILITY = 0x198; // Add hideInPlayMode to PAudioWorld
    public static final int COSTUME_ORIGINAL_PLAN = 0x19a; // added originalPlan to CostumePiece
    public static final int TRIGGER_HYSTERESIS = 0x19b; // add hysteresisMultplier and enabled to PTrigger
    public static final int ENEMY_SNAP = 0x19f; // Add snapVertex to PEnemy
    public static final int DYNAMIC_SWITCHES = 0x1a5; // added type to PSwitch, switches now carry activation
    public static final int CHECKPOINT_SPAWNS_LEFT = 0x1a7; // added spawnsLeft to PCheckpoint
    public static final int ENEMY_ANIMATION = 0x1a9; // centerOffset, animThing, and animSpeed added to PEnemy
    public static final int DETAILS_USER_DETAILS = 0x1ab; // userCreatedDetails added to InventoryItemDetails
    public static final int DETAILS_CREATION_HISTORY = 0x1b1; // added creationHistory to InventoryItemDetails
    public static final int SLOT_PROGRESSION = 0x1b9; // added gameProgressionState to slot
    public static final int SWITCHKEY_VISIBILITY = 0x1bd; // added hideInPlayMode to PSwitchKey
    public static final int DETAILS_USES = 0x1c1; // added numUses and lastUsed to InventoryItemDetails
    public static final int WORLD_DEATH_COUNT = 0x1c2; // Added deathCount to PWorld
    public static final int WORLD_MAX_PLAYERS = 0x1c4; // Added maxNumPlayers to PWorld
    public static final int THING_EMITTER = 0x1c7; // added emitter to thing
    public static final int EMITTER_MAX_EMITTED = 0x1c8; // Added maxEmittedAtOnce to PEmitter
    public static final int REF_ALIVE_FRAMES = 0x1c9; // Added oldAliveFrames to PRef
    public static final int TRIGGER_ZLAYERS = 0x1d5; // added allZLayers to PTrigger
    public static final int WORLD_DISSOLVING_THINGS = 0x1db; // Added dissolving things to PWorld
    public static final int WORLD_TUTORIAL_LEVEL = 0x1de; // added isTutorialLevel to PWorld
    public static final int SLOT_LEVEL_TYPE = 0x1df; // Added developerLevelType to slot
    public static final int SCRIPT_MODIFIERS = 0x1e5; // Added modifiers to RScript
    public static final int SHARED_SCRIPT_DATA = 0x1ec; // replace all inline script fields with shared pools, and dependingGUIDs in RScript
    public static final int RENDERMESH_PARENT_DISTANCE = 0x1f6; // added parentDistanceFront/Side to PRenderMesh
    public static final int DETAILS_ALLOW_EMIT = 0x205; // add allowEmit flag to InventoryItemDetails
    public static final int WORLD_SHAREABLE_REWARDS = 0x208; // added areRewardsShareable to PWorld
    public static final int THING_CREATED_PLAYER = 0x214; // added createdBy and changedBy to thing, placedBy to decal
    public static final int DECAL_PLAYMODE_FRAME = 0x215; // added playmodeFrame to decal
    public static final int DECAL_SCORCH_MARK = 0x219; // add scrorchMark to decal
    public static final int THING_STAMPING = 0x21b; // isStamping added to thing
    public static final int DETAILS_DATE_ADDED = 0x222; // add dateAdded to InventoryItemDetails
    public static final int DETAILS_SHAREABLE = 0x223; // add shareable flag to InventoryItemDetails
    public static final int SHAPE_MASS_DEPTH = 0x226; // added massDepth to PShape
    public static final int WORLD_SPAWN_FAILURE = 0x22e; // Added everSpawned and spawnFailureCount to PWorld
    public static final int DETAILS_RELAYOUT_LBP1 = 0x234; // Re-arrange layout of InventoryItemDetails
    public static final int ITEM_FLAGS = 0x235; // Added UID, flags to InventoryItem, add back pad to InventoryItemDetails
    public static final int SLOT_DESCRIPTOR = 0x238; // Added shareable and backgroundGUID to Slot
    public static final int MESH_MINMAX_UV = 0x239; // Added min/maxUV and areaScaleFactor to RMesh
    public static final int EGGLINK_SHAREABLE = 0x23c; // Adds shareable field to CEggLink
    public static final int MATERIAL_BULLET = 0x244; // Adds bullet ro RMaterial
    public static final int THING_PLAN_GUID = 0x254; // Adds planGUID to thing
    public static final int GENERATEDMESH_PLAN_GUID = 0x258; // Add planGUID to PGeneratedMesh
    public static final int WORLD_GLOBAL_SETTINGS = 0x25a; // Added global settings fields to PWorld
    public static final int PART_PLAN_GUIDS = 0x25b; // Plan GUIDs added to various parts
    public static final int DECAL_COLOR = 0x260; // add packed color to decal
    public static final int ENEMY_WALK_CONSTRAINT = 0x265; // newWalkConstraintMass added to PEnemy
    public static final int GROUP_EMITTER = 0x267; // emitter, lifetime, aliveFrames added to PGroup
    public static final int GROUP_PICKUP_ALL_MEMBERS = 0x26e; // added pickupAllMembers to PGroup
    public static final int WATER_LEVEL = 0x26f; // Addede waterLevel and targetWaterLevel to PWorld
    public static final int WATER_MAGNITUDE = 0x270; // Added water wave magnitude fields to PWorld
    public static final int BRANCHES = 0x271; // Added branch descriptions to resources
    public static final int AIRTIME = 0x272; // airTimeLeft added to PCreature
    
    public static final int LBP1_MAX = 0x272; // last revision for lbp1
    
    // Deploy/LBP2 revisions

    public static final int ARCADE = 0x273; // Robo boots (wall jump), custom guns, recordings, logic
    public static final int INPUT_UPDATE_TYPE = 0x274; // added updateType to PSwitchInput
    public static final int INPUT_LETHAL_TYPE = 0x275; // added lethalType to PSwitchInput
    public static final int MATERIAL_CIRCUITBOARD = 0x27b; // Add circuitboard and disableCSG to RMaterial
    public static final int GENERATEDMESH_VISIBILITY = 0x27c; // add visible to PGeneratedMesh
    public static final int THING_SWAP_UID_PARENT = 0x27f; // swap thing UID and thing parent fields in serialization
    public static final int INPUT_RIGID_CONNECTORS = 0x288; // added includeRigidConnectors to PSwitchInput
    public static final int SWITCHKEY_TYPE = 0x27d; // added type to PSwitchKey
    public static final int MICROCHIP_VISIBILITY = 0x283; // Added hideInPlayMode to PMicrochip
    public static final int SWITCHINPUT_VISIBILITY = 0x297; // Added hideInPlayMode to PSwitchInput
    public static final int THING_TEST_MARKER = 0x2a1; // Adds test serialization marker to CThing
    public static final int PARAMETER_ANIMATIONS = 0x2a2; // Added parameter animations to RGfxMaterial
    public static final int PLAYER_SIGNAL = 0x2a3; // Added gameMode to PWorld, playerNumberColor to PShape, player to CSwitchSignal
    public static final int RESPONSIBLE_PLAYER = 0x2a5; // responsiblePlayer/frames added to PCreature
    public static final int WORLD_SACKBOT_NUMBER = 0x2b0; // added nextSackbotPlayerNumber to PWorld
    public static final int WORLD_WATER_BITS = 0x2b4; // Add water bits to PWorld
    public static final int SHAPE_FLAGS = 0x2b5; // Add flags to PShape
    public static final int MATERIAL_PROPERTIES = 0x2b7; // Restitution, single grab, and sticky added to PMaterialTweak
    public static final int MICROCHIP_WIRE_VISIBILITY= 0x2b8; // Adds wiresVisible to PMicrochip
    public static final int MATERIAL_NO_AUTO_DESTRUCT = 0x2b9; // Added noAutoDestruct to PMaterialTweak
    public static final int MOTION_CONTROLLER_ZONE = 0x2ba; // Add motionControllerZone to PCameraTweak
    public static final int LAMS_KEYS = 0x2bb; // Use LAMS keys instead of translation tags
    public static final int SWITCH_BEHAVIOR = 0x2c4; // Randomiser added, SwitchInput no longer carries signals, behavior added to various structures
    public static final int TEMP_COSTUME = 0x2c5; // Adds costumePieceVec[COSTUME_TEMP_START] to PCostume
    public static final int REMOVE_GAME_UPDATE_FRAME = 0x2c6; // Remove Game.LastUpdateFrame from PEmitter
    public static final int SHOW_THERMOMETER = 0x2d3; // showThermometer added to RLocalProfile
    public static final int GAMEPLAY_DATA_TYPE = 0x2da; // added gameplayType to PGameplayData
    public static final int SWITCH_KEYNAME = 0x2dc; // name added to switchKey and switch 
    public static final int WORLD_SIM_FRAME = 0x2e2; // added simFrame to world
    public static final int RENDERMESH_VISIBILITY_FLAGS = 0x2e3; // switch visible to visibilityFlags in PRenderMesh
    public static final int MICROCHIP_LAST_TOUCHED = 0x2e4; // Last touched added to PMicroChip
    public static final int DIRECT_CONTROL = 0x2e5; // directControlPrompt added to PCreature
    public static final int MICROCHIP_OFFSET = 0x2e9; // Circuitboard board offset added to PMicroChip
    public static final int SLOT_COLLECTABUBBLES_REQUIRED = 0x2ea; // added collectabubblesRequired to slot
    public static final int WORLD_CUTSCENE_MANAGER = 0x2ee; // added cutSceneCameraManager to PWorld
    public static final int THING_HIDDEN = 0x2f2; // added hidden to thing
    public static final int SLOT_COLLECTABUBBLES_CONTAINED = 0x2f4; // added collectabubblesContained to slot
    public static final int GFXMATERIAL_ALPHA_MODE = 0x2fa; // Added alpha mode to RGfxMaterial
    public static final int SCRIPT_S64_TABLE = 0x30b; // Added S64 constants table to RScript
    public static final int WORLD_AUDIO_SETTINGS = 0x30c; // added globalAudioSettings to PWorld
    public static final int SIGNAL_TERNARY = 0x310; // Added ternary to switch signal
    public static final int ENEMY_SMOKE_COLOR = 0x31e; // smokeColor added to PEnemy
    public static final int GUN_MODIFIERS = 0x320; // added fireRate, gunAccuracy, bulletEmitOffset/Rotation, gunThing, gunTrigger, and lastGunTriggerUID
    public static final int PALETTE_CONVERTED_PLANS = 0x323; // added convertedPlans to RPalette
    public static final int LEVEL_SETTINGS_AO = 0x325; // added shadow settings and dofNear to PLevelSettings
    public static final int LEVEL_SETTINGS_DOF_FAR = 0x326; // added DofFar to PLevelSettings
    public static final int DROP_POWERUPS = 0x32c; // canDropPowerup added to PCreature, randy seed also removed from pworld?
    public static final int LEVEL_SETTINGS_ZEFFECT = 0x331; // zEffect fields added to PLevelSettings
    public static final int TRIGGER_ZOFFSET = 0x332; // add zOffset to PTrigger
    public static final int PLANET_DECORATIONS = 0x333; // Add planetDecorations to slot
    public static final int DETAILS_FLAGS = 0x335; // flags added to InventoryItemDetails
    public static final int REMOVE_UPDATED_SCRIPT = 0x33a; // removed upToDateScript from RScript
    public static final int PIN_FLAGS = 0x33b; // pinFlags added to PinsAwarded profile data
    public static final int SLOT_LABELS = 0x33c; // Added labels to slot
    public static final int THING_FLAGS = 0x341; // added flags to thing, remove localPosition from PPos
    public static final int REMOVE_WORLD_TUTORIAL_LEVEL = 0x344; // isTutoriallevel removed from PWorld
    public static final int USER_CATEGORIES = 0x34a; // userCategoryIndex added to InventoryItem
    public static final int WEEKDAYS_PLAYED_PIN = 0x34b; // added weekdaysPlayedBits to PinsAwarded
    public static final int DECAL_PLAN_DESCRIPTOR = 0x34c; // replace decal planGUID with descriptor
    public static final int COMPACT_MICROCHIP = 0x34d; // name, compactComponents, and circuitBoardSizeX/Y added to PMicrochip
    public static final int WORLD_WATER_DRAIN = 0x34e; // Added water drain sounds enabled fields to PWorld
    public static final int SLOT_SUBLEVEL = 0x352; // isSubLevel added to slot
    public static final int SLOT_SCOREBOARD_LINK = 0x35e; // added scoreboardLevelLinkSlot to PWorld
    public static final int REMOVE_OLD_LBP1_FIELDS = 0x36c; // Removed tutorialLevel and tutorialVideo from InventoryItem, removed gameProgressionState from slot 
    public static final int WORLD_CAMERA_SETTINGS = 0x370; // Added camera settings to PWorld
    public static final int WORLD_FRAME_START = 0x377; // add frameLevelStarted to PWorld
    public static final int DETAILS_RELAYOUT_LBP2 = 0x37d; // NetworkOnlineID and NetworkPlayerID are written as fixed length buffers, InventoryItemDetails has been remade again
    public static final int PROFILE_PINS = 0x385; // Add profile pin data
    public static final int ENEMY_SMOKE_BRIGHTNESS = 0x39a; // smokeBrightness added to PEnemy
    public static final int EYETOY_OUTLINE = 0x3a0; // outline added to EyetoyDATA
    public static final int WORLD_NIGHTDAY_SWAP = 0x3a3; // added lbp2NightDaySwapped to PWorld
    public static final int PRODUCTION_BUILD = 0x3b6; // fromProductionBuild added to resources containing slot lists.
    public static final int PAINTING_PHOTO_DATA = 0x3c8; // painting added to InventoryItemPhotoData
    public static final int SLOT_EXTRA_METADATA = 0x3d0; // min/maxPlayers, moveRecommended, showOnPlanet, and livesOverride added to slot
    public static final int PTG_USE_DEFAULT_BACKGROUND = 0x3e0; // add useDefaultBackground to PaintProperties
    public static final int LEVEL_CROSS_DEPENDENCIES = 0x3e6; // Added Vita cross dependency hashes to RLevel
    public static final int SLOT_CROSS_COMPATIBLE = 0x3e9; // crossCompatible added to slot
    public static final int PROFILE_CROSS_DEPENDENCIES = 0x3eb; // Added Vita cross dependency hashes to RBigProfile
    public static final int DATALABELS = 0x3ef;  // Creator data labels added to RBigProfile
    
    public static final int LBP2_MAX = 0x3f8; // last revision for lbp2, technically 0x3f9, but no resource exists with this until lbp3

    // Leerdammmer revisions

    public static final int LD_HEAD = 0x272; // Head revision for Leerdammer branch

    public static final int LD_WATER_SETTINGS = 0x0; // Adds scuba gear and water related tweak settings
    public static final int LD_WATER_TINT = 0x1; // Adds water tint and murkiness to PWorld
    public static final int LD_RESOURCES = 0x2; // Remove's depreciated CValue from PMetaData, adds compressionFlags to Resource's, adds compressed parts to CThing
    public static final int LD_SAVEGAME_INFO = 0x3; // Add showSaveGameInfo and totalLevelCount to RLocalProfile
    public static final int LD_WATER_WAVE = 0x4; // Adds Game.CurrWavePos and fromWaterLevel to PWorld
    public static final int LD_TEST_MARKER = 0x5; // Adds test serialization marker to CThing
    // 0x4c44:0x0006
    public static final int LD_WATER_BITS = 0x7; // Add water bits to PWorld
    public static final int LD_LAMS_KEYS = 0x8; // Use LAMS keys instead of translation tags
    // 0x4c44::0x0009
    // 0x4c44:0x000a
    public static final int LD_TEMP_COSTUME = 0xb; // Adds costumePieceVec[COSTUME_TEMP_START] to PCostume
    public static final int LD_REMOVED_BREATHED = 0xc; // Removes hasBreathedAir from PCreature
    public static final int LD_NEWS_TIMESTAMP = 0xd; // Adds lastNewsItemTimestamp to RLocalProfile
    public static final int LD_FAKE_TIMESTAMP = 0xe; // I think this is what this is, probably
    public static final int LD_SUBMERGED = 0xf; // Adds Game.AmountBody/HeadSubmerged to PWorld
    // 0x4c44:0x0010
    public static final int LD_REMOVED_ENEMY_STAT = 0x11; // Removes enemiesKilled from PlayerMetrics
    public static final int LD_QUEUE = 0x12; // Added lolcatFtwTimestamp to RLocalProfile
    public static final int LD_SHADER = 0x13; // Added another shader to RGfxMaterial
    public static final int LD_THERMOMETER = 0x14; // Added showThermometer to RLocalProfile
    public static final int LD_USED_SCUBA = 0x15; // Added hasUsedScuba to RGame
    public static final int LD_OWNER_ID = 0x16; // Added ownerUserId to RLocalProfile
    public static final int LD_WATER_LEVELS = 0x17; // Added numWaterLevelsPlayed to RLocalProfile
    
    public static final int LD_MAX = 0x17; // last revision for leerdammer revisions

    // Vita revisions

    public static final int D1_HEAD = 0x3e2; // Head revision for Double11/Vita branch

    public static final int D1_PERFDATA = 0x4; // Added perf data to RGfxMaterial
    public static final int D1_SHADER = 0x10; // Added additional shaders to RGfxMaterial
    public static final int D1_UV_OFFSCALE = 0x19; // Added UV offsets/scales to RGfxMaterial
    public static final int D1_VERTEX_COLORS = 0x28; // Added vertexColors to RMesh
    public static final int D1_WATER_BRIGHTNESS = 0x2d; // Adds waterBrightness to PWorld
    public static final int D1_DATALABELS = 0x2e; // Creator data labels added to RBigProfile
    public static final int D1_PROTECTED_LABELS = 0x31; // added protectedIds to PlayerDataLabels
    public static final int D1_LABEL_ANALOGUE_ARRAY = 0x33; // Make analogue value from DataLabelValue an array, rather than a singular type
    public static final int D1_LABEL_TERNARY = 0x3c; // Ternary value added to DataLabelValue
    public static final int D1_SLOT_REWARDS = 0x3d; // Added rewards and acingEnabled fields to slot
    public static final int D1_SLOT_ENFORCE_MINMAX = 0x4c; // added enforceMinMaxPlayers to slot
    public static final int D1_SLOT_SAME_SCREEN = 0x4d; // added sameScreenGame to slot
    public static final int D1_CHECKPOINT_PLAY_AUDIO = 0x50; // added playAudio to PCheckpoint
    public static final int D1_NEAR_CHALLENGES = 0x57; // Near challenges added to RBigProfile
    public static final int D1_3G_CONNECTIVITY = 0x58; // added total3gUp/DownStream to RLocalProfile
    public static final int D1_NEAR_TREASURES = 0x59; // Near treasure log added to RBigProfile
    public static final int D1_DOWNLOADED_SLOTS = 0x5a; // Downloaded slots added to RBigProfile
    public static final int D1_CHALLENGE_LEVEL_TYPE = 0x5b; // levelType added to Challenge
    public static final int D1_DEFERRED_PLAYS = 0x5c; // Added deferredPlayCount(Uploaded) to PlayedLevelData
    public static final int D1_SLOT_DOWNLOAD_DATA = 0x5d; // sizeOfResources, sizeOfSubLevels, subLevels, and slotList added to slot for download metadata
    public static final int D1_COLLECTABUBBLES = 0x5e; // containsCollectabubbles added to slot
    public static final int D1_LEVEL_TIMES_MAP = 0x60; // added levelTimesMap to PlayerMetrics
    public static final int D1_UPLOADED_HIGH_SCORE = 0x61; // added uploadedLocalHighScore to PlayedLevelData
    public static final int D1_MOE_PIN_PROGRESS = 0x64; // added moreOfEverythingPinProgress to PinsAwarded
    public static final int D1_CHALLENGE_SCORE = 0x68; // myScore and networkOnlineID added to Challenge
    public static final int D1_TOUCH_CREATE = 0x69; // createRearTouchPan, createRearTouchPinchZoom, createRearTapZoom aded to RLocalProfile
    public static final int D1_TROPHIES = 0x72; // added gold/silver/bronze trophy counts to PlayedLevelDATA
    public static final int D1_CREATE_WARNING = 0x74; // Added createFrameRateWarningMessages to RLocalProfile
    public static final int D1_CREATE_BETA = 0x75; // Added online beta timestamps to RLocalProfile
    public static final int D1_PLANET_DECORATIONS = 0x7b; // Planet decorations added to RBigProfile
    public static final int D1_DETAILS_PROPORTIONAL = 0x7d; // add makeSizeProportional to InventoryItemDetails
    public static final int D1_SLOT_REVISION = 0x80; // download revision added to slot
    
    public static final int D1_MAX = 0x87; // last revision for vita

    // LBP3 revisions

    public static final int COLLECTION_POPPET_POWERUP = 0x9; // added poppetPowerUpSelection to InventoryCollection
    public static final int SLOT_GAME_MODE = 0x12; // gameMode added to slot
    public static final int TRIGGER_ZRANGE = 0x2a; // zRangeHundreds added to PTrigger
    public static final int WORLD_BACKDROP_OFFSET = 0x6d; // add backdrop offset adjustment fields to PWorld
    public static final int WORLD_BACKDROP_AUTOZ = 0x70; // added backdropOffsetZAuto to PWorld
    public static final int TRIGGER_SCORE_VALUE = 0x90; // scoreValue added to PTrigger
    public static final int DECORATION_SHADOW = 0xc4; // Added hasShadow to decorations
    public static final int STREAMING_PLAN = 0xcc; // isUsedForStreaming added to RPlan
    public static final int SLOT_GAME_KIT = 0xd2; // isGameKit added to slot
    public static final int MESH_SKELETON_TYPE = 0xd6; // added skeletonType to RMesh
    public static final int WORLD_AMBIENCE_OVERRIDE = 0xe2; // added overrideBackdropAmbience to world
    public static final int MOVE_GAMEPLAY_DATA_TYPE = 0xef; // move gameplayType to top of struct in PGameplayData
    public static final int GAMEPLAY_DATA_TREASURE = 0xf3; // treasureType and treasureCount added to PGameplayData
    public static final int WORLD_WATER_COLORS = 0xf8; // waterHint colors added to PWorld
    public static final int ITEM_GUID = 0x106; // Added tempGUID to InventoryItem
    public static final int SLOT_ENTRANCE_DATA = 0x11b; // entranceName and originalSlotID added to slot
    public static final int CREATURE_SUBSTATE = 0x132; // subState added to PCreature
    public static final int FRESNEL = 0x13a; // Added refractive fresnel fields to RGfxMaterial
    public static final int ADVENTURE = 0x145; // Added adventure resource to slot
    public static final int SLOT_BADGE_SIZE = 0x153; // customBadgeSize added to slot
    public static final int FUZZ = 0x16b; // Added fuzzLengthAndRefractiveFlag to RGfxMaterial and triangleAdjacencyInfo to RMesh
    public static final int DECORATION_QUEST = 0x16c; // Added isQuest to decorations
    public static final int SLAPPED_AS_PIN = 0x177; // added slappedAsBits to PinsAwarded
    public static final int FUZZ_LIGHTING = 0x17c; // Added fuzz lighting/swirl fields to RGfxMaterial
    public static final int WORLD_BACKDROP_TOGGLE = 0x182; // added backdropEnabled and currBackdropEnabled to PWorld
    public static final int SLOT_TRAILER_PATH = 0x192; // added localPath for trailer to slot
    public static final int SLOT_TRAILER_THUMBNAIL = 0x206; // added thumbPath to slot
    public static final int SIMON_SAYS_PIN = 0x209; // added simonSaysProgress to PinsAwarded
    public static final int SPRINGINATOR = 0x20c; // Added springinator fields
    public static final int SLOT_ENFORCE_MINMAX = 0x215; // add enforceMinMaxPlayers to slot
    
    public static final int LBP3_MAX = 0x218;

    // Toolkit revisions

    public static final int MZ_HEAD = 0x021803f9;

    public static final int MZ_BASE = 0x0; // Base revision for Toolkit assets
    public static final int MZ_BST_REMOVE_SK = 0x1; // Remove skeleton type from RBoneSet
    public static final int MZ_CGC_ORBIS = 0x2; // Keep track if a shader cache is for PS4 or not.
    public static final int MZ_CGC_PATH = 0x3; // Keep original path of RGfxMaterial in RShaderCache
    public static final int MZ_CGC_SWIZZLE = 0x4; // Swizzled color vector in RShaderCache
    public static final int MZ_CGC_SHORT_FLAGS = 0x5; // Use short flags in RShaderCache
    
    public static final int MZ_MAX = 0x5; // last revision for toolkit revisions

    // Legacy mod revisions
    // Somewhat weird, but apparently Toolkit started with mod
    // revisions of 0x3, all older ones were only present in early versions
    // of Workbench

    public static final int LM_OLD_HEAD = 0xffff; // Head revision for mods prior to LM_TYPES
    public static final int LM_HEAD = 0x01ae03fa; // Head revision for mods >= LM_TYPES

    public static final int LM_BASE = 0x0; // Base revisions for old mods
    public static final int LM_ITEMS = 0x1; // Added cached item metadata
    public static final int LM_TRANSLATIONS = 0x2; // Store translated locations/categories in mod
    public static final int LM_TOOLKIT = 0x3; // When mods were added to Toolkit, move icon to bottom of mod, title/desription use wstr instead of int16 size + char, added minor version, entries now contain data size rather, file data is now one big buffer rather than an array of bytearrays
    public static final int LM_SLOTS_TIMESTAMPS = 0x4; // Timestamps added to each entry, slot array added
    public static final int LM_MINMAX = 0x5; // Added min/max revision to entries, meant for mods to contain multiple versions of gmats/plans, never used.
    public static final int LM_TYPES = 0x6; // author is now str16, itemCount switched from u16 to u32, base revision bumped to support LBP3 slot metadata
    
    public static final int LM_MAX = 0x6; // last revision fo legacy mods
}
