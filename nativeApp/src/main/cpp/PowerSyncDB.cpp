#include "PowerSyncDB.h"
#include "JNIEnv.h"
#include "utils.h"
#include <android/log.h>
#include <map>
#include <utility>

namespace PowerSync {

// This is a global map, not per instace, careful
// Map to hold status callbacks, keyed by a string identifier.
std::map<std::string, std::function<void(const std::string &status)>>
    status_callbacks;

std::map<std::string, std::function<void()>>
    table_callbacks;


extern "C" JNIEXPORT void JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeDB_tablesChangedCallback(JNIEnv *env,
                                                              jobject thiz,
                                                              jstring id) {
  std::string idStr = std::string(env->GetStringUTFChars(id, nullptr));
  auto callbackIt = table_callbacks.find(idStr);
  if (callbackIt != table_callbacks.end()) {
    callbackIt->second();
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeDB_syncStatusCallback(JNIEnv *env,
jobject thiz,
        jstring status,
jstring id) {
std::string idStr = std::string(env->GetStringUTFChars(id, nullptr));
std::string statusStr = std::string(env->GetStringUTFChars(status, nullptr));
auto callbackIt = status_callbacks.find(idStr);
if (callbackIt != status_callbacks.end()) {
callbackIt->second(statusStr);
}
}

DB::DB(Schema schema) : db(db) {
  // Generate a random string for the id property
  id = create_random_id();
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->FindClass("com/powersync/demo/android/jni/wrapper/NativeBridge");
  jmethodID createDbMethod = env->GetStaticMethodID(
      clazz, "createDbWithDefaultDatabaseDriverFactory",
      "(Lcom/powersync/db/schema/Schema;)Lcom/powersync/PowerSyncDatabase;");
  jobject schemaHandle = schema.getSchema();
  jobject dbHandle =
      env->CallStaticObjectMethod(clazz, createDbMethod, schemaHandle);

  db = env->NewGlobalRef(dbHandle);

  clazz = env->FindClass("com/powersync/demo/android/jni/wrapper/NativeDB");

  jmethodID ctor = env->GetMethodID(
      clazz, "<init>",
      "(Lcom/powersync/PowerSyncDatabase;Ljava/lang/String;)V");
  jstring jid = env->NewStringUTF(id.c_str());
  jobject nativeDbHandle = env->NewObject(clazz, ctor, db, jid);
  nativeDb = env->NewGlobalRef(nativeDbHandle);
  env->DeleteLocalRef(jid);
}

void DB::execute(const std::string &query,
                 const std::vector<std::string> &params) {
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->GetObjectClass(nativeDb);

  jmethodID methodId =
      env->GetMethodID(clazz, "execute",
                              "(Ljava/lang/String;Ljava/util/List;)V");
  jstring jQuery = env->NewStringUTF(query.c_str());

  // Create a new Java ArrayList
  jclass arrayListClass = env->FindClass("java/util/ArrayList");
  jmethodID arrayListCtor = env->GetMethodID(arrayListClass, "<init>", "()V");
  jobject jParamsList = env->NewObject(arrayListClass, arrayListCtor);

  // Add each param to the ArrayList
  jmethodID addMethod =
      env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
  for (const auto &param : params) {
    jstring jParam = env->NewStringUTF(param.c_str());
    env->CallBooleanMethod(jParamsList, addMethod, jParam);
    env->DeleteLocalRef(jParam);
  }

  // Now call the Java method with the constructed arguments
  env->CallVoidMethod(nativeDb, methodId, jQuery, jParamsList);

  // Clean up local references
  env->DeleteLocalRef(jQuery);
  env->DeleteLocalRef(jParamsList);
  env->DeleteLocalRef(arrayListClass);
}

std::string DB::getAll(const std::string &query,
                       const std::vector<std::string> &params) {
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->GetObjectClass(nativeDb);

  jmethodID methodId = env->GetMethodID(
      clazz, "getAll",
      "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;");
  jstring jQuery = env->NewStringUTF(query.c_str());

  // Create a new Java ArrayList
  jclass arrayListClass = env->FindClass("java/util/ArrayList");
  jmethodID arrayListCtor = env->GetMethodID(arrayListClass, "<init>", "()V");
  jobject jParamsList = env->NewObject(arrayListClass, arrayListCtor);

  // Add each param to the ArrayList
  jmethodID addMethod =
      env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
  for (const auto &param : params) {
    jstring jParam = env->NewStringUTF(param.c_str());
    env->CallBooleanMethod(jParamsList, addMethod, jParam);
    env->DeleteLocalRef(jParam);
  }

  // Now call the Java method with the constructed arguments
  jobject res = env->CallObjectMethod(nativeDb, methodId, jQuery, jParamsList);

  const char *str = env->GetStringUTFChars((jstring)res, nullptr);
  std::string result(str);

  // Clean up local references
  env->DeleteLocalRef(jQuery);
  env->DeleteLocalRef(jParamsList);
  env->DeleteLocalRef(arrayListClass);

  return result;
}

void DB::connect(Connector connector) {
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->GetObjectClass(nativeDb);
  jmethodID methodId = env->GetMethodID(clazz, "connect", "(Lcom/powersync/demo/android/jni/wrapper/NativeConnector;)V");
  env->CallVoidMethod(nativeDb, methodId, connector.getHandle());
}

void DB::register_status_listener(
    std::function<void(const std::string &)> status_callback) {

  status_callbacks[id] = std::move(status_callback);
}

void DB::register_table_listener(std::function<void()> callback) {
    table_callbacks[id] = std::move(callback);
}

} // namespace PowerSync