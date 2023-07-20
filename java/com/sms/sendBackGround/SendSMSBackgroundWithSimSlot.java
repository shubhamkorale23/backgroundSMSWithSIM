package com.sms.sendBackGround;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** BackgroundSmsWithSIMSlotPlugin */
public class SendSMSBackgroundWithSimSlot implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "background_sms_with_slot");
        channel.setMethodCallHandler(this);
    }


    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "background_sms_with_slot");
        channel.setMethodCallHandler(new BackgroundSmsPlugin());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("sendSMS")) {
            String num = call.argument("phone");
            String msg = call.argument("msg");
            Integer simSlot = call.argument("simSlot");
            sendSMS(num, msg, simSlot, result);
        } else{
            result.notImplemented();
        }
    }

    private void isSupportCustomSim(Result result){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            result.success(true);
        }else{
            result.success(false);
        }
    }

    private void sendSMS(String num, String msg, Integer simSlot,Result result) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    result.error("Failed", "Permission Issue", "");
                    return;
                }
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(simSlot);
                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(num, null, msg, sentPI, deliveredPI);
                result.success("Sent");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.error("Failed", "Sms Not Sent", "");
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}