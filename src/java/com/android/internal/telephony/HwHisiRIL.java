/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import android.content.Context;
import android.net.LocalSocket;
import android.os.AsyncResult;
import android.os.Message;
import android.os.Parcel;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.RILRequestReference;
import com.android.internal.telephony.uicc.IccIoResult;

public class HwHisiRIL
extends RIL {
    private static final boolean RILJ_LOGD = true;
    private static final boolean RILJ_LOGV = true;
    private static final String RILJ_LOG_TAG = "RILJ-HwHisiRIL";
    private static final boolean SHOW_4G_PLUS_ICON = SystemProperties.getBoolean((String)"ro.config.hw_show_4G_Plus_icon", (boolean)false);
    private int mBalongSimSlot = 0;
    private Integer mRilInstanceId = null;

    public HwHisiRIL(Context context, int n, int n2) {
        super(context, n, n2, null);
    }

    public HwHisiRIL(Context context, int n, int n2, Integer n3) {
        super(context, n, n2, n3);
        this.mRilInstanceId = n3;
    }

    static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 65 + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 97 + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static byte[] hexStringToBcd(String string) {
        if (string == null) {
            return new byte[0];
        }
        int n = string.length();
        byte[] arrby = new byte[n / 2];
        int n2 = 0;
        do {
            byte[] arrby2 = arrby;
            if (n2 >= n) return arrby2;
            arrby[n2 / 2] = (byte)(HwHisiRIL.hexCharToInt(string.charAt(n2 + 1)) << 4 | HwHisiRIL.hexCharToInt(string.charAt(n2)));
            n2 += 2;
        } while (true);
    }

    static String requestToString(int n) {
        Rlog.d((String)"RILJ-HwHisiRIL", (String)"Enter HwHisiRIL requestToString,");
        switch (n) {
            default: {
                return "<unknown request>";
            }
            case 2019: {
                return "RIL_REQUEST_HW_SET_VOICECALL_BACKGROUND_STATE";
            }
            case 528: {
                return "RIL_REQUEST_HW_QUERY_CARDTYPE";
            }
            case 2028: {
                return "RIL_REQUEST_HW_SET_SIM_SLOT_CFG";
            }
            case 2029: {
                return "RIL_REQUEST_HW_GET_SIM_SLOT_CFG";
            }
            case 2032: {
                return "RIL_REQUEST_HW_SIM_GET_ATR";
            }
            case 2088: {
                return "RIL_REQUEST_HW_SET_ACTIVE_MODEM_MODE";
            }
            case 2006: {
                return "SIM_TRANSMIT_BASIC";
            }
            case 2007: {
                return "SIM_OPEN_CHANNEL";
            }
            case 2008: {
                return "SIM_CLOSE_CHANNEL";
            }
            case 2009: {
                return "SIM_TRANSMIT_CHANNEL";
            }
            case 2108: {
                return "RIL_REQUEST_HW_SET_LTE_RELEASE_VERSION";
            }
            case 2109: {
                return "RIL_REQUEST_HW_GET_LTE_RELEASE_VERSION";
            }
            case 2075: {
                return "RIL_REQUEST_HW_GET_ICCID";
            }
            case 2037: {
                return "RIL_REQUEST_HW_VSIM_SET_SIM_STATE";
            }
            case 2038: {
                return "RIL_REQUEST_HW_VSIM_GET_SIM_STATE";
            }
            case 2093: {
                return "RIL_REQUEST_HW_SET_TEE_DATA_READY_FLAG";
            }
            case 2094: {
                return "RIL_REQUEST_HW_SWITCH_SIM_SLOT_WITHOUT_RESTART_RILD";
            }
            case 2119: {
                return "RIL_REQUEST_HW_SET_UE_OPERATION_MODE";
            }
            case 2022: 
        }
        return "RIL_REQUEST_HW_SET_NETWORK_RAT_AND_SRVDOMAIN_CFG";
    }

    private Object responseICCID(Parcel parcel) {
        return HwHisiRIL.hexStringToBcd(parcel.readString());
    }

    private Object responseICC_IO(Parcel object) {
        int n = object.readInt();
        int n2 = object.readInt();
        object = object.readString();
        Rlog.d((String)"RILJ-HwHisiRIL", (String)("< iccIO:  0x" + Integer.toHexString(n) + " 0x" + Integer.toHexString(n2) + " " + (String)object));
        return new IccIoResult(n, n2, (String)object);
    }

    private Object responseInts(Parcel parcel) {
        int n = parcel.readInt();
        int[] arrn = new int[n];
        for (int i = 0; i < n; ++i) {
            arrn[i] = parcel.readInt();
        }
        return arrn;
    }

    private Object responseRaw(Parcel parcel) {
        return parcel.createByteArray();
    }

    private Object responseString(Parcel parcel) {
        return parcel.readString();
    }

    private Object responseStrings(Parcel parcel) {
        int n = parcel.readInt();
        String[] arrstring = new String[n];
        for (int i = 0; i < n; ++i) {
            arrstring[i] = parcel.readString();
        }
        return arrstring;
    }

    private Object responseVoid(Parcel parcel) {
        return null;
    }

    /*
     * Enabled aggressive block sorting
     */
    private void riljLog(String string) {
        StringBuilder stringBuilder = new StringBuilder().append(string);
        string = this.mRilInstanceId != null ? " [SUB" + this.mRilInstanceId + "]" : "";
        Rlog.d((String)"RILJ-HwHisiRIL", (String)stringBuilder.append(string).toString());
    }

    private void unsljLog(int n) {
        this.riljLog("[UNSL]< " + this.unsolResponseToString(n));
    }

    private String unsolResponseToString(int n) {
        switch (n) {
            default: {
                return "<unknown response>:" + n;
            }
            case 3032: {
                return "UNSOL_HOOK_HW_VP_STATUS";
            }
            case 3037: 
        }
        return "UNSOL_HW_CA_STATE_CHANGED";
    }

    public void getBalongSim(Message message) {
        message = RILRequestReference.obtain((int)2029, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public String getHwPrlVersion() {
        return SystemProperties.get((String)"persist.radio.hwprlversion", (String)"0");
    }

    public String getHwUimid() {
        return SystemProperties.get((String)"persist.radio.hwuimid", (String)"0");
    }

    public void getICCID(Message message) {
        message = RILRequestReference.obtain((int)2075, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void getLocationInfo(Message message) {
        message = RILRequestReference.obtain((int)534, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void getLteReleaseVersion(Message message) {
        message = RILRequestReference.obtain((int)2109, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void getSimHotPlugState(Message message) {
        message = RILRequestReference.obtain((int)533, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void getSimState(Message message) {
        message = RILRequestReference.obtain((int)2038, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void handleUnsolicitedDefaultMessage(int n, Object object, Context context) {
        super.handleUnsolicitedDefaultMessage(n, object, context);
        switch (n) {
            default: {
                return;
            }
            case 3032: {
                this.unsljLog(n);
                this.notifyVpStatus((byte[])object);
                return;
            }
            case 3037: {
                this.unsljLog(n);
                if (!SHOW_4G_PLUS_ICON) return;
                this.mCaStateChangedRegistrants.notifyRegistrants(new AsyncResult((Object)null, object, null));
                return;
            }
        }
    }

    protected Object handleUnsolicitedDefaultMessagePara(int n, Parcel parcel) {
        Object object = super.handleUnsolicitedDefaultMessagePara(n, parcel);
        if (object != null) {
            return object;
        }
        switch (n) {
            default: {
                return object;
            }
            case 3031: {
                return null;
            }
            case 3032: {
                return this.responseRaw(parcel);
            }
            case 3037: 
        }
        return this.responseInts(parcel);
    }

    public void hotSwitchSimSlot(int n, int n2, int n3, Message message) {
        message = RILRequestReference.obtain((int)2094, (Message)message);
        message.getParcel().writeInt(3);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        message.getParcel().writeInt(n3);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()) + "modem0: " + n + " modem1: " + n2 + " modem2: " + n3);
        this.send((RILRequestReference)message);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void hotSwitchSimSlotFor2Modem(int n, int n2, int n3, Message message) {
        this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem modem0=" + n + " modem1=" + n2 + " modem2=" + n3);
        if (1 == n2 && n == 0 && 2 == n3 || 2 == n && n3 == 0) {
            n3 = 0;
            n2 = 1;
            if (n == 0) {
                this.mRilSocketMaps[0] = 0;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 2;
                this.mRilSocketMaps[1] = 1;
                this.mRilSocketMaps[2] = 0;
            }
            this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[0]=" + this.mRilSocketMaps[0] + " mRilSocketMaps[1]=" + this.mRilSocketMaps[1] + " mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
            n = n3;
        } else {
            if (!(n2 == 0 && 1 == n && 2 == n3 || 2 == n && 1 == n3)) {
                this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem error branch!");
                return;
            }
            n3 = 1;
            n2 = 0;
            if (1 == n) {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 0;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 2;
                this.mRilSocketMaps[2] = 0;
            }
            this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[0]=" + this.mRilSocketMaps[0]);
            this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[1]=" + this.mRilSocketMaps[1]);
            this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem set mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
            n = n3;
        }
        this.mRilSocketMapEnable = this.mRilSocketMaps[0] == 2 || this.mRilSocketMaps[1] == 2;
        if (this.mSocket == null) {
            this.notifyPendingRilSocket();
            this.riljLog("[2Cards]hotSwitchSimSlotFor2Modem notify mPendingRilSocketLock!");
            this.mResultMessage = message;
            return;
        }
        RILRequestReference rILRequestReference = RILRequestReference.obtain((int)2094, (Message)null);
        rILRequestReference.getParcel().writeInt(2);
        rILRequestReference.getParcel().writeInt(n);
        rILRequestReference.getParcel().writeInt(n2);
        this.mResultMessage = message;
        this.send(rILRequestReference);
        this.riljLog(rILRequestReference.serialString() + "[2Cards]> " + HwHisiRIL.requestToString(rILRequestReference.getRequest()) + " sendData1: " + n + " sendData2: " + n2);
    }

    public void iccCloseChannel(int n, Message message) {
        message = RILRequestReference.obtain((int)2008, (Message)message);
        message.getParcel().writeInt(1);
        message.getParcel().writeInt(n);
        this.riljLog(message.serialString() + "> iccCloseChannel: " + HwHisiRIL.requestToString(message.getRequest()) + " " + n);
        this.send((RILRequestReference)message);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void iccExchangeAPDU(int n, int n2, int n3, int n4, int n5, int n6, String string, Message message) {
        message = n3 == 0 ? RILRequestReference.obtain((int)2006, (Message)message) : RILRequestReference.obtain((int)2009, (Message)message);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        message.getParcel().writeInt(n3);
        message.getParcel().writeString(null);
        message.getParcel().writeInt(n4);
        message.getParcel().writeInt(n5);
        message.getParcel().writeInt(n6);
        message.getParcel().writeString(string);
        message.getParcel().writeString(null);
        this.riljLog(message.serialString() + "> iccExchangeAPDU: " + HwHisiRIL.requestToString(message.getRequest()) + " 0x" + Integer.toHexString(n) + " 0x" + Integer.toHexString(n2) + " 0x" + Integer.toHexString(n3) + " " + n4 + "," + n5 + "," + n6);
        this.send((RILRequestReference)message);
    }

    public void iccGetATR(Message message) {
        message = RILRequestReference.obtain((int)2032, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void iccOpenChannel(String string, Message message) {
        message = RILRequestReference.obtain((int)2007, (Message)message);
        message.getParcel().writeString(string);
        this.riljLog(message.serialString() + "> iccOpenChannel: " + HwHisiRIL.requestToString(message.getRequest()) + " " + string);
        this.send((RILRequestReference)message);
    }

    @Override
    protected void notifyVpStatus(byte[] asyncResult) {
        int n = asyncResult.length;
        Rlog.d((String)"RILJ-HwHisiRIL", (String)("notifyVpStatus: len = " + n));
        if (1 != n) {
            return;
        }
        asyncResult = new AsyncResult((Object)null, (Object)asyncResult, null);
        this.mReportVpStatusRegistrants.notifyRegistrants(asyncResult);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected Object processSolicitedEx(int n, Parcel object) {
        Object object2 = super.processSolicitedEx(n, (Parcel)object);
        if (object2 != null) {
            return object2;
        }
        switch (n) {
            default: {
                return object2;
            }
            case 2019: {
                return this.responseVoid((Parcel)object);
            }
            case 2032: {
                return this.responseString((Parcel)object);
            }
            case 2029: {
                return this.responseSimSlot((Parcel)object);
            }
            case 2088: {
                return this.responseInts((Parcel)object);
            }
            case 2006: {
                return this.responseICC_IO((Parcel)object);
            }
            case 2007: {
                return this.responseInts((Parcel)object);
            }
            case 2008: {
                return this.responseVoid((Parcel)object);
            }
            case 2009: {
                return this.responseICC_IO((Parcel)object);
            }
            case 2108: {
                return this.responseVoid((Parcel)object);
            }
            case 2109: {
                return this.responseInts((Parcel)object);
            }
            case 2075: {
                return this.responseICCID((Parcel)object);
            }
            case 2037: {
                return this.responseInts((Parcel)object);
            }
            case 2038: {
                return this.responseInts((Parcel)object);
            }
            case 2093: {
                return this.responseInts((Parcel)object);
            }
            case 2094: {
                object = object2 = this.responseVoid((Parcel)object);
                if (!this.isPlatformTwoModems()) return object;
                this.shouldBreakRilSocket = true;
                return object2;
            }
            case 2119: {
                return this.responseVoid((Parcel)object);
            }
            case 2022: {
                return this.responseVoid((Parcel)object);
            }
            case 534: 
        }
        return this.responseStrings((Parcel)object);
    }

    public void queryCardType(Message message) {
        message = RILRequestReference.obtain((int)528, (Message)message);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public Object responseSimSlot(Parcel parcel) {
        int n = parcel.readInt();
        int[] arrn = new int[n];
        for (int i = 0; i < n; ++i) {
            arrn[i] = parcel.readInt();
        }
        this.mBalongSimSlot = arrn[0];
        return arrn;
    }

    public void setActiveModemMode(int n, Message message) {
        message = RILRequestReference.obtain((int)2088, (Message)message);
        message.getParcel().writeInt(1);
        message.getParcel().writeInt(n);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void setLTEReleaseVersion(boolean bl, Message object) {
        int n = 1;
        RILRequestReference rILRequestReference = RILRequestReference.obtain((int)2108, (Message)object);
        rILRequestReference.getParcel().writeInt(1);
        object = rILRequestReference.getParcel();
        if (!bl) {
            n = 0;
        }
        object.writeInt(n);
        StringBuilder stringBuilder = new StringBuilder().append(rILRequestReference.serialString()).append("> ").append(HwHisiRIL.requestToString(rILRequestReference.getRequest()));
        object = bl ? " CA function on" : " CA function off";
        this.riljLog(stringBuilder.append((String)object).toString());
        this.send(rILRequestReference);
    }

    public void setNetworkRatAndSrvDomainCfg(int n, int n2, Message message) {
        message = RILRequestReference.obtain((int)2022, (Message)message);
        message.getParcel().writeInt(2);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()) + " rat: " + n + " srvDomain: " + n2);
        this.send((RILRequestReference)message);
    }

    /*
     * Enabled aggressive block sorting
     */
    public void setSimState(int n, int n2, Message message) {
        message = RILRequestReference.obtain((int)2037, (Message)message);
        if (this.isPlatformTwoModems()) {
            message.getParcel().writeInt(3);
            message.getParcel().writeInt(n);
            message.getParcel().writeInt(n2);
            message.getParcel().writeInt(1);
        } else {
            message.getParcel().writeInt(2);
            message.getParcel().writeInt(n);
            message.getParcel().writeInt(n2);
        }
        this.riljLog(message.serialString() + "> setSimState: " + HwHisiRIL.requestToString(message.getRequest()) + " index= " + n + ", enable = " + n2);
        this.send((RILRequestReference)message);
    }

    public void setTEEDataReady(int n, int n2, int n3, Message message) {
        message = RILRequestReference.obtain((int)2093, (Message)message);
        message.getParcel().writeInt(3);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        message.getParcel().writeInt(n3);
        this.riljLog(message.serialString() + "> setTEEDataReady: " + HwHisiRIL.requestToString(message.getRequest()) + " apn= " + n + ", dh = " + n2 + ", sim = " + n3);
        this.send((RILRequestReference)message);
    }

    public void setUEOperationMode(int n, Message message) {
        message = RILRequestReference.obtain((int)2119, (Message)message);
        message.getParcel().writeInt(1);
        message.getParcel().writeInt(n);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    public void switchBalongSim(int n, int n2, int n3, Message message) {
        message = RILRequestReference.obtain((int)2028, (Message)message);
        message.getParcel().writeInt(3);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        message.getParcel().writeInt(n3);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()) + ", modem1ToSlot: " + n + " modem2ToSlot: " + n2 + " modem3ToSlot: " + n3);
        this.send((RILRequestReference)message);
    }

    public void switchBalongSim(int n, int n2, Message message) {
        message = RILRequestReference.obtain((int)2028, (Message)message);
        message.getParcel().writeInt(2);
        message.getParcel().writeInt(n);
        message.getParcel().writeInt(n2);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()) + ", modem1ToSlot: " + n + " modem2ToSlot: " + n2 + "currentSimSlot: " + this.mBalongSimSlot);
        this.send((RILRequestReference)message);
    }

    public void switchVoiceCallBackgroundState(int n, Message message) {
        message = RILRequestReference.obtain((int)2019, (Message)message);
        message.getParcel().writeInt(1);
        message.getParcel().writeInt(n);
        this.riljLog(message.serialString() + "> " + HwHisiRIL.requestToString(message.getRequest()));
        this.send((RILRequestReference)message);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public boolean updateSocketMapForSlaveSub(int var1_1, int var2_2, int var3_3) {
        this.riljLog("[2Cards]updateSocketMapForSlaveSub modem0=" + var1_1 + " modem1=" + var2_2 + " modem2=" + var3_3);
        if ((1 != var2_2 || var1_1 != 0 || 2 != var3_3) && (2 != var1_1 || var3_3 != 0)) ** GOTO lbl12
        if (var1_1 == 0) {
            this.mRilSocketMaps[0] = 0;
            this.mRilSocketMaps[1] = 1;
            this.mRilSocketMaps[2] = 2;
        } else {
            this.mRilSocketMaps[0] = 2;
            this.mRilSocketMaps[1] = 1;
            this.mRilSocketMaps[2] = 0;
        }
        ** GOTO lbl21
lbl12: // 1 sources:
        if (var2_2 == 0 && 1 == var1_1 && 2 == var3_3 || 2 == var1_1 && 1 == var3_3) {
            if (1 == var1_1) {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 0;
                this.mRilSocketMaps[2] = 2;
            } else {
                this.mRilSocketMaps[0] = 1;
                this.mRilSocketMaps[1] = 2;
                this.mRilSocketMaps[2] = 0;
            }
lbl21: // 4 sources:
            this.riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[0]=" + this.mRilSocketMaps[0]);
            this.riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[1]=" + this.mRilSocketMaps[1]);
            this.riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMaps[2]=" + this.mRilSocketMaps[2]);
            if (this.mRilSocketMaps[0] != 2 && this.mRilSocketMaps[1] != 2) {
                this.mRilSocketMapEnable = false;
                this.riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable false!");
                return true;
            }
            this.mRilSocketMapEnable = true;
            this.riljLog("[2Cards]updateSocketMapForSlaveSub set mRilSocketMapEnable true!");
            return true;
        }
        this.riljLog("[2Cards]updateSocketMapForSlaveSub error branch!");
        return false;
    }
}

