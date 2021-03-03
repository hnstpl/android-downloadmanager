package com.hns.download;
import java.text.DecimalFormat;


public final class CommonUtils {
    private CommonUtils() {
    }
    public static String convertFileLengthFromkbToKBMB(long size) {
        DecimalFormat df = new DecimalFormat("0.0");
        DecimalFormat df2 = new DecimalFormat("0");
        DecimalFormat df3 = new DecimalFormat("0.00");
        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;
        if (size < sizeMo)
            return df2.format(size / sizeKb) + " KB";
        else if (size < sizeGo)
            return df.format(size / sizeMo) + " MB";
        else if (size < sizeTerra)
            return df3.format(size / sizeGo) + " GB";
        return "";
    }

}
