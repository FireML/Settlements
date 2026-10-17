package de.themoep.settlements.db;

public enum MySqlTable {

    SETTLEMENTS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlements ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "name VARCHAR(255) NOT NULL, INDEX (name), UNIQUE (name), "
            + "chiefId INTEGER NOT NULL,"
            + "FOREIGN KEY (chiefId) REFERENCES {tablePrefix}residents(id) ON DELETE RESTRICT,"
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
            + "bonusClaims INTEGER,"
            + "homeLoc_world VARCHAR(255),"
            + "homeLoc_x DOUBLE,"
            + "homeLoc_y DOUBLE,"
            + "homeLoc_z DOUBLE,"
            + "homeLoc_yaw FLOAT,"
            + "homeLoc_pitch FLOAT,"
            + "enableNPCs BOOLEAN"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_OUTPOSTS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_outposts ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "name VARCHAR(255),"
            + "UNIQUE (settlementId, name),"
            + "world VARCHAR(255),"
            + "x DOUBLE,"
            + "y DOUBLE,"
            + "z DOUBLE,"
            + "yaw FLOAT,"
            + "pitch FLOAT"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_VAULTS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_vaults ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "content VARCHAR(255) NOT NULL,"
            + "amount BIGINT,"
            + "UNIQUE (settlementId, content)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_MISSIONDATA("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_missiondata ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "mission VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "progress BIGINT DEFAULT 0,"
            + "UNIQUE list (settlementId, mission, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_PERKS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_perks ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "perk VARCHAR(255) NOT NULL,"
            + "times INTEGER NOT NULL,"
            + "lastUsed BIGINT,"
            + "active BOOLEAN,"
            + "UNIQUE (settlementId, perk)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_NPCS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_npcs ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "type VARCHAR(255) NOT NULL,"
            + "npcId INTEGER NOT NULL,"
            + "level INTEGER NOT NULL,"
            + "npcSkinName VARCHAR(255) NOT NULL,"
            + "enabled BOOLEAN NOT NULL,"
            + "UNIQUE (settlementId, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_NPC_VAULT("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_npc_vaults ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "npcId INTEGER, INDEX(npcId),"
            + "FOREIGN KEY (npcId) REFERENCES {tablePrefix}settlement_npcs(id) ON DELETE CASCADE,"
            + "content VARCHAR(255) NOT NULL,"
            + "amount BIGINT,"
            + "UNIQUE (npcId, content)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_USES("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_uses ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "type VARCHAR(255) NOT NULL,"
            + "state VARCHAR(255) NOT NULL,"
            + "UNIQUE (settlementId, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_BANISHED("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_banished ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "banished_id VARCHAR(128) NOT NULL,"
            + "banished_date BIGINT(11) NOT NULL,"
            + "banished_sender VARCHAR(36),"
            + "banished_reason TEXT,"
            + "UNIQUE list (settlementId, banished_id)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_UNBANISHED("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_unbanished ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "unbanished_id VARCHAR(128) NOT NULL,"
            + "unbanished_date BIGINT(11) NOT NULL,"
            + "unbanished_sender VARCHAR(36),"
            + "unbanished_reason TEXT,"
            + "unbanished_unbanisher VARCHAR(36),"
            + "unbanished_unbanishdate BIGINT(11) NOT NULL,"
            + "UNIQUE list (settlementId, unbanished_id, unbanished_date)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_ALLIES("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_allies ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "allies_id VARCHAR(36) NOT NULL,"
            + "allies_created BIGINT(11) NOT NULL, "
            + "allies_nearbyClaim BOOLEAN NOT NULL, "
            + "UNIQUE list (settlementId, allies_id)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_PERMISSIONS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_permissions ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "permission VARCHAR(255) NOT NULL,"
            + "value BOOLEAN NOT NULL,"
            + "UNIQUE (settlementId, rank, permission)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_TITLES("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_titles ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "UNIQUE (settlementId, rank, title)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_DEFAULTTITLES("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_defaulttitles ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "UNIQUE (settlementId, rank)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENTS("CREATE TABLE IF NOT EXISTS {tablePrefix}residents ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "uuid CHAR(36) NOT NULL, INDEX(uuid), UNIQUE (uuid),"
            + "name VARCHAR(16) NOT NULL,"
            + "settlementId INTEGER,"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE SET NULL,"
            + "settlementJoin BIGINT(11) NOT NULL,"
            + "rank VARCHAR(255),"
            + "title VARCHAR(16),"
            + "bonusClaims INTEGER,"
            + "missionPoints BIGINT,"
            + "lastLogin BIGINT(11) NOT NULL,"
            + "taxExempted BOOLEAN NOT NULL,"
            + "taxDebtAction VARCHAR(255) NOT NULL,"
            + "taxPaid NUMERIC(19,4) DEFAULT 0,"
            + "donated NUMERIC(19,4) DEFAULT 0,"
            + "activeChannel VARCHAR(255) NOT NULL,"
            + "swear BOOLEAN NOT NULL,"
            + "socialSpy BOOLEAN NOT NULL"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_CHANNELS("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_channels ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "channel VARCHAR(255),"
            + "UNIQUE (userId, channel)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_CHANNEL_COLORS("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_channel_colors ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "channel VARCHAR(255),"
            + "color VARCHAR(255),"
            + "UNIQUE (userId, channel)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_CHANNEL_PREFIX_COLORS("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_channel_prefix_colors ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "channel VARCHAR(255),"
            + "color VARCHAR(255),"
            + "UNIQUE (userId, channel)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_FRIENDS("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_friends ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "friendId INTEGER,"
            + "FOREIGN KEY (friendId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "UNIQUE (userId, friendId)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_IGNORES("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_ignores ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "ignoredId INTEGER,"
            + "FOREIGN KEY (ignoredId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "UNIQUE (userId, ignoredId)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    RESIDENT_MISSIONDATA("CREATE TABLE IF NOT EXISTS {tablePrefix}resident_missiondata ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "userId INTEGER, INDEX (userId),"
            + "FOREIGN KEY (userId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "mission VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "progress BIGINT DEFAULT 0,"
            + "UNIQUE list (userId, mission, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    INVITES("CREATE TABLE IF NOT EXISTS {tablePrefix}invites ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "type VARCHAR(255) NOT NULL, INDEX (type),"
            + "targetType VARCHAR(255) NOT NULL,"
            + "targetId INTEGER NOT NULL,"
            + "sourceType VARCHAR(255) NOT NULL,"
            + "sourceId INTEGER NOT NULL,"
            + "time BIGINT(11) NOT NULL"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    CLAIMS("CREATE TABLE IF NOT EXISTS {tablePrefix}claims ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "landOwnerType VARCHAR(64), INDEX(landOwnerType),"
            + "landOwnerId INTEGER NOT NULL, INDEX(landOwnerId),"
            + "world VARCHAR(255),INDEX(world),"
            + "x INTEGER,INDEX(x),"
            + "z INTEGER,INDEX(z),"
            + "UNIQUE(world, x, z),"
            + "unclaimIn INTEGER,"
            + "ownerId INTEGER,"
            + "FOREIGN KEY (ownerId) REFERENCES {tablePrefix}residents(id) ON DELETE SET NULL,"
            + "price NUMERIC(19,4) DEFAULT 0,"
            + "forSale BOOLEAN NOT NULL,"
            + "tax NUMERIC(19,4) DEFAULT 0,"
            + "embassy BOOLEAN NOT NULL,"
            + "name VARCHAR(255),"
            + "description TEXT,"
            + "buildState VARCHAR(255),"
            + "link VARCHAR(36)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    CLAIM_USES("CREATE TABLE IF NOT EXISTS {tablePrefix}claim_uses ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "claimId INTEGER, INDEX(claimId),"
            + "FOREIGN KEY (claimId) REFERENCES {tablePrefix}claims(id) ON DELETE CASCADE,"
            + "type VARCHAR(255) NOT NULL,"
            + "state VARCHAR(255) NOT NULL,"
            + "UNIQUE (claimId, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    CLAIM_MODES("CREATE TABLE IF NOT EXISTS {tablePrefix}claim_modes ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "claimId INTEGER, INDEX(claimId),"
            + "FOREIGN KEY (claimId) REFERENCES {tablePrefix}claims(id) ON DELETE CASCADE,"
            + "mode VARCHAR(255) NOT NULL,"
            + "enabled BOOLEAN NOT NULL,"
            + "UNIQUE (claimId, mode)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    CLAIM_MEMBERS("CREATE TABLE IF NOT EXISTS {tablePrefix}claim_members ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "claimId INTEGER, INDEX(claimId),"
            + "FOREIGN KEY (claimId) REFERENCES {tablePrefix}claims(id) ON DELETE CASCADE,"
            + "memberId INTEGER,"
            + "FOREIGN KEY (memberId) REFERENCES {tablePrefix}residents(id) ON DELETE CASCADE,"
            + "UNIQUE (claimId, memberId)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    LOGS("CREATE TABLE IF NOT EXISTS {tablePrefix}logs ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX(settlementId),"
            + "residentId INTEGER, INDEX(residentId),"
            + "`action` VARCHAR(255) NOT NULL,"
            + "type VARCHAR(255) NOT NULL,"
            + "amount BIGINT NOT NULL,"
            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

    private final String sql;

    MySqlTable(String sql) {
        this.sql = sql;
    }

    public String getSql(String tablePrefix) {
        return sql.replace("{tablePrefix}", tablePrefix);
    }

    public String getDbName() {
        return name().toLowerCase();
    }
}
