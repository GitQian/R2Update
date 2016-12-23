package com.xinzhihui.r2update.bean;

/**
 * Created by Administrator on 2016/12/22.
 */

public class CheckUpdateBean {
    public String status;
    public String message;
    public int errorCode;
    public Data data;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        public String packMD5;

        public int packSize;

        public String detailDesc;

        public String packUrl;

        public String pubTime;

        public String newVersion;

        public String oldVersion;

        public String upgradeType;

        public void setPackMD5(String packMD5) {
            this.packMD5 = packMD5;
        }

        public String getPackMD5() {
            return this.packMD5;
        }

        public void setPackSize(int packSize) {
            this.packSize = packSize;
        }

        public int getPackSize() {
            return this.packSize;
        }

        public void setDetailDesc(String detailDesc) {
            this.detailDesc = detailDesc;
        }

        public String getDetailDesc() {
            return this.detailDesc;
        }

        public void setPackUrl(String packUrl) {
            this.packUrl = packUrl;
        }

        public String getPackUrl() {
            return this.packUrl;
        }

        public void setPubTime(String pubTime) {
            this.pubTime = pubTime;
        }

        public String getPubTime() {
            return this.pubTime;
        }

        public void setNewVersion(String newVersion) {
            this.newVersion = newVersion;
        }

        public String getNewVersion() {
            return this.newVersion;
        }

        public void setOldVersion(String oldVersion) {
            this.oldVersion = oldVersion;
        }

        public String getOldVersion() {
            return this.oldVersion;
        }

        public void setUpgradeType(String upgradeType) {
            this.upgradeType = upgradeType;
        }

        public String getUpgradeType() {
            return this.upgradeType;
        }

    }
}
