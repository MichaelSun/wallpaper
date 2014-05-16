package com.dajie.vcard.dao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by zhangdi on 13-12-16.
 */
public class VcardDaoGenerator {

    private static final String PACKAGE_NAME = "com.michael.qrcode.dao.model";

    private static final int VERSION = 3;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION, PACKAGE_NAME);

        generateScanHistoryDao(schema);
//        generateMessageDao(schema);
//        generateUserProfileDao(schema);
//        generateUserCountDao(schema);
//        generateFriendInfoDao(schema);

        new DaoGenerator().generateAll(schema, "../../src");
    }

    private static void generateScanHistoryDao(Schema schema) {
        Entity entity = schema.addEntity("ScanHistory");
        entity.addLongProperty("id").primaryKey().autoincrement();
        entity.addStringProperty("title").notNull();
        entity.addStringProperty("content").notNull();
        entity.addLongProperty("timestamp").notNull();
        entity.setHasKeepSections(true);
    }

    private static void generateMessageDao(Schema schema) {
        Entity entity = schema.addEntity("VcardMessage");
        entity.addLongProperty("messageId").primaryKey();
        entity.addIntProperty("fromUsrId").notNull();
        entity.addIntProperty("toUsrId").notNull();
        entity.addIntProperty("type").notNull();
        entity.addLongProperty("createTime").notNull();
        entity.addStringProperty("content").notNull();
        entity.addStringProperty("payload");
        entity.addStringProperty("fromUsrName").notNull();
        entity.addStringProperty("fromUsrAvator").notNull();
        entity.addIntProperty("readStatus");
        entity.addBooleanProperty("hasSolved");
        entity.setHasKeepSections(true);
    }

    private static void generateFriendInfoDao(Schema schema) {
        Entity entity = schema.addEntity("FriendInfo");
        entity.addLongProperty("userId").primaryKey();
        entity.addStringProperty("headerUrl");
        entity.addStringProperty("name");
        entity.addStringProperty("namePinyin");
        entity.addStringProperty("description");
        entity.addStringProperty("company");
        entity.addStringProperty("companyPosition");
        entity.addLongProperty("relationType");
        entity.addBooleanProperty("hasShareRecent");
        entity.addLongProperty("recentShareTime");
    }

    private static void generateUserProfileDao(Schema schema) {
        Entity entity = schema.addEntity("UserProfile");
        entity.addIntProperty("userId").notNull().unique();
        entity.addStringProperty("wikaId");
        entity.addStringProperty("name");
        entity.addIntProperty("gender");
        entity.addStringProperty("avatar");
        entity.addStringProperty("faceQrcode");
        entity.addIntProperty("wikaTemplate");
        entity.addStringProperty("email");
        entity.addStringProperty("mobile");
        entity.addStringProperty("location");
        entity.addStringProperty("corp");
        entity.addIntProperty("industry");
        entity.addStringProperty("position");
        entity.addIntProperty("jobType");
        entity.addIntProperty("userValue");
        entity.addStringProperty("userValues");
        entity.addStringProperty("introduce");
        entity.addLongProperty("birth");
        entity.addStringProperty("department");
        entity.addIntProperty("relationType");

        entity.setHasKeepSections(true);
    }

    private static void generateUserCountDao(Schema schema) {
        Entity entity = schema.addEntity("UserCount");
        entity.addIntProperty("userId").notNull().unique();
        entity.addFloatProperty("rank");
        entity.addIntProperty("followingCount");
        entity.addIntProperty("followerCount");
        entity.addIntProperty("friendCount");
        entity.addIntProperty("visitiedCount");
        entity.addIntProperty("yesterdayFollowingCount");
        entity.addIntProperty("yesterdayFollowerCount");
        entity.addIntProperty("userInfoValue");
        entity.addIntProperty("shareWikaValue");
        entity.addIntProperty("visitedValue");
        entity.addIntProperty("changeQrValue");
        entity.addIntProperty("changeWikaValue");
        entity.addIntProperty("followingValue");
        entity.addIntProperty("totalValue");
        entity.addStringProperty("userValues");

        entity.setHasKeepSections(true);
    }
}
