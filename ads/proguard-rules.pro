# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes Exceptions, Signature, InnerClasses

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

#================================= ADJUST

-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
#pangle
-keep class com.bytedance.sdk.openadsdk.** { *; }
#Appsflyer
-keep class com.appsflyer.** { *; }

#tintergal
-keepattributes Signature
-keepattributes *Annotation*
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep class com.android.billingclient.api.** { *; }
-keep public class * extends androidx.** { *; }
-keep class **.R$* { public static final int mbridge*; }

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-keep, allowobfuscation class com.ads.admob.**
-keepclassmembers, allowobfuscation class * { *; }
-keepnames class com.ads.admob.**
-keepclassmembernames class com.ads.admob.** {
    public <methods>;
    public <fields>;
}
-keepclassmembers class *.R$ {
    public static <fields>;
}
-keepparameternames

-keep class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
   int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
   com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
   java.lang.String getId();
   boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.invoke.StringConcatFactory