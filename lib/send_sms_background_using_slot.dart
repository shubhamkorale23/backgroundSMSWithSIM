library send_sms_background_using_slot;

import 'dart:async';

import 'package:flutter/services.dart';

enum SmsStatus { sent, failed }

class SMSSenderWithSimSlot {
  static const MethodChannel _channel = MethodChannel('background_sms_with_slot');

  static Future<SmsStatus> sendMessage(
      {required String phoneNumber,
        required String message,
        required int simSlot}) async {
    try {
      String? result = await _channel.invokeMethod('sendSMS', <String, dynamic>{
        "phone": phoneNumber,
        "msg": message,
        "simSlot": simSlot
      });
      return result == "Sent" ? SmsStatus.sent : SmsStatus.failed;
    } on PlatformException catch (e) {
      print(e);
      return SmsStatus.failed;
    }
  }
}