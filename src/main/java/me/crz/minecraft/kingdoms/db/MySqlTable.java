package me.crz.minecraft.kingdoms.db;

public enum MySqlTable {

    EMPIRES("CREATE TABLE IF NOT EXISTS {tablePrefix}empires ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "name VARCHAR(255) NOT NULL, INDEX (name), UNIQUE (name), "
            + "capitalId INTEGER NOT NULL,"
            + "FOREIGN KEY (capitalId) REFERENCES {tablePrefix}settlements(id) ON DELETE RESTRICT,"
            + "description TEXT NOT NULL,"
            + "discord TEXT NOT NULL,"
            + "created BIGINT,"
            + "lastRename BIGINT,"
            + "symbol VARCHAR(16) NOT NULL,"
            + "access VARCHAR(255) NOT NULL,"
            + "buildState VARCHAR(255) NOT NULL,"
            + "pvp BOOLEAN NOT NULL,"
            + "fireSpread BOOLEAN NOT NULL,"
            + "explosions BOOLEAN NOT NULL,"
            + "mobSpawning BOOLEAN NOT NULL,"
            + "buff BOOLEAN NOT NULL,"
            + "bank BIGINT,"
            + "joinFee BIGINT,"
            + "tax BIGINT,"
            + "taxDebtAction VARCHAR(255) NOT NULL,"
            + "bonusClaims INTEGER,"
            + "homeLoc_world VARCHAR(255),"
            + "homeLoc_x DOUBLE,"
            + "homeLoc_y DOUBLE,"
            + "homeLoc_z DOUBLE,"
            + "homeLoc_yaw FLOAT,"
            + "homeLoc_pitch FLOAT"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_BANISHED("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_banished ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX (empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "banished VARCHAR(36),"
            + "UNIQUE list (empireId, banished)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_ALLIES("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_allies ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX (empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "allies VARCHAR(36),"
            + "UNIQUE list (empireId, allies)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_VAULTS("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_vaults ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "content VARCHAR(255) NOT NULL,"
            + "amount BIGINT,"
            + "UNIQUE (empireId, content)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_PERKS("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_perks ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "perk VARCHAR(255) NOT NULL,"
            + "times INTEGER NOT NULL,"
            + "lastUsed BIGINT,"
            + "active BOOLEAN,"
            + "UNIQUE (empireId, perk)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_USES("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_uses ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "type VARCHAR(255) NOT NULL,"
            + "state VARCHAR(255) NOT NULL,"
            + "UNIQUE (empireId, type)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_PERMISSIONS("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_permissions ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "permission VARCHAR(255) NOT NULL,"
            + "value BOOLEAN NOT NULL,"
            + "UNIQUE (empireId, rank, permission)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_TITLES("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_titles ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "UNIQUE (empireId, rank, title)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    EMPIRE_DEFAULTTITLES("CREATE TABLE IF NOT EXISTS {tablePrefix}empire_defaulttitles ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE CASCADE,"
            + "rank VARCHAR(255) NOT NULL,"
            + "title VARCHAR(16) NOT NULL,"
            + "UNIQUE (empireId, rank)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENTS("CREATE TABLE IF NOT EXISTS {tablePrefix}settlements ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "name VARCHAR(255) NOT NULL, INDEX (name), UNIQUE (name), "
            + "chiefId INTEGER NOT NULL,"
            + "FOREIGN KEY (chiefId) REFERENCES {tablePrefix}residents(id) ON DELETE RESTRICT,"
            + "level INTEGER NOT NULL,"
            + "settlementState VARCHAR(255) NOT NULL,"
            + "empireId INTEGER,"
            + "FOREIGN KEY (empireId) REFERENCES {tablePrefix}empires(id) ON DELETE SET NULL,"
            + "description TEXT NOT NULL,"
            + "discord TEXT NOT NULL,"
            + "created BIGINT,"
            + "access VARCHAR(255) NOT NULL,"
            + "buildState VARCHAR(255) NOT NULL,"
            + "allowAllyClaim BOOLEAN NOT NULL,"
            + "pvp BOOLEAN NOT NULL,"
            + "fireSpread BOOLEAN NOT NULL,"
            + "explosions BOOLEAN NOT NULL,"
            + "mobSpawning BOOLEAN NOT NULL,"
            + "buff BOOLEAN NOT NULL,"
            + "bank BIGINT,"
            + "joinFee BIGINT,"
            + "tax BIGINT,"
            + "taxDebtAction VARCHAR(255) NOT NULL,"
            + "bonusClaims INTEGER,"
            + "homeLoc_world VARCHAR(255),"
            + "homeLoc_x DOUBLE,"
            + "homeLoc_y DOUBLE,"
            + "homeLoc_z DOUBLE,"
            + "homeLoc_yaw FLOAT,"
            + "homeLoc_pitch FLOAT"
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
            + "banished VARCHAR(36),"
            + "UNIQUE list (settlementId, banished)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    SETTLEMENT_ALLIES("CREATE TABLE IF NOT EXISTS {tablePrefix}settlement_allies ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER, INDEX (settlementId),"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE CASCADE,"
            + "allies VARCHAR(36),"
            + "UNIQUE list (settlementId, allies)"
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
            + "uuid CHAR(36) NOT NULL, UNIQUE (uuid),"
            + "name VARCHAR(16) NOT NULL,"
            + "settlementId INTEGER,"
            + "FOREIGN KEY (settlementId) REFERENCES {tablePrefix}settlements(id) ON DELETE SET NULL,"
            + "settlementJoin BIGINT(11) NOT NULL,"
            + "rank VARCHAR(255),"
            + "empirerank VARCHAR(255),"
            + "title VARCHAR(16),"
            + "empireTitle VARCHAR(16),"
            + "bonusClaims INTEGER,"
            + "lastLogin BIGINT(11) NOT NULL,"
            + "activeChannel VARCHAR(255) NOT NULL,"
            + "swear BOOLEAN NOT NULL"
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

    CLAIMS("CREATE TABLE IF NOT EXISTS {tablePrefix}claims ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "settlementId INTEGER NOT NULL, INDEX (settlementId), "
            + "world VARCHAR(255),INDEX(world),"
            + "x INTEGER,INDEX(x),"
            + "z INTEGER,INDEX(z),"
            + "UNIQUE(world, x, z),"
            + "unclaimIn INTEGER,"
            + "ownerId INTEGER,"
            + "FOREIGN KEY (ownerId) REFERENCES {tablePrefix}residents(id) ON DELETE SET NULL,"
            + "price INTEGER DEFAULT 0,"
            + "forSale BOOLEAN NOT NULL,"
            + "tax INTEGER DEFAULT 0,"
            + "embassy BOOLEAN NOT NULL,"
            + "name VARCHAR(255),"
            + "description TEXT,"
            + "buildState VARCHAR(255) NOT NULL,"
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

    PORTALS("CREATE TABLE IF NOT EXISTS {tablePrefix}portals ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "world VARCHAR(255),"
            + "x INTEGER,"
            + "y INTEGER,"
            + "z INTEGER,"
            + "UNIQUE(world, y, x, z),"
            + "direction CHAR(1)" // direction the portal is pointing: N,E,S,W
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    PORTAL_LINKS("CREATE TABLE IF NOT EXISTS {tablePrefix}portal_links ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "sourceId INTEGER, INDEX (sourceId),"
            + "FOREIGN KEY (sourceId) REFERENCES {tablePrefix}portals(id) ON DELETE CASCADE,"
            + "targetId INTEGER,"
            + "FOREIGN KEY (targetId) REFERENCES {tablePrefix}portals(id) ON DELETE CASCADE,"
            + "UNIQUE(sourceId, targetId)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    LOCKS("CREATE TABLE IF NOT EXISTS {tablePrefix}locks ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "world VARCHAR(255),INDEX(world),"
            + "x INTEGER,INDEX(x),"
            + "y INTEGER,INDEX(y),"
            + "z INTEGER,INDEX(z),"
            + "UNIQUE(world, y, x, z),"
            + "ownerId INTEGER,"
            + "FOREIGN KEY (ownerId) REFERENCES {tablePrefix}residents(id) ON DELETE SET NULL,"
            + "`block` VARCHAR(255) NOT NULL,"
            + "allowHoppers BOOLEAN NOT NULL"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    LOCK_ALLOWED("CREATE TABLE IF NOT EXISTS {tablePrefix}lock_allowed ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "lockId INTEGER, INDEX (lockId),"
            + "FOREIGN KEY (lockId) REFERENCES {tablePrefix}locks(id) ON DELETE CASCADE,"
            + "allowed VARCHAR(320)"
            + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;"),

    LOGS("CREATE TABLE IF NOT EXISTS {tablePrefix}logs ("
            + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
            + "empireId INTEGER, INDEX(empireId),"
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
