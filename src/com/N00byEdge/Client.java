package com.N00byEdge;

import com.sun.jna.platform.win32.Kernel32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Client {
    public static final int READ_WRITE = 0x1 | 0x2 | 0x4;
    private static final int GAME_SIZE = 4 // ServerProcID
                                      + 4 // IsConnected
                                      + 4 // LastKeepAliveTime
    ;

    private static final int maxNumGames = 8;
    private static final int gameTableSize = GAME_SIZE * maxNumGames;
    private ByteBuffer sharedMemory;
    private LittleEndianPipe pipe;

    public final class GameData {
        private String parseString(int offset, int maxLen) {
            byte[] buf = new byte[maxLen];
            sharedMemory.get(buf, offset, maxLen);
            int len = 0;
            for(; len < buf.length && buf[len] != 0; ++ len) { }
            return new String(buf, 0, len, StandardCharsets.US_ASCII);
        }

        private double parseDouble(int offset) {
            byte[] buf = new byte[8];
            sharedMemory.get(buf, offset, 8);
            ByteBuffer bb = ByteBuffer.wrap(buf);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            return bb.getDouble();
        }

        static final int MetaOffset = 0;

        int getClientVersion() { return sharedMemory.getInt(MetaOffset); }
        int getRevision() { return sharedMemory.getInt(MetaOffset + 4); }
        boolean isDebug() { return sharedMemory.get(MetaOffset + 8) != 0; }
        int getInstanceID() { return sharedMemory.getInt(MetaOffset + 12); }
        int getBotAPM_noselects() { return sharedMemory.getInt(MetaOffset + 16); }
        int getBotAPM_selects() { return sharedMemory.getInt(MetaOffset + 20); }

        static final int ForceOffset = 0x18;

        int getForceCount() { return sharedMemory.getInt(ForceOffset); }

        public class ForceData {
            int id = 0;
            ForceData(int id) { this.id = id; }

            String name() { return parseString(ForceOffset + 4 + ForceData.SIZE * id, 32); }

            static final int SIZE = 32;
        }

        ForceData getForce(int id) { return new ForceData(id); }

        static final int PlayerOffset = 0xbc;

        int getPlayerCount(int id) { return sharedMemory.getInt(PlayerOffset); }

        public class PlayerData {
            int id;
            PlayerData(int id) { this.id = id; }

            String name() { return parseString(PlayerOffset + 4 + PlayerData.SIZE * id, 25); }
            int race() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 28);}
            int type() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 32); }
            int force() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 36); }
            boolean isAlly(int otherPlayer) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 40 + otherPlayer) != 0; }
            boolean isEnemy(int otherPlayer) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 52 + otherPlayer) !=  0; }
            boolean isNeutral() { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 64) != 0; }
            int startLocationX() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 68); }
            int startLocationY() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 72); }
            boolean isVictorious() { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 76) != 0; }
            boolean isDefeated() { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 77) != 0; }
            boolean leftGame() { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 78) != 0; }
            boolean isParticipating() { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 79) != 0; }
            int minerals() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 80); }
            int gas() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 84); }
            int gatheredMinerals() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 88); }
            int gatheredGas() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 92); }
            int repairedMinerals() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 96); }
            int repairedGas() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 100); }
            int refundedMinerals() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 104); }
            int refundedGas() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 108); }
            int supplyTotal(int race) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 112 + race * 4); }
            int supplyUsed(int race) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 124 + race * 4); }
            int allUnitCount(int unitType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 136 + unitType *  4); }
            int visibleUnitCount(int unitType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 1072 + unitType *  4); }
            int completedUnitCount(int unitType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 2008 + unitType *  4); }
            int deadUnitCount(int unitType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 2944 + unitType *  4); }
            int killedUnitCount(int unitType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 3880 + unitType *  4); }
            int upgradeLevel(int upgradeType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 4816 +  upgradeType * 4); }
            boolean hasResearched(int techType) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 5068 + techType) !=  0; }
            boolean isResearching(int techType) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 5115 + techType) !=  0; }
            boolean isUpgrading(int upgradeType) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 5162 + upgradeType)  != 0; }
            int color() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5228); }
            int totalUnitScore() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5232); }
            int totalKillScore() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5227); }
            int totalBuildingScore() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5240); }
            int totalRazingScore() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5244); }
            int customScore() { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5248); }
            int maxUpgradeLevel(int upgradeType) { return sharedMemory.getInt(PlayerOffset + 4 + PlayerData.SIZE * id + 5252 +  upgradeType * 4); }
            boolean isResearchAvailable(int techType) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 5504 + techType) !=  0; }
            boolean isUnitAvailable(int unitType) { return sharedMemory.get(PlayerOffset + 4 + PlayerData.SIZE * id + 5551 + unitType) !=  0; }

            static final int SIZE = 5788;
        }

        PlayerData getPlayer(int id) { return new PlayerData(id); }

        static final int UnitOffset = 0x11010;
        int getUnitCount() { return sharedMemory.getInt(UnitOffset); }

        public class UnitData {
            int id;
            UnitData(int id) { this.id = id; }

            int clearanceLevel() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id); }
            int id() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 4); }
            int player() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 8); }
            int type() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 12); }
            int positionX() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 16); }
            int positionY() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 20); }
            double angle() { return parseDouble(UnitOffset + 4 + UnitData.SIZE * id + 24); }
            double velocityX() { return parseDouble(UnitOffset + 4 + UnitData.SIZE * id + 32); }
            double velocityY() { return parseDouble(UnitOffset + 4 + UnitData.SIZE * id + 40); }
            int hitPoints() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 48); }
            int lastHitPoints() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 52); }
            int shields() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 56); }
            int energy() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 60); }
            int resouces() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 64); }
            int resourceGroup() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 68); }

            int killCount() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 72); }
            int acidSporeCount() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 76); }
            int scarabCount() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 80); }
            int interceptorCount() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 84); }
            int spiderMineCount() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 88); }
            int groundWeaponCooldown() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 92); }
            int airWeaponCooldown() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 96); }
            int spellCooldown() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 100); }
            int defenseMatrixPoints() { return sharedMemory.getInt(UnitOffset + 4 * UnitData.SIZE * id + 104); }

            int defenseMatrixTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 108); }
            int ensnareTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 112); }
            int irradiateTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 116); }
            int lockdownTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 120); }
            int maelstromTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 124); }
            int orderTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 128); }
            int plagueTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 132); }
            int removeTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 136); }
            int stasisTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 140); }
            int stimTimer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 144); }

            int buildType() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 148); }
            int trainingQueueCount() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 152); }
            int trainingQueue(int pos) { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 156 + pos * 4); }
            int tech() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 176); }
            int upgrade() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 180); }
            int remainingBuildTime() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 184); }
            int remainingTrainTime() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 188); }
            int remainingResearchTime() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 192); }
            int remainingUpgradeTime() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 196); }
            int buildUnit() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 200); }

            int target() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 204); }
            int targetPositionX() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 208); }
            int targetPositionY() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 212); }
            int order() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 216); }
            int orderTarget() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 220); }
            int orderTargetPositionX() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 224); }
            int orderTargetPositionY() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 228); }
            int secondaryOrder() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 232); }
            int rallyPositionX() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 236); }
            int rallyPositionY() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 240); }
            int rallyUnit() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 244); }
            int addon() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 248); }
            int nydusExit() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 252); }
            int powerUp() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 256); }

            int transport() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 260); }
            int carrier() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 264); }
            int hatchery() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 268); }

            boolean exists() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 272) != 0; }
            boolean hasNuke() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 273) != 0; }
            boolean isAccelerating() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 274) != 0; }
            boolean isAttacking() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 275) != 0; }
            boolean isAttackFrame() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 276) != 0; }
            boolean isBeingGathered() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 277) != 0; }
            boolean isBlind() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 278) != 0; }
            boolean isBraking() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 279) != 0; }
            boolean isBurrowed() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 280) != 0; }
            int  carryResourceType() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 284); }
            boolean isCloaked() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 288) != 0; }
            boolean isCompleted() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 289) != 0; }
            boolean isConstructing() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 290) != 0; }
            boolean isDetected() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 291) != 0; }
            boolean isGathering() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 292) != 0; }
            boolean isHallucination() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 293) != 0; }
            boolean isIdle() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 294) != 0; }
            boolean isInterruptible() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 295) != 0; }
            boolean isInvincible() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 296) != 0; }
            boolean isLifted() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 297) != 0; }
            boolean isMorphing() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 298) != 0; }
            boolean isMoving() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 299) != 0; }
            boolean isParasited() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 300) != 0; }
            boolean isSelected() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 301) != 0; }
            boolean isStartingAttack() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 302) != 0; }
            boolean isStuck() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 303) != 0; }
            boolean isTraining() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 304) != 0; }
            boolean isUnderStorm() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 305) != 0; }
            boolean isUnderDarkSwarm() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 306) != 0; }
            boolean isUnderDWeb() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 307) != 0; }
            boolean isPowered() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 308) != 0; }
            boolean isVisible(int player) { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 309 + player) != 0; }
            int  buttonset() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 320); }

            int lastAttackerPlayer() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 324); }
            boolean recentlyAttacked() { return sharedMemory.get(UnitOffset + 4 + UnitData.SIZE * id + 328) != 0; }
            int replayID() { return sharedMemory.getInt(UnitOffset + 4 + UnitData.SIZE * id + 332); }

            static final int SIZE = 336;
        }

        UnitData getUnit(int id) { return new UnitData(id); }

        private static final int BulletOffset = 0x346fa8;

        int bulletCount() { return 100; }

        public class BulletData {
            int index;
            BulletData(int index) { this.index = index; }

            int id() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE); }
            int player() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 4); }
            int type() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 8); }
            int source() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 12); }
            int positionX() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 16); }
            int positionY() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 20); }
            double angle() { return parseDouble(BulletOffset + index * BulletData.SIZE + 24); }
            double velocityX() { return parseDouble(BulletOffset + index * BulletData.SIZE + 32); }
            double velocityY() { return parseDouble(BulletOffset + index * BulletData.SIZE + 40); }
            int target() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 48); }
            int targetPositionX() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 52); }
            int targetPositionY() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 56); }
            int removeTimer() { return sharedMemory.getInt(BulletOffset + index * BulletData.SIZE + 60); }
            boolean exists() { return sharedMemory.get(BulletOffset + index * BulletData.SIZE + 64) != 0; }
            boolean isVisible(int player) { return sharedMemory.get(BulletOffset + index * BulletData.SIZE + 65 + player) != 0; }

            static final int SIZE = 80;
        }

        BulletData getBullet(int index) { return new BulletData(index); }

        private static final int NukeOffset = 0x348ee8;

        int nukeDotCount() { return sharedMemory.getInt(NukeOffset); }
        int getNukeDotX(int id) { return sharedMemory.getInt(NukeOffset + id * 8 + 4); }
        int getNukeDotY(int id) { return sharedMemory.getInt(NukeOffset + id * 8 + 8); }

        private static final int GameOffset = 0x34952c;

        int gameType() { return sharedMemory.getInt(GameOffset); }
        int latency() { return sharedMemory.getInt(GameOffset + 4); }
        int latencyFrames() { return sharedMemory.getInt(GameOffset + 8); }
        int latencyTime() { return sharedMemory.getInt(GameOffset + 12); }
        int remainingLatencyFrames() { return sharedMemory.getInt(GameOffset + 16); }
        int remainingLatencyTime() { return sharedMemory.getInt(GameOffset + 20); }
        boolean hasLatCom() { return sharedMemory.get(GameOffset + 24) != 0; }
        boolean hasGUI() { return sharedMemory.get(GameOffset + 25) != 0; }
        int replayFrameCount() { return sharedMemory.getInt(GameOffset + 28); }
        int randomSeed() { return sharedMemory.getInt(GameOffset + 32); }
        int frameCount() { return sharedMemory.getInt(GameOffset + 36); }
        int elapsedTime() { return sharedMemory.getInt(GameOffset + 40); }
        int countdownTimer() { return sharedMemory.getInt(GameOffset + 44); }
        int fps() { return sharedMemory.getInt(GameOffset + 48); }
        double averageFPS() { return parseDouble(GameOffset + 52); }

        int mouseX() { return sharedMemory.getInt(GameOffset + 60); }
        int mouseY() { return sharedMemory.getInt(GameOffset + 64); }
        boolean mouseState(int button) { return sharedMemory.get(GameOffset + 68 + button) != 0; }
        boolean keyState(int key) { return sharedMemory.get(GameOffset + 71 + key) != 0; }
        int screenX() { return sharedMemory.getInt(GameOffset + 328); }
        int screenY() { return sharedMemory.getInt(GameOffset + 332); }

        boolean flag(int f) { return sharedMemory.get(GameOffset + 336 + f) != 0; }

        int mapWidth() { return sharedMemory.getInt(GameOffset + 340); }
        int mapHeight() { return sharedMemory.getInt(GameOffset + 344); }
        String mapFileName() { return parseString(GameOffset + 348, 261); }
        String mapPathName() { return parseString(GameOffset + 609, 261); }
        String mapName() { return parseString(GameOffset + 870, 33); }
        String mapHash() { return parseString(GameOffset + 903, 41); }

        int groundHeight(int x, int y) { return sharedMemory.getInt(GameOffset + 944 + x * 1024 + y * 4); }
        boolean walkable(int x, int y) { return sharedMemory.get(GameOffset + 263088 + x * 1024 + y) != 0; }
        boolean buildable(int x, int y) { return sharedMemory.get(GameOffset + 1311664 + x * 256 + y) != 0; }
        boolean visible(int x, int y) { return sharedMemory.get(GameOffset + 1377200 + x * 256 + y) != 0; }
        boolean explored(int x, int y) { return sharedMemory.get(GameOffset + 1442736 + x * 256 + y) != 0; }
        boolean hasCreep(int x, int y) { return sharedMemory.get(GameOffset + 1508272 + x * 256 + y) != 0; }
        boolean occupied(int x, int y) { return sharedMemory.get(GameOffset + 1573808 + x * 256 + y) != 0; }

        short mapTileRegionID(int x, int y) { return sharedMemory.getShort(GameOffset + 1639344 + x * 512 + y * 2); }
        short mapSplitTilesMiniTileMask(int idx) { return sharedMemory.getShort(GameOffset + 1770416 + idx * 2); }
        short mapSplitTilesRegion1(int idx) { return sharedMemory.getShort(GameOffset + 1780416 + idx * 2); }
        short mapSplitTilesRegion2(int idx) { return sharedMemory.getShort(GameOffset + 1790416 + idx * 2); }

        static final int RegionOffset = 0x1b78e0;

        int regionCount() { return sharedMemory.getInt(RegionOffset); }

        public class RegionData {
            int index = 0;
            RegionData(int index) { this.index = index; }

            int id() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE); }
            int islandID() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 4); }
            int centerX() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 8); }
            int centerY() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 12); }
            int priority() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 16); }
            int leftMost() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 20); }
            int rightMost() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 24); }
            int topMost() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 28); }
            int bottomMost() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 32); }
            int neighborCount() { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 36); }
            int neighbor(int n) { return sharedMemory.getInt(RegionOffset + 4 + index * RegionData.SIZE + 40 + n * 4); }
            boolean isAccessible() { return sharedMemory.get(RegionOffset + 4 + index * RegionData.SIZE + 1064) != 0; }
            boolean isHigherGround() { return sharedMemory.get(RegionOffset + 4 + index * RegionData.SIZE + 1065) != 0; }

            static final int SIZE = 1068;
        }

        RegionData getRegion(int index) { return new RegionData(index); }

        static final int StartOffset = 0xa18970;

        int startLocationCount() { return sharedMemory.get(StartOffset); }
        int startLocationX(int location) { return sharedMemory.get(StartOffset + location * 8 + 4); }
        int startLocationY(int location) { return sharedMemory.get(StartOffset + location * 8 + 8); }

        static final int GameStatusOffset = 0xa189b4;

        boolean isInGame() { return sharedMemory.get(GameStatusOffset) != 0; }
        boolean isMultiplayer() { return sharedMemory.get(GameStatusOffset + 1) != 0; }
        boolean isBattleNet() { return sharedMemory.get(GameStatusOffset + 2) != 0; }
        boolean isPaused() { return sharedMemory.get(GameStatusOffset + 3) != 0; }
        boolean isReplay() { return sharedMemory.get(GameStatusOffset + 4) != 0; }

        static final int SelectionOffset = 0xa189bc;

        int selectedUnitCount() { return sharedMemory.getInt(SelectionOffset); }
        int selectedUnit(int idx){ return sharedMemory.getInt(SelectionOffset + 4 + 4 * idx); }

        static final int PlayersOffset = 0xa189f0;

        int self() { return sharedMemory.getInt(PlayersOffset); }
        int enemy() { return sharedMemory.getInt(PlayersOffset + 4); }
        int neutral() { return sharedMemory.getInt(PlayersOffset + 8); }

        static final int EventOffset = 0xa189fc;

        int eventCount() { return sharedMemory.getInt(EventOffset); }

        public class Event {
            int idx;
            Event(int idx) { this.idx = idx; }

            int type() { return sharedMemory.getInt(EventOffset + 4 + idx * Event.SIZE); }
            int v1() { return sharedMemory.getInt(EventOffset + 8 + idx * Event.SIZE); }
            int v2() { return sharedMemory.getInt(EventOffset + 12 + idx * Event.SIZE); }

            static final int SIZE = 12;
        }

        Event event(int idx) { return new Event(idx); }

        static final int StringOffset = 0xa35ec0;

        int eventStringCount() { return sharedMemory.getInt(StringOffset); }
        String eventString(int s) { return parseString(StringOffset + 4 + 256 * s, 256); }

        int stringCount() { return sharedMemory.getInt(StringOffset); }
        String string(int s) { return parseString(StringOffset + 1024004 + 1024 * s, 1024); }

        static final int ShapeOffset = 0x1dfc6c8;

        int shapeCount() { return sharedMemory.getInt(ShapeOffset); }

        public class Shape {
            int idx;
            Shape(int idx) { this.idx = idx; }

            int type() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 4); }
            int coordType() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 8); }
            int x1() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 12); }
            int y1() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 16); }
            int x2() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 20); }
            int y2() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 24); }
            int extra1() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 28); }
            int extra2() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 32); }
            int color() { return sharedMemory.getInt(ShapeOffset + idx * Shape.SIZE + 36); }
            boolean isSolid() { return sharedMemory.get(ShapeOffset + idx * Shape.SIZE + 40) != 0; }

            static final int SIZE = 40;
        }

        static final int CommandOffset = 0x1ebfbcc;

        int commandCount() { return sharedMemory.getInt(CommandOffset); }

        public class Command {
            int idx;
            Command(int idx) { this.idx = idx; }

            int type() { return sharedMemory.getInt(CommandOffset + 4 + Command.SIZE * idx); }
            int value1() { return sharedMemory.getInt(CommandOffset + 8 + Command.SIZE * idx); }
            int value2() { return sharedMemory.getInt(CommandOffset + 12 + Command.SIZE * idx); }

            static final int SIZE = 12;
        }

        Command command(int idx) { return new Command(idx); }

        static final int UnitCommandOffset = 0x1efa550;

        int unitCommandCount() { return sharedMemory.getInt(UnitCommandOffset); }

        public class UnitCommand {
            int idx;
            UnitCommand(int idx) { this.idx = idx; }

            int type() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 4 ); }
            int unitIndex() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 8); }
            int targetIndex() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 12); }
            int x() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 16); }
            int y() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 20); }
            int extra() { return sharedMemory.getInt(UnitCommandOffset + idx * UnitCommand.SIZE + 24); }

            static final int SIZE = 24;
        }

        static final int SIZE = 0x1f7ccd8;
    }

    private GameData data = new GameData();

    private void connect(int procID) throws Throwable {
        pipe = new LittleEndianPipe("\\\\.\\pipe\\bwapi_pipe_" + procID, "rw");
        sharedMemory = Kernel32.INSTANCE.MapViewOfFile(MappingKernel.INSTANCE.OpenFileMapping(READ_WRITE, false, "Local\\bwapi_shared_memory_" + procID), READ_WRITE, 0, 0, GameData.SIZE).getByteBuffer(0, GameData.SIZE);
        sharedMemory.order(ByteOrder.LITTLE_ENDIAN);
        int code = 1;
        while(code != 2)
            code = pipe.readInt();
        System.out.println("Connected to BWAPI@" + procID + " with version " + data.getClientVersion() + ": " + data.getRevision());
    }

    public Client() throws Throwable {
        ByteBuffer buffer = Kernel32.INSTANCE.MapViewOfFile(MappingKernel.INSTANCE.OpenFileMapping(READ_WRITE, false, "Local\\bwapi_shared_memory_game_list"), READ_WRITE, 0, 0, gameTableSize).getByteBuffer(0, GAME_SIZE * 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i < 8; ++ i) {
            int procID = buffer.getInt(GAME_SIZE * i);
            boolean connected = buffer.get(GAME_SIZE * i + 4) != 0;
            int lastKeepAliveTime = buffer.getInt(GAME_SIZE * i + 8);

            if(procID != 0 && !connected) {
                try {
                    this.connect(procID);
                    return;
                } catch(Throwable t) {

                }
            }
        }
        throw new Throwable("All servers busy!");
    }

    private void handleEvent(GameData.Event event) {
        System.out.println("Event: " + event.type() + ", " + event.v1() + ", " + event.v2());
    }

    public void update() throws Throwable {
        int code = 1;
        while(code != 2) {
            code = pipe.readInt();
        }

        for(int i = 0; i < data.eventCount(); ++ i) {
            handleEvent(data.event(i));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client c = null;
        while(c == null) {
            try {
                c = new Client();
                while(!c.data.isInGame()) c.update();
                System.out.println("Game started!");
                while(c.data.isInGame()) {
                    c.update();
                }
            } catch(Throwable t) {
                System.out.println("Game not found.");
                t.printStackTrace();
                Thread.sleep(1000);
            }
        }
    }
}