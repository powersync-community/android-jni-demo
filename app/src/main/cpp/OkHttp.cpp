#include "OkHttp.h"
#include "JNIEnv.h"
#include <android/log.h>
#include <jni.h>

namespace PowerSync {
std::string get(const std::string &url) {
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->FindClass("com/powersync/demo/android/jni/wrapper/OkHttpClient");
  jmethodID get_method = env->GetStaticMethodID(
      clazz, "get", "(Ljava/lang/String;)Ljava/lang/String;");
  jstring jurl = env->NewStringUTF(url.c_str());
  jobject jbody = env->CallStaticObjectMethod(clazz, get_method, jurl);

  const char *utf_chars = env->GetStringUTFChars((jstring)jbody, nullptr);
  std::string body(utf_chars); // Copy the string content

  // Release the UTF string before deleting the references
  env->ReleaseStringUTFChars((jstring)jbody, utf_chars);
  env->DeleteLocalRef(jurl);
  env->DeleteLocalRef(jbody);

  return body;
}
} // namespace PowerSync