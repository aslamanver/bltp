package com.aslam.bltprinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class BitmapReceipt {

    public static Bitmap generateReceipt(Context context) {

        Bitmap logoImg = BitmapFactory.decodeResource(context.getResources(), R.drawable.seylan_logo);

        String mName = "Test PAX";
        String mAddress1 = "No 192, Dehiwala";
        String mAddress2 = "Colombo";

        String mId = "25666";
        String tId = "12222222";
        String bId = "256633";
        String dateTime = "2011-11-11";
        String appCode = "T5889999999";
        String exRate = "1 USD = 178.14785 LKR";
        String totalAmount = "1500.00 USD";

        String divText = "........................................................................";

        String dummy = "0000";

        int bWidth = 400;
        int bHeight = 800;

        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888);
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        bitmap.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        // text color - #3D3D3D
        paint.setColor(Color.BLACK);
        // text size in pixels
        paint.setTextSize(17);

        // draw text to the Canvas center
        int y = 30;
        int x = 15;
        int yConstant = 22;

        // Bitmap logoCarg = BitmapFactory.decodeResource(getResources(), R.drawable.print_cargills);
        canvas.drawBitmap(logoImg, null, new Rect(x, y, bWidth - 50, 100), null);

        // Bitmap logoSuperm = BitmapFactory.decodeResource(getResources(), R.drawable.print_supreme);
        // canvas.drawBitmap(logoSuperm, null, new Rect((bWidth / 2) + 50, y, bWidth - 30, 70), null);
        //
        // Bitmap logoAliPay = BitmapFactory.decodeResource(getResources(), R.drawable.print_alipay);
        // canvas.drawBitmap(logoAliPay, null, new Rect((bWidth / 2) - 40, y + 60, (bWidth / 2) + 70, y + 95), null);

        y = y + yConstant + 115;
        paint.setTextSize(22);
        canvas.drawText(mName, getApproxXToCenterText(mName, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, bWidth), y, paint);
        y = y + yConstant + 5;
        canvas.drawText(mAddress1, getApproxXToCenterText(mAddress1, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, 350), y, paint);
        y = y + yConstant + 5;
        canvas.drawText(mAddress2, getApproxXToCenterText(mAddress2, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, 350), y, paint);

        paint.setTextSize(17);

        y = y + yConstant + 10;
        canvas.drawText(divText, getApproxXToCenterText(divText, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, bWidth), y, paint);

        y = y + yConstant + 20;
        canvas.drawText("Merchant ID", x, y, paint);
        canvas.drawText(mId, getApproxXToRightText(mId, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Terminal ID", x, y, paint);
        canvas.drawText(tId, getApproxXToRightText(tId, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Batch ID:", x, y, paint);
        canvas.drawText(bId, getApproxXToRightText(bId, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant + 10;
        canvas.drawText(divText, getApproxXToCenterText(divText, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, bWidth), y, paint);

        y = y + yConstant + 20;
        canvas.drawText("Date Time", x, y, paint);
        canvas.drawText(dateTime, getApproxXToRightText(dateTime, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Invoice No", x, y, paint);
        canvas.drawText(dummy, getApproxXToRightText(dummy, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Wallet ID", x, y, paint);
        canvas.drawText("001", getApproxXToRightText("001", Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Approval Code", x, y, paint);
        canvas.drawText(appCode, getApproxXToRightText(appCode, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Retrial Reference No", x, y, paint);
        canvas.drawText(dummy, getApproxXToRightText(dummy, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Surcharge", x, y, paint);
        canvas.drawText(dummy, getApproxXToRightText(dummy, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant;
        canvas.drawText("Exchange Rate", x, y, paint);
        canvas.drawText(exRate, getApproxXToRightText(exRate, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        y = y + yConstant + 10;
        canvas.drawText(divText, getApproxXToCenterText(divText, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 22, bWidth), y, paint);

        y = y + yConstant + 20;
        canvas.drawText("Transaction Amount", x, y, paint);
        paint.setTextSize(20);
        canvas.drawText(totalAmount, getApproxXToRightText(totalAmount, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20, bWidth), y, paint);

        String ln1 = "I agree to pay the above total amount";
        String ln2 = "according to the AliPay agreement";
        String ln3 = "Customer Copy";
        String ln4 = "Thank You";

        paint.setTextSize(17);
        y = y + yConstant + 20;
        canvas.drawText(ln1, x, y, paint);
        y = y + yConstant;
        canvas.drawText(ln2, x, y, paint);

        y = y + yConstant + 10;
        Bitmap printPayable = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_powered_by_payable);
        canvas.drawBitmap(printPayable, null, new Rect(x, y, 170, y + 50), null);

        y = y + yConstant + 60;
        canvas.drawText(ln3, x, y, paint);
        canvas.drawText(ln4, getApproxXToRightText(ln4, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 17, bWidth), y, paint);

        // y = y + yConstant + 150;
        // canvas.drawText("_", x, y, paint);

        return bitmap;
    }

    public static int getApproxXToRightText(String text, Typeface typeface, int fontSize, int widthToFitStringInto) {

        Paint p = new Paint();
        p.setTypeface(typeface);
        p.setTextSize(fontSize);
        float textWidth = p.measureText(text);
        int xOffset = (int) ((widthToFitStringInto - textWidth));
        if (xOffset < 0) {
            xOffset = 20;
        } else {
            xOffset = xOffset - 20;
        }
        return xOffset;
    }

    public static int getApproxXToCenterText(String text, Typeface typeface, int fontSize, int widthToFitStringInto) {

        Paint p = new Paint();
        p.setTypeface(typeface);
        p.setTextSize(fontSize);
        float textWidth = p.measureText(text);
        int xOffset = (int) ((widthToFitStringInto - textWidth) / 2f) - (int) (fontSize / 2f);
        if (xOffset < 0) {
            xOffset = 20;
        } else {
            xOffset = xOffset + 20;
        }
        return xOffset;
    }
}
