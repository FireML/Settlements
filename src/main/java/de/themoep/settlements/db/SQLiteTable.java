package de.themoep.settlements.db;

public enum SQLiteTable {

    SETTLEMENTS("CREATE TABLE IF NOT EXISTS settlements ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "name VARCHAR(255) NOT NULL,"
            + "chiefId INTEGER NOT NULL,"
            + "level INTEGER NOT NULL,"
            + "settlementState VARCHAR(255) NOT NULL,"
            + "description TEXT NOT NULL,"
            + "rules TEXT NOT NULL,"
            + "discord TEXT NOT NULL,"
            + "created BIGINT,"
            + "access VARCHAR(255) NOT NULL,"
            + "joinType VARCHAR(255) NOT NULL,"
            + "buildState VARCHAR(255) NOT NULL,"
            + "allowAllyClaim BOOLEAN NOT NULL,"
            + "pvp BOOLEAN NOT NULL,"
            + "fireSpread BOOLEAN NOT NULL,"
            + "explosions BOOLEAN NOT NULL,"
            + "mobSpawning BOOLEAN NOT NULL,"
            + "passiveSpawning BOOLEAN NOT NULL,"
            + "buff BOOLEAN NOT NULL,"
            + "bank NUMERIC(19,4),"
            + "joinFee NUMERIC(19,4),"
            + "tax NUMERIC(19,4),"
            + "auctionTax NUMERIC(19,4),"
            + "taxDebtAction VARCHAR(255) NOT NULL,"
            + "finishedMissions BIGINT,"
            + "missionPoints BIGINT,"
            + "bonusClaims LONG,"
            + "homeLoc_world VARCHAR(255),"
            + "homeLoc_x DOUBLE,"
            + "homeLoc_y DOUBLE,"
            + "homeLoc_z DOUBLE,"
            + "homeLoc_yaw FLOAT,"
            + "homeLoc_pitch FLOAT,"
            + "FOREIGN KEY (chiefId) REFERENCES residents(id) ON DELETE RESTRICT"
            + ");"
            + "CREATE UNIQUE INDEX settlements_names ON settlements (name);"),

    SETTLEMENT_OUTPOSTS("CREATE TABLE IF NOT EXISTS settlement_outposts ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "name VARCHAR(255),"
            + "world VARCHAR(255),"
            + "x DOUBLE,"
            + "y DOUBLE,"
            + "z DOUBLE,"
            + "yaw FLOAT,"
            + "pitch FLOAT,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, name)"
            + ");"
            + "CREATE INDEX settlement_outposts_id ON settlement_outposts (settlementId);"),

    SETTLEMENT_VAULTS("CREATE TABLE IF NOT EXISTS settlement_vaults ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "settlementId INTEGER,"
        + "content VARCHAR(255) NOT NULL,"
        + "amount BIGINT,"
        + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
        + "UNIQUE (settlementId, content)"
        + ");"
        + "CREATE INDEX settlement_vault_id ON settlement_vaults (settlementId);"),

    SETTLEMENT_MISSIONDATA("CREATE TABLE IF NOT EXISTS settlement_missiondata ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "mission VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "progress BIGINT DEFAULT 0,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, mission, type)"
            + ");"
            + "CREATE INDEX settlement_missiondata_id ON settlement_missiondata (settlementId);"),

    SETTLEMENT_PERKS("CREATE TABLE IF NOT EXISTS settlement_perks ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "perk VARCHAR(255) NOT NULL,"
            + "times INTEGER NOT NULL,"
            + "lastUsed BIGINT,"
            + "active BOOLEAN,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, perk)"
            + ");"
            + "CREATE INDEX settlement_perks_id ON settlement_perks (settlementId);"),

    SETTLEMENT_USES("CREATE TABLE IF NOT EXISTS settlement_uses ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "type VARCHAR(255) NOT NULL,"
            + "state VARCHAR(255) NOT NULL,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, type)"
            + ");"
            + "CREATE INDEX settlement_uses_id ON settlement_uses (settlementId);"),

    SETTLEMENT_BANISHED("CREATE TABLE IF NOT EXISTS settlement_banished ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "banished_id VARCHAR(128) NOT NULL,"
            + "banished_date BIGINT(11) NOT NULL,"
            + "banished_sender VARCHAR(36),"
            + "banished_reason TEXT,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, banished_id)"
            + ");"
            + "CREATE INDEX settlement_banished_id ON settlement_banished (settlementId);"),

    SETTLEMENT_UNBANISHED("CREATE TABLE IF NOT EXISTS settlement_unbanished ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "unbanished_id VARCHAR(128) NOT NULL,"
            + "unbanished_date BIGINT(11) NOT NULL,"
            + "unbanished_sender VARCHAR(36),"
            + "unbanished_reason TEXT,"
            + "unbanished_unbanisher VARCHAR(36),"
            + "unbanished_unbanishdate VARCHAR(36),"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, unbanished_id, unbanished_date)"
            + ");"
            + "CREATE INDEX settlement_unbanished_id ON settlement_unbanished (settlementId);"),

    SETTLEMENT_ALLIES("CREATE TABLE IF NOT EXISTS settlement_allies ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "allies_id VARCHAR(36) NOT NULL,"
            + "allies_created BIGINT(11) NOT NULL, "
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, allies_id)"
            + ");"
            + "CREATE INDEX settlement_allies_id ON settlement_allies (settlementId);"),

    SETTLEMENT_PERMISSIONS("CREATE TABLE IF NOT EXISTS settlement_permissions ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "rank VARCHAR(255) NOT NULL,"
            + "permission VARCHAR(255) NOT NULL,"
            + "value BOOLEAN NOT NULL,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, rank, permission)"
            + ");"
            + "CREATE INDEX settlement_permissions_id ON settlement_permissions (settlementId);"),

    SETTLEMENT_TITLES("CREATE TABLE IF NOT EXISTS settlement_titles ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, rank, title)"
            + ");"
            + "CREATE INDEX settlement_titles_id ON settlement_titles (settlementId);"),

    SETTLEMENT_DEFAULTTITLES("CREATE TABLE IF NOT EXISTS settlement_defaulttitles ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE CASCADE,"
            + "UNIQUE (settlementId, rank)"
            + ");"
            + "CREATE INDEX settlement_defaulttitles_id ON settlement_defaulttitles (settlementId);"),

    RESIDENTS("CREATE TABLE IF NOT EXISTS residents ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "uuid CHAR(36) NOT NULL,"
            + "name VARCHAR(16) NOT NULL,"
            + "settlementId INTEGER,"
            + "settlementJoin BIGINT(11) NOT NULL,"
            + "rank VARCHAR(255),"
            + "title VARCHAR(16),"
            + "bonusClaims LONG,"
            + "missionPoints BIGINT,"
            + "lastLogin BIGINT(11) NOT NULL,"
            + "taxExempted BOOLEAN NOT NULL,"
            + "taxDebtAction VARCHAR(255) NOT NULL,"
            + "taxPaid NUMERIC(19,4) DEFAULT 0,"
            + "donated NUMERIC(19,4) DEFAULT 0,"
            + "FOREIGN KEY (settlementId) REFERENCES settlements(id) ON DELETE SET NULL"
            + ");"
            + "CREATE UNIQUE INDEX residents_id ON residents (uuid);"),

    RESIDENT_FRIENDS("CREATE TABLE IF NOT EXISTS resident_friends ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userId INTEGER,"
            + "friendId INTEGER,"
            + "FOREIGN KEY (userId) REFERENCES residents(id) ON DELETE CASCADE,"
            + "FOREIGN KEY (friendId) REFERENCES residents(id) ON DELETE CASCADE,"
            + "UNIQUE (userId, friendId)"
            + ");"
            + "CREATE INDEX resident_friends_id ON resident_friends (userId);"),

    RESIDENT_MISSIONDATA("CREATE TABLE IF NOT EXISTS resident_missiondata ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userId INTEGER,"
            + "mission VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "progress BIGINT DEFAULT 0,"
            + "FOREIGN KEY (userId) REFERENCES residents(id) ON DELETE CASCADE,"
            + "UNIQUE (userId, mission, type)"
            + ");"
            + "CREATE INDEX resident_missiondata_id ON resident_missiondata (userId);"),

    INVITES("CREATE TABLE IF NOT EXISTS invites ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "type VARCHAR(255) NOT NULL,"
            + "targetType VARCHAR(255) NOT NULL,"
            + "targetId INTEGER NOT NULL,"
            + "sourceType VARCHAR(255) NOT NULL,"
            + "sourceId INTEGER NOT NULL,"
            + "time BIGINT(11) NOT NULL"
            + ");"
            + "CREATE INDEX invite_type ON invites (type);"),

    CLAIMS("CREATE TABLE IF NOT EXISTS claims ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "landOwnerType VARCHAR(255) NOT NULL,"
            + "landOwnerId INTEGER NOT NULL,"
            + "world VARCHAR(255),"
            + "x INTEGER,"
            + "z INTEGER,"
            + "unclaimIn INTEGER,"
            + "ownerId INTEGER,"
            + "price NUMERIC(19,4) DEFAULT 0,"
            + "forSale BOOLEAN NOT NULL,"
            + "tax NUMERIC(19,4) DEFAULT 0,"
            + "embassy BOOLEAN NOT NULL,"
            + "name VARCHAR(255),"
            + "description TEXT,"
            + "buildState VARCHAR(255),"
            + "link VARCHAR(36),"
            + "FOREIGN KEY (ownerId) REFERENCES residents(id) ON DELETE SET NULL,"
            + "UNIQUE(world, x, z)"
            + ");"
            + "CREATE INDEX claims_worlds ON claims (world);"
            + "CREATE INDEX claims_x ON claims (x);"
            + "CREATE INDEX claims_z ON claims (z);"
            + "CREATE INDEX claims_type ON claims (landOwnerType);"
            + "CREATE INDEX claims_id ON claims (landOwnerId);"),

    CLAIM_USES("CREATE TABLE IF NOT EXISTS claim_uses ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "claimId INTEGER,"
            + "type VARCHAR(255) NOT NULL,"
            + "state VARCHAR(255) NOT NULL,"
            + "FOREIGN KEY (claimId) REFERENCES claims(id) ON DELETE CASCADE,"
            + "UNIQUE (claimId, type)"
            + ");"
            + "CREATE INDEX claim_uses_id ON claim_uses (claimId);"),

    CLAIM_MODES("CREATE TABLE IF NOT EXISTS claim_modes ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "claimId INTEGER,"
            + "mode VARCHAR(255) NOT NULL,"
            + "enabled BOOLEAN NOT NULL,"
            + "FOREIGN KEY (claimId) REFERENCES claims(id) ON DELETE CASCADE,"
            + "UNIQUE (claimId, mode)"
            + ");"
            + "CREATE INDEX claim_modes_id ON claim_modes (claimId);"),

    CLAIM_MEMBERS("CREATE TABLE IF NOT EXISTS claim_members ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "claimId INTEGER,"
            + "memberId INTEGER,"
            + "FOREIGN KEY (claimId) REFERENCES claims(id) ON DELETE CASCADE,"
            + "FOREIGN KEY (memberId) REFERENCES residents(id) ON DELETE CASCADE,"
            + "UNIQUE (claimId, memberId)"
            + ");"
            + "CREATE INDEX claim_members_id ON claim_members (claimId);"),

    LOGS("CREATE TABLE IF NOT EXISTS logs ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "settlementId INTEGER,"
            + "residentId INTEGER,"
            + "`action` VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "amount BIGINT NOT NULL,"
            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ");"
            + "CREATE INDEX logs_settlementId ON logs (settlementId);"
            + "CREATE INDEX logs_residentId ON logs (residentId);");

    private final String sql;

    SQLiteTable(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String getDbName() {
        return name().toLowerCase();
    }
}
