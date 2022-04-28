package com.lakala.htk;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class HtkParams implements Parcelable {
    private String title;
    private String url;
    private String appType;
    private String temporaryToken;
    private String channelId;
    private int statusColor;
    private int backColor;
    private boolean needToolBar = false;
    private JSONObject ext = new JSONObject();

    public HtkParams() {

    }

    protected HtkParams(Parcel in) {
        title = in.readString();
        url = in.readString();
        appType = in.readString();
        temporaryToken = in.readString();
        channelId = in.readString();
        statusColor = in.readInt();
        backColor = in.readInt();
        needToolBar = in.readInt() == 1;
        try {
            ext = new JSONObject(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(appType);
        dest.writeString(temporaryToken);
        dest.writeString(channelId);
        dest.writeInt(statusColor);
        dest.writeInt(backColor);
        dest.writeInt(needToolBar ? 1 : 0);
        if (ext != null) {
            dest.writeString(ext.toString());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HtkParams> CREATOR = new Creator<HtkParams>() {
        @Override
        public HtkParams createFromParcel(Parcel in) {
            return new HtkParams(in);
        }

        @Override
        public HtkParams[] newArray(int size) {
            return new HtkParams[size];
        }
    };

    public int getBackColor() {
        return backColor;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public int getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(int statusColor) {
        this.statusColor = statusColor;
    }

    public JSONObject getExt() {
        return ext;
    }

    public void setExt(JSONObject ext) {
        this.ext = ext;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getTemporaryToken() {
        return temporaryToken;
    }

    public void setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isNeedToolBar() {
        return needToolBar;
    }

    public void setNeedToolBar(boolean needToolBar) {
        this.needToolBar = needToolBar;
    }
}
